package br.com.deveberte.urlshortening.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.sqids")
public record SqidsRecord (int minLenght, String alphabet){
}
