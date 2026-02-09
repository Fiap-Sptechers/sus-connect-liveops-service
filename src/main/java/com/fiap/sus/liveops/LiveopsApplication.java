package com.fiap.sus.liveops;

import io.mongock.runner.springboot.EnableMongock;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@EnableMongock
@SpringBootApplication
public class LiveopsApplication {

	public static void main(String[] args) {
		SpringApplication.run(LiveopsApplication.class, args);
	}

}
