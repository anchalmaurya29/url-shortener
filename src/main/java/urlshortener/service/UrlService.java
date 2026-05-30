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

    // Generates a unique 6-character short code
    private String generateShortCode() {
        StringBuilder code = new StringBuilder();
        String uuid = java.util.UUID.randomUUID().toString().replace("-", "");
        for (int i = 0; i < 6; i++) {
            int index = (uuid.charAt(i) % BASE62.length() + BASE62.length()) % BASE62.length();
            code.append(BASE62.charAt(index));
        }
        return code.toString();
    }

    // Creates a short URL from a long URL
    public Url createShortUrl(String longUrl) {
        String shortCode;
        // Keep generating until we get a unique code
        do {
            shortCode = generateShortCode();
        } while (urlRepository.existsByShortCode(shortCode));

        Url url = new Url();
        url.setLongUrl(longUrl);
        url.setShortCode(shortCode);

        return urlRepository.save(url);
    }

    // Gets the original URL by short code
    public Optional<Url> getByShortCode(String shortCode) {
        return urlRepository.findByShortCode(shortCode);
    }

    // Increments click count every time a short URL is visited
    public void incrementClickCount(Url url) {
        url.setClickCount(url.getClickCount() + 1);
        urlRepository.save(url);
    }
}
