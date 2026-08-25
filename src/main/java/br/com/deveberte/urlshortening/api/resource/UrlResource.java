package br.com.deveberte.urlshortening.api.resource;

import br.com.deveberte.urlshortening.domain.entity.Link;
import br.com.deveberte.urlshortening.api.dto.UrlRequest;
import br.com.deveberte.urlshortening.api.dto.UrlResponse;
import br.com.deveberte.urlshortening.service.UrlService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/shorten")
@Validated
public class UrlResource {
    private final UrlService urlService;

    public UrlResource(UrlService urlService) {
        this.urlService = urlService;
    }

    @PostMapping
    public ResponseEntity<UrlResponse> create(@RequestBody @Valid UrlRequest request, UriComponentsBuilder uriBuilder){
        Link link = this.urlService.create(request.link());
        URI local = uriBuilder.path("/api/shorten/{id}").buildAndExpand(link.getId()).toUri();
        return ResponseEntity.created(local).body(UrlResponse.of(link));
    }

    @GetMapping("/{shortCode}")
    public ResponseEntity<UrlResponse> get(@PathVariable String shortCode){
        Link link = this.urlService.getLinkByShortCode(shortCode);
        return ResponseEntity.ok(UrlResponse.of(link));
    }

    @PutMapping("/{shortCode}")
    public ResponseEntity<UrlResponse> update(@PathVariable String shortCode, @RequestBody @Valid UrlRequest request){
        Link link = this.urlService.update(shortCode, request.link());
        return ResponseEntity.ok(UrlResponse.of(link));
    }

    @DeleteMapping("/{shortCode}")
    public ResponseEntity<Void> delete(@PathVariable String shortCode){
        this.urlService.delete(shortCode);
        return ResponseEntity.noContent().build();
    }
}
