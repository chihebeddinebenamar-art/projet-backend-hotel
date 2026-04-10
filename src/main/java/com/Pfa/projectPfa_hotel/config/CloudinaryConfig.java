package com.Pfa.projectPfa_hotel.config;

import com.cloudinary.Cloudinary;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Le bean {@link Cloudinary} n’est créé que si la clé et le secret sont non vides
 * (ex. via {@code CLOUDINARY_API_KEY} / {@code CLOUDINARY_API_SECRET}), pour permettre
 * le démarrage de l’application sans Cloudinary en local.
 */
@Configuration
@EnableConfigurationProperties(CloudinaryProperties.class)
public class CloudinaryConfig {

    @Bean
    @ConditionalOnExpression(
            "'${cloudinary.api-key:}'.length() > 0 && '${cloudinary.api-secret:}'.length() > 0")
    public Cloudinary cloudinary(CloudinaryProperties properties) {
        if (!StringUtils.hasText(properties.getCloudName())) {
            throw new IllegalStateException(
                    "cloudinary.cloud-name est vide (ex. CLOUDINARY_CLOUD_NAME ou valeur par défaut dans application.properties).");
        }
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", properties.getCloudName());
        config.put("api_key", properties.getApiKey());
        config.put("api_secret", properties.getApiSecret());
        return new Cloudinary(config);
    }
}
