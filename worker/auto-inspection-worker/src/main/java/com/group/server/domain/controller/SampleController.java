package com.group.server.domain.controller;

import com.group.server.event.SampleEventListener;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class SampleController {

	private final SampleEventListener sampleService;

	@GetMapping("/sample")
	public String sample() {
		return sampleService.onSampleEvent();
	}

}
