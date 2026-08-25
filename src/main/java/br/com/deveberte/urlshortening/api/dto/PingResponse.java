package br.com.deveberte.urlshortening.api.dto;

import java.time.LocalDateTime;

public record PingResponse(String status, LocalDateTime timestamp) {
}
