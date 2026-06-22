package com.wordforge.enrichment;

import java.util.List;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("dev")
public class MockEnrichmentService implements EnrichmentService {
    @Override
    public EnrichmentResult enrich(String lemma, String sourceLanguage, String targetLanguage) {
        return new EnrichmentResult("READY", List.of("Mock example for %s".formatted(lemma)));
    }
}
