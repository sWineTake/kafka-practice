package com.group.server.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.debezium.engine.ChangeEvent;
import io.debezium.engine.DebeziumEngine;
import io.debezium.engine.format.Json;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
			.with("topic.prefix", "campus")
			.with("database.include.list", "campus")
			.with("table.include.list", "campus.out_box_table")

			// 스키마 히스토리 설정 통합
			.with("schema.history.internal", "io.debezium.storage.kafka.history.KafkaSchemaHistory")
			.with("schema.history.internal.kafka.topic", "schema-changes.campus")
			.with("schema.history.internal.kafka.bootstrap.servers", "localhost:9092,localhost:9093,localhost:9094")

			// 복구 관련 설정 추가
			.with("schema.history.internal.recovery.attempts", "3")
			.with("schema.history.internal.recovery.poll.interval.ms", "1000")

			// offset 설정
			.with("offset.storage", "org.apache.kafka.connect.storage.FileOffsetBackingStore")
			.with("offset.storage.file.filename", "./offsets.dat")
			.with("offset.flush.interval.ms", "1000")

			// .with("database.server.id", serverId)  // 동적으로 설정된 server.id
			// .with("group.id", "campus-group")      // 그룹 ID 추가

			.build();
	}

	@Bean
	public DebeziumEngine<ChangeEvent<String, String>> debeziumEngine(io.debezium.config.Configuration connectorConfiguration) {
		return DebeziumEngine.create(Json.class)
			.using(connectorConfiguration.asProperties())
			.notifying(this::handleChangeEvent)
			.build();
	}

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
					}
				);
			}
		} catch (Exception e) {
			log.error("Error processing event: {}", e.getMessage(), e);
		}
	}

}
