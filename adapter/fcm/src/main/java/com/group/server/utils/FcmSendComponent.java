package com.group.server.utils;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
public class FcmSendComponent {

	@Async
	public CompletableFuture<String> send(String message) {
		return CompletableFuture.supplyAsync(() -> {
			// FCM 발송 로직
			return "발송 결과";
		});
	}

}
