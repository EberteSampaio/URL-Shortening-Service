package br.com.deveberte.urlshortening.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.sqids.Sqids;


@Configuration
public class SqidsConfig {
    private static final int MIN_LENGHT = 7;
    private static final String ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    @Bean
    public Sqids sqids(){
        return Sqids.builder().minLength(MIN_LENGHT).alphabet(ALPHABET).build();
    }
}
