package br.com.deveberte.urlshortening.api.resource;

import br.com.deveberte.urlshortening.api.dto.RegisterRequest;
import br.com.deveberte.urlshortening.service.RegisterService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth/register")
public class RegisterResourcer {

    private final RegisterService service;

    public RegisterResourcer(RegisterService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Void> register(RegisterRequest request){
        this.service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
