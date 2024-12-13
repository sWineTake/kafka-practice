package com.group.server.service;

import com.group.server.utils.FcmSendComponent;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import static com.group.server.config.Topic.MY_CUSTOM_CDC_TOPIC_DLT;
import static com.group.server.config.Topic.MY_CUSTOM_OUT_BOX_TOPIC;

@Component
@RequiredArgsConstructor
public class ConsumerComponent {

	private final FcmSendComponent fcmSendComponent;

	@KafkaListener(
		topics = {MY_CUSTOM_OUT_BOX_TOPIC},
		groupId = "out-box-consumer-group-1",
		containerFactory = "kafkaListenerContainerFactory",
		concurrency = "2" // 토픽 파티션 갯수 / 서버 갯수 = concurrency
	)
	public void outBoxConsumer(ConsumerRecord<String, String> message, Acknowledgment acknowledgment) {
		System.out.println("[OUTBOX]****************************" + message.partition());
		System.out.println(message.value());

		// case1. JsonParseException 발생 재시도 하지않음
		// throw new JsonParseException();

		// case2. 그외는 재시도 시도
		// throw new IllegalArgumentException("Something happened!");

		// 수동으로 커밋
		acknowledgment.acknowledge();
	}

	@KafkaListener(
		topics = {MY_CUSTOM_OUT_BOX_TOPIC},
		groupId = "out-box-consumer-group-1",
		containerFactory = "kafkaListenerContainerFactory",
		concurrency = "2"
	)
	public void exampleSendFcm(ConsumerRecord<String, String> message, Acknowledgment acknowledgment) {
		/*
			컨슈머 스레드가 블로킹되지 않습니다
			FCM 전송 결과가 오면 그때 callback으로 처리합니다
			실패시 예외를 던져서 재시도가 가능하게 합니다
		*/
		fcmSendComponent.send(message.value())
			.thenAccept(result -> {
				// FCM 전송 성공 시
				acknowledgment.acknowledge();
			})
			.exceptionally(throwable -> {
				// FCM 전송 실패 시
				throw new IllegalArgumentException("FCM 발송실패 - 재시도");
			});
	}



	@KafkaListener(
		topics = {MY_CUSTOM_CDC_TOPIC_DLT},
		groupId = "cdc-consumer-group-1",
		containerFactory = "kafkaListenerContainerFactory",
		concurrency = "3"
	)
	public void cdcConsumer(ConsumerRecord<String, String> message, Acknowledgment acknowledgment) {
		System.out.println("[CDC]****************************" + message.partition());
		System.out.println(message.value());

		// 수동으로 커밋
		acknowledgment.acknowledge();
	}

}
