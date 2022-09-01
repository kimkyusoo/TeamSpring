package com.example.imageupload;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
@EnableScheduling
@EnableWebSecurity
@EnableJpaAuditing
@SpringBootApplication
public class ImageuploadApplication {

	public static void main(String[] args) {
		SpringApplication.run(ImageuploadApplication.class, args);
	}

}
