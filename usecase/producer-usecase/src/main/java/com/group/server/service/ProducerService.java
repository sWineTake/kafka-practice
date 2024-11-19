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
	public void saveMessage(String message) {

		// 비즈니스 로직 START - END

		// OutBox에 메시지 저장 -> 아웃박스 패턴에 의한 트랙잭션을 같이 가져가기 위한 수단
		outBoxMessageSender.sendWithOutBox(message, Topic.MY_CUSTOM_OUT_BOX_TOPIC);

		// case1. 강제 에러 발생 : OutBoxTable에 저장도 되지않고 kafka 메세지도 전송되지 않음
		// throw new RuntimeException("강제 에러 발생");
	}
}
