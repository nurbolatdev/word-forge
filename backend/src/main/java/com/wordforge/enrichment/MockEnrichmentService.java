package com.wordforge.enrichment;

import java.util.List;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("dev")
public class MockEnrichmentService implements EnrichmentService {
    @Override
    public EnrichmentResult enrich(String lemma, String sourceLanguage, String targetLanguage) {
        String mnemonic = buildMnemonic(lemma);
        return new EnrichmentResult(
                "A2",
                mnemonic,
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

    private String buildMnemonic(String lemma) {
        // Generates a memorable association linking the word sound to its meaning.
        // Real impl would call an LLM; this is a deterministic placeholder.
        char first = Character.toUpperCase(lemma.charAt(0));
        return "💡 Remember \"%s\": the word starts with '%c' — picture a vivid scene where you say it out loud three times. The more specific the image, the stronger the memory!".formatted(lemma, first);
    }
}
