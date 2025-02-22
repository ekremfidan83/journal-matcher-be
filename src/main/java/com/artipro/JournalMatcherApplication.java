package com.artipro;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan("com.artipro.model.entity")
@EnableJpaRepositories("com.artipro.repository")
public class JournalMatcherApplication {

	public static void main(String[] args) {
		SpringApplication.run(JournalMatcherApplication.class, args);
	}

}
