package com.payment.processing_system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ProcessingSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProcessingSystemApplication.class, args);
	}

}
