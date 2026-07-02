package com.task.app.demo;

import com.task.app.demo.repository.TaskRepository;
import com.task.app.demo.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	@Bean
	public CommandLineRunner cleanDatabase(TaskRepository taskRepository, UserRepository userRepository) {
		return args -> {
			System.out.println("Cleaning database records on startup...");
			try {
				taskRepository.deleteAll();
				userRepository.deleteAll();
				System.out.println("Database records successfully cleaned.");
			} catch (Exception e) {
				System.err.println("Could not clean database: " + e.getMessage());
			}
		};
	}
}
