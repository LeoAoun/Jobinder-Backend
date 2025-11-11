package br.com.jobinder.chatservice;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition(info = @Info(
		title = "Jobinder - Chat Service API",
		version = "1.0",
		description = "API for managing chat communications between users"
))
public class ChatserviceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ChatserviceApplication.class, args);
	}

}
