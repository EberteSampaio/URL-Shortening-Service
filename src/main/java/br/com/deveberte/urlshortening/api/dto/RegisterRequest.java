package br.com.deveberte.urlshortening.api.dto;

import br.com.deveberte.urlshortening.exception.DomainException;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegisterRequest(
        @NotBlank(message = "O username deve ser preenchido")
        String username,
        @Email(message = "O e-mail deve ser válido")
        String email,
        @NotBlank(message = "A senha é obrigatória")
        String password,
        @NotBlank(message = "A confirmação de senha é obrigatória")
        String confirmPassword) {

    public RegisterRequest(String username, String email, String password, String confirmPassword) {
        if (!password.equals(confirmPassword)){
            throw new DomainException("Senhas não coincidem");
        }
        this.username = username;
        this.email = email;
        this.password = password;
        this.confirmPassword = confirmPassword;
    }
}
