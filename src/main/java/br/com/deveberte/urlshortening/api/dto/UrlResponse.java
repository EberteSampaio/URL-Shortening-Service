package br.com.deveberte.urlshortening.api.dto;

import br.com.deveberte.urlshortening.domain.entity.Link;

import java.time.LocalDateTime;

public record UrlResponse(
        Long id,
        String url,
        String shortcode,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static UrlResponse of(Link link){
        return new UrlResponse(link.getId(), link.getUrl(), link.getShortCode(), link.getCreatedAt(), link.getUpdatedAt());
    }
}
