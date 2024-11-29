package com.faitoncodes.core_processor_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CoreProcessorServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(CoreProcessorServiceApplication.class, args);
	}
	//TODO trocar todas as zonedDateTime por LocalDateTime:
	//Some examples of when I would want to use LocalDateTime:
	//
	//I'm assured that my system only needs to care about one time zone - mine.
}
