package urlshortener.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import urlshortener.model.Url;
import urlshortener.service.UrlService;

import java.net.URI;
import java.util.Map;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UrlController {

    private final UrlService urlService;

    @PostMapping("/api/shorten")
    public ResponseEntity<?> shortenUrl(@RequestBody @Valid ShortenRequest request) {
        try {
            Url url;
            if (request.customAlias() != null && !request.customAlias().isBlank()) {
                url = urlService.createShortUrlWithAlias(request.longUrl(), request.customAlias().trim());
            } else {
                url = urlService.createShortUrl(request.longUrl());
            }

            return ResponseEntity.ok(Map.of(
                    "shortCode", url.getShortCode(),
                    "shortUrl", "http://localhost:8080/" + url.getShortCode(),
                    "longUrl", url.getLongUrl(),
                    "expiresAt", url.getExpiresAt()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{shortCode:[a-zA-Z0-9-_]{4,20}}")
    public ResponseEntity<?> redirect(@PathVariable String shortCode) {
        Optional<Url> url = urlService.getByShortCode(shortCode);
        if (url.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Short URL not found"));
        }
        urlService.incrementClickCount(url.get());
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(url.get().getLongUrl()))
                .build();
    }

    @GetMapping("/api/info/{shortCode}")
    public ResponseEntity<?> getInfo(@PathVariable String shortCode) {
        Optional<Url> url = urlService.getByShortCode(shortCode);
        if (url.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Short URL not found"));
        }
        Url u = url.get();
        return ResponseEntity.ok(Map.of(
                "shortCode", u.getShortCode(),
                "longUrl", u.getLongUrl(),
                "clickCount", u.getClickCount(),
                "createdAt", u.getCreatedAt(),
                "expiresAt", u.getExpiresAt()
        ));
    }

    record ShortenRequest(
            @NotBlank(message = "URL cannot be empty")
            @Pattern(regexp = "^https?://.*", message = "URL must start with http:// or https://")
            String longUrl,
            String customAlias
    ) {}
}