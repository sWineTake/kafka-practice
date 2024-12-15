package com.group.server.utils;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Component
public class FcmSendComponent {

	@Async
	public CompletableFuture<String> send(String message) {
		return CompletableFuture.supplyAsync(() -> {
			// 강제 슬립 5초
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
			// FCM 발송 로직
			return "발송 결과";
		});
	}

}
