package br.com.deveberte.urlshortening.api.resource;

import br.com.deveberte.urlshortening.api.dto.PingResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/status")
public class APIStatusResource {

    @GetMapping
    @ApiResponse(responseCode = "200", description = "Application Up", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PingResponse.class)))
    public ResponseEntity<PingResponse> get(){
        return ResponseEntity.ok(new PingResponse("UP", LocalDateTime.now()));
    }
}
