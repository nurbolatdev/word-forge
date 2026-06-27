package com.wordforge.enrichment;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

// Fallback: active when GroqEnrichmentService is NOT configured (no API key).
@Service
@ConditionalOnMissingBean(GroqEnrichmentService.class)
public class MockEnrichmentService implements EnrichmentService {

    // Varied sentence templates so every word gets different patterns
    private static final List<String[]> TEMPLATES = List.of(
        new String[]{"She couldn't imagine life without %s.", "Она не могла представить жизнь без «%s»."},
        new String[]{"The teacher asked us to define %s in our own words.", "Учитель попросил нас объяснить «%s» своими словами."},
        new String[]{"Learning the word %s helped me a lot.", "Изучение слова «%s» мне очень помогло."},
        new String[]{"He used %s in a sentence to practise.", "Он использовал «%s» в предложении для практики."},
        new String[]{"Without understanding %s, the text makes no sense.", "Не понимая «%s», текст лишён смысла."}
    );

    // Hardcoded CEFR by category — enough to avoid all words being "A2"
    private static final Map<String, String> CEFR_HINTS = Map.of(
        "a", "A1", "b", "A1", "c", "A2", "d", "A2",
        "e", "B1", "f", "B1", "g", "B1", "h", "B2",
        "i", "B2", "j", "B2"
    );

    @Override
    public EnrichmentResult enrich(String lemma, String sourceLanguage, String targetLanguage) {
        String first = lemma.isEmpty() ? "a" : String.valueOf(lemma.charAt(0)).toLowerCase();
        String cefr = CEFR_HINTS.getOrDefault(first, "B1");

        // Pick a template based on lemma hash so it's deterministic per word
        String[] tpl = TEMPLATES.get(Math.abs(lemma.hashCode()) % TEMPLATES.size());
        String[] tpl2 = TEMPLATES.get((Math.abs(lemma.hashCode()) + 1) % TEMPLATES.size());

        return new EnrichmentResult(
                cefr,
                buildMnemonic(lemma),
                List.of(
                        new EnrichmentResult.ExampleData(
                                tpl[0].formatted(lemma), tpl[1].formatted(lemma)),
                        new EnrichmentResult.ExampleData(
                                tpl2[0].formatted(lemma), tpl2[1].formatted(lemma))
                )
        );
    }

    private String buildMnemonic(String lemma) {
        char first = Character.toUpperCase(lemma.charAt(0));
        return ("💡 Remember \"%s\": starts with '%c' — " +
                "picture a vivid scene and say it aloud three times.").formatted(lemma, first);
    }
}
