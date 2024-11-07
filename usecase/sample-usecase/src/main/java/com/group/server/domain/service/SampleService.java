package com.group.server.domain.service;

import com.group.server.sample.SampleAdapter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SampleService {

	private final SampleAdapter sampleAdapter;

	public String sampleMethod() {
		return sampleAdapter.sampleMethod();
	}


}
