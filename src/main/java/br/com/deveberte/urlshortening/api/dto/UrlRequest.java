package br.com.deveberte.urlshortening.api.dto;

import jakarta.validation.constraints.NotBlank;

public record UrlRequest(
        @NotBlank(message = "O link é inválido") String link
) {
}

