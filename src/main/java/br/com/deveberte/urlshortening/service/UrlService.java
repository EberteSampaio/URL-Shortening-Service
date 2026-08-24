package br.com.deveberte.urlshortening.service;

import br.com.deveberte.urlshortening.exception.UrlNotFoundException;
import br.com.deveberte.urlshortening.repository.UrlRepository;
import br.com.deveberte.urlshortening.domain.entity.Link;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotBlank;
import org.springframework.stereotype.Service;
import org.sqids.Sqids;

import java.util.*;

@Service
public class UrlService
{
    private final UrlRepository urlRepository;
    private final Sqids sqids;

    public UrlService(UrlRepository urlRepository, Sqids sqids) {
        this.urlRepository = urlRepository;
        this.sqids = sqids;
    }

    @Transactional
    public Link create(String originalUrl){
        Optional<Link> byUrl = this.urlRepository.findByUrl(originalUrl);

        if (byUrl.isPresent()) {
            return byUrl.get();
        }

        Link link = this.urlRepository.save(Link.create(originalUrl));

        String shortUrl = this.sqids.encode(Collections.singletonList(link.getId()));
        link.setShortCode(shortUrl);

        return this.urlRepository.save(link);
    }

    public Link getLinkByShortCode(String shortCode) {
        return getLink(shortCode);
    }

    @Transactional
    public Link update(String shortCode, String newUrl) {
        Link link = getLink(shortCode);

        link.setUrl(newUrl);

        return this.urlRepository.save(link);
    }

    public void delete(String shortCode){
        Link link = getLink(shortCode);
        this.urlRepository.delete(link);
    }

    private Link getLink(String shortCode) {
        return this.urlRepository.findFirstByShortCode(shortCode).orElseThrow(() -> new UrlNotFoundException("URL não encontrada"));
    }
}
