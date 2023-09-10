package com.example.chang;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class ChangApplication {

	public static void main(String[] args) {
		SpringApplication.run(ChangApplication.class, args);
	}

}
