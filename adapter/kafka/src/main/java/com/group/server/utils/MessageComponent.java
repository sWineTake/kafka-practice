package com.group.server.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageComponent {

	private final KafkaTemplate<String, String> kafkaTemplate;

	private static final String SUCCESS_MESSAGE_FORMAT = "Message successfully sent to Kafka topic {}: {}";
	private static final String FAIL_MESSAGE_FORMAT = "Failed to send message to Kafka topic {}: {}";

	public CompletableFuture<SendResult<String, String>> send(String message, String topicName) {
		return kafkaTemplate.send(topicName, message);
	}

	/*
	 * 모든 재시도 후에 마지막에 한번으로 호출됨
	 * */
	public void resultEvent(String message, String topicName, Throwable ex, Runnable successCallback) {
		if (ex == null) {
			log.info(SUCCESS_MESSAGE_FORMAT, topicName, message);
			if (successCallback != null) {
				successCallback.run();
			}
		} else {
			log.error(FAIL_MESSAGE_FORMAT, topicName, message, ex);
		}
	}
}
