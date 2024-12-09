package com.group.server.config;


import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.json.JsonParseException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.BackOff;
import org.springframework.util.backoff.ExponentialBackOff;

import java.util.HashMap;
import java.util.Map;

import static com.group.server.config.Topic.MY_CUSTOM_CDC_TOPIC_DLT;

@Configuration
@EnableKafka
public class KafkaConfig {

	@Bean
	@Primary
	@ConfigurationProperties("spring.kafka")
	public KafkaProperties kafkaProperties() {
		return new KafkaProperties();
	}

	@Bean
	@Primary
	public ProducerFactory<String, Object> producerFactory(KafkaProperties kafkaProperties) {
		Map<String, Object> props = new HashMap<>();
		// 프로듀서가 연결할 서버의 주소
		props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
		// 키, 값의 직렬화 방법
		props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		// 전송 성공을 위한 설정 -> -1은 복제까지 모두 성공해야 성공으로 간주
		props.put(ProducerConfig.ACKS_CONFIG, "-1");
		// 프로듀서가 메시지에 고유 ID값을 넣음으로 중복 메세지 발행이 안되도록 하는설정
		props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");
		// 재시도 횟수 설정
		props.put(ProducerConfig.RETRIES_CONFIG, 3);
		// 재시도 대기 시간
		props.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, 1000);
		return new DefaultKafkaProducerFactory<>(props);
	}

	@Bean
	@Primary
	public KafkaTemplate<String, ?> kafkaTemplate(KafkaProperties kafkaProperties) {
		return new KafkaTemplate<>(producerFactory(kafkaProperties));
	}

	@Bean
	@Primary
	public ConsumerFactory<String, Object> consumerFactory(KafkaProperties kafkaProperties) {
		Map<String, Object> props = new HashMap<>();
		// 컨슈머가 연결할 서버의 주소
		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
		// 키, 값의 역직렬화 방법
		props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, kafkaProperties.getConsumer().getKeyDeserializer());
		props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, kafkaProperties.getConsumer().getValueDeserializer());
		// 컨슈머가 처음 실행될 때 어떻게 처리할지 설정 -> latest는 가장 최근에 발행된 메시지부터 처리
		props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
		// 컨슈머가 토픽을 자동으로 생성하지 않도록 설정
		props.put(ConsumerConfig.ALLOW_AUTO_CREATE_TOPICS_CONFIG, "false");
		// Kafka Consumer 레벨의 설정 : 수동 커밋으로 변경 - 컨슈머가 메시지를 처리한 후에만 커밋
		// ENABLE_AUTO_COMMIT_CONFIG=false는 "자동 커밋하지 마"라는 의미
		props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
		return new DefaultKafkaConsumerFactory<>(props);
	}

	@Bean
	@Primary
	public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory(
		ConsumerFactory<String, Object> consumerFactory,
		KafkaTemplate<String, Object> kafkaTemplate
	) {
		ConcurrentKafkaListenerContainerFactory<String, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
		factory.setConsumerFactory(consumerFactory);

		// 데드 레더 큐 추가 (미리 생성해놔야함 컨슈머에서는 토픽생성불가) -> 에러가 발생하면 해당 토픽으로 프로듀서됨
		// Todo) DLQ 토픽에 프로듀서되면 어떻게 처리할지?
		DefaultErrorHandler errorHandler = new DefaultErrorHandler(
			// 해당 정책 재발송으로 하여도 에러가 발생하면 - 데드레더 토픽에 프로듀서됨
			(recod, ex) -> {
				// recod : 재발송으로도 실패한 메시지
				kafkaTemplate.send(MY_CUSTOM_CDC_TOPIC_DLT, (String) recod.key(), recod.value());
			}
			, exponetialBackOff() // 해당 정책으로 재발송 실행
		);
		// 해당 에러 발생시 재시도 하지않음 (예외처리)
		errorHandler.addNotRetryableExceptions(JsonParseException.class);
		factory.setCommonErrorHandler(errorHandler);

		// Spring Kafka 컨테이너 레벨의 설정 : 수동 커밋으로 변경
		// AckMode.MANUAL은 "대신 수동으로 커밋할거야"라는 의미
		factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);

		return factory;
	}


	private BackOff exponetialBackOff() {
		ExponentialBackOff backOff = new ExponentialBackOff(1000L, 2L); // 1초 간격으로 시작해서 2배씩 간격이 증가
		// 최대 10초까지만 증가
		// backOff.setMaxElapsedTime(10000L);

		// 최대 실행 횟수 지정
		backOff.setMaxAttempts(3);
		return backOff;
	}


}
