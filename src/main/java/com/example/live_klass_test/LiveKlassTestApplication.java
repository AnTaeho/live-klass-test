package com.example.live_klass_test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@EnableJpaAuditing
@SpringBootApplication
public class LiveKlassTestApplication {

	public static void main(String[] args) {
		SpringApplication.run(LiveKlassTestApplication.class, args);
	}

}
