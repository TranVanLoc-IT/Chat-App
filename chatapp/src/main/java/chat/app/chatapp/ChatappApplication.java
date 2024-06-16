package chat.app.chatapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScans;

@SpringBootApplication
@ComponentScan(basePackages = "chat.app.chatapp.controller")
@ComponentScan(basePackages = "chat.app.chatapp.model")
@ComponentScan(basePackages = "chat.app.chatapp.config")
public class ChatappApplication {
	public static void main(String[] args) {
		SpringApplication.run(ChatappApplication.class, args);
	}
}
