package com.wordforge.common;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "wordforge.external-services")
public record ExternalServiceProperties(
        ProviderProperties translation,
        ProviderProperties tts,
        ProviderProperties enrichment
) {
    public record ProviderProperties(String provider) {
    }
}
