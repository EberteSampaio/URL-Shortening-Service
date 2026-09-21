package br.com.deveberte.urlshortening.service;

import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final PasswordEncoder passwordEncoder;

    public AuthService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public void loginValidate(){
        this.passwordEncoder.matches("08012004", this.passwordEncoder.encode("08012004"));
    }
}
