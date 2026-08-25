package br.com.deveberte.urlshortening;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
public class UrlShorteningApplication {

	public static void main(String[] args) {
		SpringApplication.run(UrlShorteningApplication.class, args);
	}

}
