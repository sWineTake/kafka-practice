package com.group.server.config;

import com.group.server.domain.OutBoxTable;
import com.group.server.domain.OutBoxTableRepository;
import com.group.server.utils.MessageComponent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutBoxMessageSender {
	private final OutBoxTableRepository outBoxRepository;
	private final MessageComponent messageComponent;

	/*
		- 데이터 정합성 보장: DB 트랜잭션과 메시지 발행이 원자적으로 처리됨
		- 메시지 유실 방지: DB에 기록이 남아있어 재시도 가능
		- 장애 복구 용이: 아웃박스 테이블을 통해 실패한 메시지 추적/재처리 가능
		***** 아웃박스 메시지 사용 추천 케이스 *****
		case1. 중요한 비즈니스 이벤트 where 메시지 유실이 치명적인 경우
		case2. DB 트랜잭션과 메시지 발행의 원자성이 필요한 경우
		case3. 장애 추적과 복구가 중요한 경우
	*/
	@Transactional
	public void sendWithOutBox(String message, String topicName) {
		OutBoxTable outBox = saveOutBoxTable(message);

		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
			@Override
			public void afterCommit() {
				messageComponent.send(message, topicName)
					.whenComplete((result, ex) ->
						messageComponent.resultEvent(message, topicName, ex, () -> {
							outBox.updateSend(true);
							outBoxRepository.save(outBox);
						}
					)
				);
			}
		});
	}

	public void sendMessage(String message, String topicName) {
		messageComponent.send(message, topicName)
			.whenComplete((result, ex) -> messageComponent.resultEvent(message, topicName, ex, null));
	}

	public void sendWithDebezium(String message) {
		saveOutBoxTable(message);
	}

	private OutBoxTable saveOutBoxTable(String message) {
		return outBoxRepository.save(OutBoxTable.create(message));
	}
}
