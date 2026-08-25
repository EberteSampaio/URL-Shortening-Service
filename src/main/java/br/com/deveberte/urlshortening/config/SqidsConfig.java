package br.com.deveberte.urlshortening.config;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.sqids.Sqids;


@Configuration
@EnableConfigurationProperties(SqidsRecord.class)
public class SqidsConfig {

    @Bean
    public Sqids sqids(SqidsRecord properties){
        return Sqids.builder().minLength(properties.minLength()).alphabet(properties.alphabet()).build();
    }
}
