package com.wordforge.enrichment;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

/**
 * Real enrichment via Groq API (free tier, LLaMA 3 — open-source model by Meta).
 * Activated when wordforge.groq.api-key is set in config or WORDFORGE_GROQ_API_KEY env var.
 * Free key: https://console.groq.com/keys
 */
@Service
@ConditionalOnProperty(name = "wordforge.groq.api-key")
public class GroqEnrichmentService implements EnrichmentService {

    private static final Logger log = LoggerFactory.getLogger(GroqEnrichmentService.class);
    private static final String GROQ_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final String MODEL = "llama-3.3-70b-versatile";

    private final RestClient http;
    private final ObjectMapper mapper;

    GroqEnrichmentService(@Value("${wordforge.groq.api-key}") String apiKey,
                          ObjectMapper mapper) {
        this.mapper = mapper;
        this.http = RestClient.builder()
                .baseUrl(GROQ_URL)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Override
    public EnrichmentResult enrich(String lemma, String sourceLanguage, String targetLanguage) {
        String prompt = buildPrompt(lemma, targetLanguage);
        try {
            var body = Map.of(
                    "model", MODEL,
                    "temperature", 0.6,
                    "max_tokens", 400,
                    "messages", List.of(
                            Map.of("role", "system", "content",
                                    "You are a language learning assistant. Always respond with valid JSON only, no explanation."),
                            Map.of("role", "user", "content", prompt)
                    )
            );

            String raw = http.post()
                    .body(body)
                    .retrieve()
                    .body(GroqResponse.class)
                    .choices().get(0).message().content();

            // Strip markdown code fences and trailing commas (common LLM artifacts)
            String json = raw.replaceAll("(?s)```json\\s*|```\\s*", "").strip();
            json = json.replaceAll(",\\s*([}\\]])", "$1");
            return parseResult(json, lemma);

        } catch (Exception e) {
            log.warn("Groq enrichment failed for '{}': {} — falling back to mock", lemma, e.getMessage());
            return fallback(lemma);
        }
    }

    private String buildPrompt(String lemma, String targetLanguage) {
        String lang = "RU".equalsIgnoreCase(targetLanguage) ? "Russian" : targetLanguage;
        return """
                For the English word "%s", return ONLY this JSON with no extra text:
                {"cefrLevel":"B1","mnemonic":"short hint","sentence":"Natural sentence using %s here.","translation":"%s translation of the sentence"}
                Rules: cefrLevel is A1/A2/B1/B2/C1/C2. The sentence MUST contain the word "%s" literally. mnemonic is a short memory tip. translation is the %s translation.
                """.formatted(lemma, lemma, lang, lemma, lang);
    }

    private EnrichmentResult parseResult(String json, String lemma) throws Exception {
        var node = mapper.readTree(json);
        String cefr = node.path("cefrLevel").asText("B1");
        String mnemonic = node.path("mnemonic").asText(null);
        String sentence = node.path("sentence").asText(null);
        String translation = node.path("translation").asText("(перевод недоступен)");

        if (sentence == null || sentence.isBlank()) return fallback(lemma);

        List<EnrichmentResult.ExampleData> examples = List.of(
                new EnrichmentResult.ExampleData(sentence, translation)
        );
        return new EnrichmentResult(cefr, mnemonic, examples);
    }

    private EnrichmentResult fallback(String lemma) {
        return new EnrichmentResult("B1", null, List.of(
                new EnrichmentResult.ExampleData(
                        "She worked hard every day to improve her " + lemma + ".",
                        "(перевод недоступен)")
        ));
    }

    // ── JSON shapes ──────────────────────────────────────────────────────────

    @JsonIgnoreProperties(ignoreUnknown = true)
    record GroqResponse(List<Choice> choices) {
        record Choice(Message message) {}
        record Message(String content) {}
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record ExampleNode(String text, String translation) {}
}
