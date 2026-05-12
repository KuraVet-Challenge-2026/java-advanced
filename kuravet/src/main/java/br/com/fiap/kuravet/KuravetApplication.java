package br.com.fiap.kuravet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class KuravetApplication {

	public static void main(String[] args) {
		SpringApplication.run(KuravetApplication.class, args);
	}
}