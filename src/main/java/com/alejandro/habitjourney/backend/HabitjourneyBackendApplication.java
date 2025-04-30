package com.alejandro.habitjourney.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;


@SpringBootApplication(exclude = {
		UserDetailsServiceAutoConfiguration.class,
})

public class HabitjourneyBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(HabitjourneyBackendApplication.class, args);
	}

}
