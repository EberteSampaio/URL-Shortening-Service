package br.com.deveberte.urlshortening.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.constraints.URL;

public record UrlRequest(
        @NotBlank(message = "O link é inválido")
        @URL(message = "O link deve ser uma URL válida")
        @Pattern(regexp = "^https?://.+")
        String link
) {
}

