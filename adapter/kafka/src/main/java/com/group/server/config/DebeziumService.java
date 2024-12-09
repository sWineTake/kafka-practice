package com.group.server.config;

import io.debezium.engine.ChangeEvent;
import io.debezium.engine.DebeziumEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
// @Service // 해당 주석 해지시 디비지움 연동
@RequiredArgsConstructor
public class DebeziumService implements InitializingBean, DisposableBean {

	private final DebeziumEngine<ChangeEvent<String, String>> debeziumEngine;
	private final ExecutorService executor = Executors.newSingleThreadExecutor();

	@Override
	public void afterPropertiesSet() {
		executor.execute(debeziumEngine);
	}

	@Override
	public void destroy() {
		try {
			debeziumEngine.close();
		} catch (IOException e) {
			log.error("Error while shutting down Debezium", e);
		}
		executor.shutdownNow();
	}
}
