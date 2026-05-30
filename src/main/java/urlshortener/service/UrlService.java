package urlshortener.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import urlshortener.model.Url;
import urlshortener.repository.UrlRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UrlService {

    private final UrlRepository urlRepository;

    private static final String BASE62 = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    private String generateShortCode() {
        StringBuilder code = new StringBuilder();
        String uuid = java.util.UUID.randomUUID().toString().replace("-", "");
        for (int i = 0; i < 6; i++) {
            int index = (uuid.charAt(i) % BASE62.length() + BASE62.length()) % BASE62.length();
            code.append(BASE62.charAt(index));
        }
        return code.toString();
    }

    // Creates short URL with auto-generated code
    public Url createShortUrl(String longUrl) {
        String shortCode;
        do {
            shortCode = generateShortCode();
        } while (urlRepository.existsByShortCode(shortCode));

        Url url = new Url();
        url.setLongUrl(longUrl);
        url.setShortCode(shortCode);
        return urlRepository.save(url);
    }

    // Creates short URL with custom alias
    public Url createShortUrlWithAlias(String longUrl, String customAlias) {
        // Validate alias
        if (!customAlias.matches("^[a-zA-Z0-9-_]{3,20}$")) {
            throw new IllegalArgumentException("Alias must be 3-20 characters and only contain letters, numbers, - or _");
        }
        if (urlRepository.existsByShortCode(customAlias)) {
            throw new IllegalArgumentException("This alias is already taken. Please choose another.");
        }

        Url url = new Url();
        url.setLongUrl(longUrl);
        url.setShortCode(customAlias);
        return urlRepository.save(url);
    }

    public Optional<Url> getByShortCode(String shortCode) {
        return urlRepository.findByShortCode(shortCode);
    }

    public void incrementClickCount(Url url) {
        url.setClickCount(url.getClickCount() + 1);
        urlRepository.save(url);
    }
}