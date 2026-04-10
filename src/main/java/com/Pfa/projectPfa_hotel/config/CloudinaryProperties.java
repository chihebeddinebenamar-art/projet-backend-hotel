package com.Pfa.projectPfa_hotel.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "cloudinary")
public class CloudinaryProperties {
    /** Cloudinary cloud name (dashboard). */
    private String cloudName;
    /** API key. */
    private String apiKey;
    /** API secret — ne jamais committer en clair, utiliser une variable d’environnement. */
    private String apiSecret;
}
