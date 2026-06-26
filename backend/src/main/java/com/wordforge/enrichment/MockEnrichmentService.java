package com.wordforge.enrichment;

import java.util.List;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("dev")
public class MockEnrichmentService implements EnrichmentService {
    @Override
    public EnrichmentResult enrich(String lemma, String sourceLanguage, String targetLanguage) {
        return new EnrichmentResult(
                "A2",
                List.of(
                        new EnrichmentResult.ExampleData(
                                "I use the word %s every day.".formatted(lemma),
                                "Я использую слово «%s» каждый день.".formatted(lemma)),
                        new EnrichmentResult.ExampleData(
                                "Can you explain what %s means?".formatted(lemma),
                                "Можешь объяснить, что значит «%s»?".formatted(lemma))
                )
        );
    }
}
