package br.com.deveberte.urlshortening.domain.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table( schema = "public", name = "short_urls")
public class Link {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String url;
    @Column(unique = true)
    private String shortCode;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    public Link(){}
    public Link(String url, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.url = url;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Link create(String url){
        return new Link(url, LocalDateTime.now(), LocalDateTime.now());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getShortCode() {
        return shortCode;
    }

    public void setShortCode(String shortCode) {
        this.shortCode = shortCode;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}

