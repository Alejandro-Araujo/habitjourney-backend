package com.alejandro.habitjourney.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("com.alejandro.habitjourney.backend.user.mapper")
public class HabitjourneyBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(HabitjourneyBackendApplication.class, args);
	}

}
