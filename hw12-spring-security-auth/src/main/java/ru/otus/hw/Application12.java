package ru.otus.hw;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application12 {

	public static void main(String[] args) {
		SpringApplication.run(Application12.class, args);
		System.out.printf("Чтобы перейти на страницу сайта необходимо открыть: %n%s%n%s",
				"http://localhost:8080",
				"страница с эндпоинтами swagger-ui " +
				"http://localhost:8080/webjars/swagger-ui/index.html (с запросом /v3/api-docs)");
	}

}
