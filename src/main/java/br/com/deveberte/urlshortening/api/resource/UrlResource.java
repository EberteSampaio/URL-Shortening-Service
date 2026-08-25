package br.com.deveberte.urlshortening.api.resource;

import br.com.deveberte.urlshortening.config.exceptionhandler.ApiError;
import br.com.deveberte.urlshortening.domain.entity.Link;
import br.com.deveberte.urlshortening.api.dto.UrlRequest;
import br.com.deveberte.urlshortening.api.dto.UrlResponse;
import br.com.deveberte.urlshortening.service.UrlService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

    @Operation(summary = "Create a short url by url")
    @ApiResponses(value ={
            @ApiResponse(
                    responseCode = "201", description = "Short URL created",
                    content = {@Content(mediaType = "application/json", schema =  @Schema(implementation = UrlResponse.class))}
            ),
            @ApiResponse(
                    responseCode = "400", description = "Invalid URL",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))}
            )
    })
    @PostMapping
    public ResponseEntity<UrlResponse> create(@RequestBody @Valid UrlRequest request, UriComponentsBuilder uriBuilder){
        Link link = this.urlService.create(request.link());
        URI local = uriBuilder.path("/api/shorten/{id}").buildAndExpand(link.getId()).toUri();
        return ResponseEntity.created(local).body(UrlResponse.of(link));
    }


    @Operation(summary = "Get original URL by short code")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", description = "URL founded",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = UrlResponse.class))}
            ),
            @ApiResponse(
                    responseCode = "404", description = "URL not found",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))}
            ),
    })
    @GetMapping("/{shortCode}")
    public ResponseEntity<UrlResponse> get(@PathVariable @Parameter(description = "Short code from URL") String shortCode){
        Link link = this.urlService.getLinkByShortCode(shortCode);
        return ResponseEntity.ok(UrlResponse.of(link));
    }


    @Operation(summary = "Update original URL by short code")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", description = "URL updated",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = UrlResponse.class))}
            ),
            @ApiResponse(
                    responseCode = "404", description = "URL not found",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))}
            ),
    })
    @PutMapping("/{shortCode}")
    public ResponseEntity<UrlResponse> update(@PathVariable @Parameter(description = "Short code from URL") String shortCode, @RequestBody @Valid UrlRequest request){
        Link link = this.urlService.update(shortCode, request.link());
        return ResponseEntity.ok(UrlResponse.of(link));
    }

    @Operation(summary = "Update original URL by short code")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204", description = "URL was deleted"
            ),
            @ApiResponse(
                    responseCode = "404", description = "URL not found",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))}
            ),
    })
    @DeleteMapping("/{shortCode}")
    public ResponseEntity<Void> delete(@PathVariable @Parameter(description = "Short code from URL") String shortCode){
        this.urlService.delete(shortCode);
        return ResponseEntity.noContent().build();
    }
}
