package com.wordforge.translation;

import java.util.List;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("dev")
public class MockTranslationService implements TranslationService {
    @Override
    public List<TranslationOption> suggest(String lemma, String sourceLanguage, String targetLanguage) {
        return List.of(
                new TranslationOption("%s · primary".formatted(lemma), "mock"),
                new TranslationOption("%s · alternate".formatted(lemma), "mock"),
                new TranslationOption("%s · contextual".formatted(lemma), "mock")
        );
    }
}
