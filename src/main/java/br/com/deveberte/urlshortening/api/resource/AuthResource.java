package br.com.deveberte.urlshortening.api.resource;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthResource {
    @PostMapping("/login")
    public void doLogin(){

    }
}
