package com.example.springbatchtest;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
@RequiredArgsConstructor
@EnableBatchProcessing
@SpringBootApplication
public class SpringBatchTestApplication {
	private final JobLauncher jobLauncher;
	private final Job txDailyMonthlyJob;
	public static void main(String[] args) {
		SpringApplication.run(SpringBatchTestApplication.class, args);



	}

}
