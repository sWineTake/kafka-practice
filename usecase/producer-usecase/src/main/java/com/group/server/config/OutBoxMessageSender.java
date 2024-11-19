package com.group.server.config;

import com.group.server.domain.OutBoxTable;
import com.group.server.domain.OutBoxTableRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutBoxMessageSender {
	private final OutBoxTableRepository outBoxRepository;
	private final KafkaTemplate<String, String> kafkaTemplate;

	@Transactional
	public void sendWithOutBox(String message, String topicName) {
		OutBoxTable outBox = OutBoxTable.create(
			message,
			false,
			LocalDateTime.now()
		);
		outBoxRepository.save(outBox);

		// 트랜잭션이 커밋된 후에 메세지를 전송함
		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
			@Override
			public void afterCommit() {
				kafkaTemplate.send(topicName, message)
					.whenComplete((result, ex) -> {
						if (ex == null) {
							outBox.updateSend(true);
							outBoxRepository.save(outBox);
							log.info("Message successfully sent to Kafka topic {}: {}", topicName, message);
						} else {
							log.error("Failed to send message to Kafka topic {}: {}", topicName, message, ex);
						}
					});
			}
		});
	}
}
