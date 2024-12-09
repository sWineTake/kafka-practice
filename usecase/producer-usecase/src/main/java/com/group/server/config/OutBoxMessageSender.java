package com.group.server.config;

import com.group.server.domain.OutBoxTable;
import com.group.server.domain.OutBoxTableRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutBoxMessageSender {
	private final OutBoxTableRepository outBoxRepository;
	private final KafkaTemplate<String, String> kafkaTemplate;

	private static final String SUCCESS_MESSAGE_FORMAT = "Message successfully sent to Kafka topic {}: {}";
	private static final String FAIL_MESSAGE_FORMAT = "Failed to send message to Kafka topic {}: {}";

	@Transactional
	/*
		- 데이터 정합성 보장: DB 트랜잭션과 메시지 발행이 원자적으로 처리됨
		- 메시지 유실 방지: DB에 기록이 남아있어 재시도 가능
		- 장애 복구 용이: 아웃박스 테이블을 통해 실패한 메시지 추적/재처리 가능
		***** 아웃박스 메시지 사용 추천 케이스 *****
		case1. 중요한 비즈니스 이벤트 where 메시지 유실이 치명적인 경우
		case2. DB 트랜잭션과 메시지 발행의 원자성이 필요한 경우
		case3. 장애 추적과 복구가 중요한 경우
	*/
	public void sendWithOutBox(String message, String topicName) {
		OutBoxTable outBox = saveOutBoxTable(message);

		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
			@Override
			public void afterCommit() {
				sendMethodCreate(message, topicName)
					.whenComplete(
						(result, ex) ->
						handleKafkaResult(message, topicName, ex, () -> {
							outBox.updateSend(true);
							outBoxRepository.save(outBox);
						}
					)
				);
			}
		});
	}

	public void sendMessage(String message, String topicName) {
		sendMethodCreate(message, topicName)
			.whenComplete((result, ex) -> handleKafkaResult(message, topicName, ex, null));
	}

	public void sendWithDebezium(String message) {
		saveOutBoxTable(message);
	}

	private CompletableFuture<SendResult<String, String>> sendMethodCreate(String message, String topicName) {
		return kafkaTemplate.send(topicName, message);
	}

	private OutBoxTable saveOutBoxTable(String message) {
		OutBoxTable outBox = OutBoxTable.create(
			message,
			false,
			LocalDateTime.now()
		);
		outBoxRepository.save(outBox);
		return outBox;
	}

	/*
	* 모든 재시도 후에 마지막에 한번으로 호출됨
	* */
	private void handleKafkaResult(String message, String topicName, Throwable ex, Runnable successCallback) {
		if (ex == null) {
			/* log.info(SUCCESS_MESSAGE_FORMAT, topicName, message); */
			if (successCallback != null) {
				successCallback.run();
			}
		} else {
			log.error(FAIL_MESSAGE_FORMAT, topicName, message, ex);
		}
	}
}
