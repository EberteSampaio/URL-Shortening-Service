package br.com.deveberte.urlshortening.service;

import br.com.deveberte.urlshortening.api.dto.RegisterRequest;
import br.com.deveberte.urlshortening.domain.entity.User;
import br.com.deveberte.urlshortening.exception.DomainException;
import br.com.deveberte.urlshortening.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class RegisterService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public RegisterService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void create(RegisterRequest request){

        if (this.userRepository.existsByUsername(request.username())){
            throw new DomainException("Já existe um usuário com este e-mail");
        }

        if (this.userRepository.existsByEmail(request.email())) {
            throw new DomainException("Já existe um usuário com este e-mail");
        }

        this.userRepository.save(User.of(request, this.passwordEncoder));
    }
}
