package com.group.server.event;

import com.group.server.sample.SampleAdapter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SampleEventListener {

	private final SampleAdapter sampleAdapter;

	public String onSampleEvent() {
		return sampleAdapter.sampleMethod();
	}

}
