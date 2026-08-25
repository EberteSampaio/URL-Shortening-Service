package br.com.deveberte.urlshortening.service;

import br.com.deveberte.urlshortening.exception.UrlNotFoundException;
import br.com.deveberte.urlshortening.repository.UrlRepository;
import br.com.deveberte.urlshortening.domain.entity.Link;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

        return link;
    }
    @Transactional(readOnly = true)
    @Cacheable(value = "urls", key = "#shortCode")
    public Link getLinkByShortCode(String shortCode) {
        return getLink(shortCode);
    }

    @Transactional
    @CacheEvict(value = "urls", key = "#shortCode")
    public Link update(String shortCode, String newUrl) {
        Link link = getLink(shortCode);

        link.setUrl(newUrl);

        return this.urlRepository.save(link);
    }
    @Transactional
    @CacheEvict(value = "urls", key = "#shortCode")
    public void delete(String shortCode){
        Link link = getLink(shortCode);
        this.urlRepository.delete(link);
    }

    @Transactional
    public void registerAccess(String shortCode){
        this.urlRepository.incrementAccesCount(shortCode);
    }

    @Transactional(readOnly = true)
    public Link getStats(String shortCode){
        return getLink(shortCode);
    }
    private Link getLink(String shortCode) {
        return this.urlRepository.findFirstByShortCode(shortCode).orElseThrow(() -> new UrlNotFoundException("URL não encontrada"));
    }
}
