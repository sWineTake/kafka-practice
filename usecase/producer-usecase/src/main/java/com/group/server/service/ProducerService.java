package com.group.server.service;

import com.group.server.config.OutBoxMessageSender;
import com.group.server.config.Topic;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProducerService {

	private final OutBoxMessageSender outBoxMessageSender;

	@Transactional
	public void saveMessageWithOutBox(String message) {
		// 비즈니스 로직 START - END

		// OutBox에 메시지 저장 -> 아웃박스 패턴에 의한 트랙잭션을 같이 가져가기 위한 수단
		outBoxMessageSender.sendWithOutBox(message, Topic.MY_CUSTOM_OUT_BOX_TOPIC);

		// case1. 강제 에러 발생 : OutBoxTable에 저장도 되지않고 kafka 메세지도 전송되지 않음
		// throw new RuntimeException("강제 에러 발생");
	}

	/* 단순 메시지 발생 */
	public void saveMessage(String message) {

		// 메세지 발송
		outBoxMessageSender.sendMessage(message, Topic.MY_CUSTOM_OUT_BOX_TOPIC);

		// case1. 강제 에러 발생 : 에러에 상관없이 카프카 메세지를 보냄
		// throw new RuntimeException("강제 에러 발생");
	}

	@Transactional
	public void saveMessageWithDebezium(String message) {

		// 메세지 발송
		outBoxMessageSender.sendWithDebezium(message);

		// case1. 강제 에러 발생 : 에러 발생시 OutBox테이블에 데이터가 저장되지 않기에 카프카 메시지가 발송되지않음
		// throw new RuntimeException("강제 에러 발생");
	}
}
