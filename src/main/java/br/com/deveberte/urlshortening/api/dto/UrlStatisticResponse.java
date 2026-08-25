package br.com.deveberte.urlshortening.api.dto;

import br.com.deveberte.urlshortening.domain.entity.Link;

import java.time.LocalDateTime;

public record UrlStatisticResponse(
        Long id,
        String url,
        String shortcode,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Long accessCount
) {

    public static UrlStatisticResponse of(Link link){
        return new UrlStatisticResponse(
                link.getId(),
                link.getUrl(),
                link.getShortCode(),
                link.getCreatedAt(),
                link.getUpdatedAt(),
                link.getAccessCount()
        );
    }
}
