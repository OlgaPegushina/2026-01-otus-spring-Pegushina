package ru.otus.hw;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application10 {

	public static void main(String[] args) {
		SpringApplication.run(Application10.class, args);
		System.out.printf("Чтобы перейти на страницу сайта необходимо открыть: %n%s%n",
				"http://localhost:8080");
	}

}
