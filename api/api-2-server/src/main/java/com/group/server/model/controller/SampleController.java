package com.group.server.model.controller;

import com.group.server.domain.service.SampleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class SampleController {

	private final SampleService sampleService;

	@GetMapping("/sample")
	public String sample() {
		return sampleService.sampleMethod();
	}


}
