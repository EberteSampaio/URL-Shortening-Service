package br.com.deveberte.urlshortening.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@EnableWebSecurity
@Configuration
public class DefaultSecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity){
        httpSecurity
                .authorizeHttpRequests(auth ->{
                    auth.requestMatchers("/api/shorten/**", "/api/status", "/api/auth/**").permitAll()
                            .requestMatchers("/api/private/**").hasRole("ADMIN")
                            .anyRequest().authenticated();
                })
                .formLogin(Customizer.withDefaults())
                .logout(Customizer.withDefaults());

        return httpSecurity.getOrBuild();
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
}
