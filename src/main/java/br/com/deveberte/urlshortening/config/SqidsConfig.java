package br.com.deveberte.urlshortening.config;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.sqids.Sqids;


@Configuration
@EnableConfigurationProperties(SqidsRecord.class)
public class SqidsConfig {
    private static int MIN_LENGHT;
    private static  String ALPHABET;

    @Bean
    public Sqids sqids(){
        return Sqids.builder().minLength(MIN_LENGHT).alphabet(ALPHABET).build();
    }
}
