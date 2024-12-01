package com.group.server.controller;

import com.group.server.service.ProducerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ProducerController {

	private final ProducerService producerService;

	@PostMapping("/api/v1.0/producer-outbox")
	public void produceMessageWithOutBox() {
		producerService.saveMessageWithOutBox("Hello Kafka");
	}

	@PostMapping("/api/v1.0/producer")
	public void produceMessage() {
		producerService.saveMessage("Hello Kafka");
	}

}
