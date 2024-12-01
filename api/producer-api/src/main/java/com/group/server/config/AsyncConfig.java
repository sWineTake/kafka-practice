package com.group.server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAsync
public class AsyncConfig {

	@Bean
	public Executor asyncExecutor() {
		int processors = Runtime.getRuntime().availableProcessors();
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

		// CPU 코어 수 * 2: I/O 바운드 작업을 고려한 설정
		executor.setCorePoolSize(processors * 2);

		// 코어 스레드의 2배: 부하가 높을 때 추가 처리 가능
		executor.setMaxPoolSize(processors * 4);

		// 작업 큐 크기: 코어 수 * 50
		executor.setQueueCapacity(processors * 50);

		// 유휴 스레드 유지 시간 (60초)
		executor.setKeepAliveSeconds(60);

		// 큐가 가득 찼을 때의 정책 설정 -> 큐가 가득 찼을때 호출 스레드에서 직접 설정
		executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

		// 스레드 이름 접두사
		executor.setThreadNamePrefix("AsyncSlack-");

		// 종료 시 진행 중인 작업 완료 대기
		executor.setWaitForTasksToCompleteOnShutdown(true);

		// 종료 대기 시간 설정 (30초)
		executor.setAwaitTerminationSeconds(30);

		executor.initialize();
		return executor;
	}
}
