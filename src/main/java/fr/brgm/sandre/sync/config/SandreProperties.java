package fr.brgm.sandre.sync.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "sandre")
public record SandreProperties(
        String baseUrl,
        int pageSize,
        Duration timeout,
        int maxInMemorySizeMb
) {
    public SandreProperties {
        if (baseUrl == null || baseUrl.isBlank()) {
            baseUrl = "https://api.sandre.eaufrance.fr/referentiels/v1";
        }
        if (pageSize <= 0) {
            pageSize = 1000;
        }
        if (timeout == null) {
            timeout = Duration.ofSeconds(60);
        }
        if (maxInMemorySizeMb <= 0) {
            maxInMemorySizeMb = 64;
        }
    }
}
