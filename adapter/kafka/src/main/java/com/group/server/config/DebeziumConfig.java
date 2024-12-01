package com.group.server.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.debezium.engine.ChangeEvent;
import io.debezium.engine.DebeziumEngine;
import io.debezium.engine.format.Json;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DebeziumConfig {
	private final KafkaTemplate<String, String> kafkaTemplate;
	private final ObjectMapper objectMapper = new ObjectMapper();

	@Bean
	public io.debezium.config.Configuration connectorConfiguration() {
		return io.debezium.config.Configuration.create()
			.with("name", "mysql-connector")
			.with("connector.class", "io.debezium.connector.mysql.MySqlConnector")
			.with("database.hostname", "localhost")
			.with("database.port", "3306")
			.with("database.user", "myuser")
			.with("database.password", "mypassword")
			.with("database.server.id", "1")
			.with("topic.prefix", "campus")  // database.server.name과 동일하게 설정
			.with("database.include.list", "campus")
			.with("table.include.list", "campus.out_box_table")
			.with("database.history.kafka.bootstrap.servers", "localhost:9092,localhost:9093,localhost:9094")
			.with("database.history.kafka.topic", Topic.MY_CUSTOM_OUT_BOX_TOPIC)

			// Kafka 클러스터 설정
			.with("bootstrap.servers", "localhost:9092,localhost:9093,localhost:9094")
			// Schema History 관련 설정
			.with("schema.history.internal", "io.debezium.storage.kafka.history.KafkaSchemaHistory")
			.with("schema.history.internal.kafka.topic", "schema-changes.campus")
			.with("schema.history.internal.kafka.bootstrap.servers", "localhost:9092,localhost:9093,localhost:9094")
			// offset 설정
			.with("offset.storage.file.filename", "./offsets.dat")

			.build();
	}

	@Bean
	public DebeziumEngine<ChangeEvent<String, String>> debeziumEngine(io.debezium.config.Configuration connectorConfiguration) {
		return DebeziumEngine.create(Json.class)
			.using(connectorConfiguration.asProperties())
			.notifying(this::handleChangeEvent)
			.build();
	}

	/*
		private void handleChangeEvent(ChangeEvent<String, String> event) {
			log.info("Key = {}, Value = {}", event.key(), event.value());
		}
	*/

	private void handleChangeEvent(ChangeEvent<String, String> event) {
		try {
			if (event.value() != null) {
				// JSON을 파싱하여 payload만 추출
				JsonNode eventJson = objectMapper.readTree(event.value());
				JsonNode payload = eventJson.get("payload");

				// payload를 다시 문자열로 변환
				String payloadJson = objectMapper.writeValueAsString(payload);

				// payload의 after 노드에서 message 필드 추출
				String message = payload.get("after").get("message").asText();

				// Kafka로 메시지 전송
				kafkaTemplate.send(Topic.MY_CUSTOM_OUT_BOX_TOPIC, event.key(), payloadJson)
					.whenComplete((result, ex) -> {
						if (ex == null) {
							log.info("Message sent to topic: {}", Topic.MY_CUSTOM_OUT_BOX_TOPIC);
						} else {
							log.error("Failed to send message to topic: {}", Topic.MY_CUSTOM_OUT_BOX_TOPIC, ex);
						}
					});
			}
		} catch (Exception e) {
			log.error("Error processing event: {}", e.getMessage(), e);
		}
	}

}
