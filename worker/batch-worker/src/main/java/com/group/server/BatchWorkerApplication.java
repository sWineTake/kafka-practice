package com.group.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BatchWorkerApplication {

	public static void main(String[] args) {
		SpringApplication.run(BatchWorkerApplication.class, args);
	}

}
