package com.wordforge.translation;

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

@Service
@ConditionalOnProperty(name = "wordforge.groq.api-key")
public class GroqTranslationService implements TranslationService {

    private static final Logger log = LoggerFactory.getLogger(GroqTranslationService.class);
    private static final String GROQ_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final String MODEL = "llama-3.3-70b-versatile";

    private final RestClient http;
    private final ObjectMapper mapper;

    GroqTranslationService(@Value("${wordforge.groq.api-key}") String apiKey, ObjectMapper mapper) {
        this.mapper = mapper;
        this.http = RestClient.builder()
                .baseUrl(GROQ_URL)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Override
    public List<TranslationOption> suggest(String lemma, String sourceLanguage, String targetLanguage) {
        String lang = "RU".equalsIgnoreCase(targetLanguage) ? "Russian" : targetLanguage;
        String prompt = """
                Translate the English word "%s" into %s.
                Return ONLY this JSON with no extra text:
                {"translations":["primary translation","alternative meaning 2","alternative meaning 3"]}
                Rules:
                - Include 2 to 4 translations covering different meanings or usage contexts.
                - If the word has only one common meaning, return just 1 translation.
                - Each translation must be a single word or very short phrase.
                - Return valid JSON only, no markdown, no explanation.
                """.formatted(lemma, lang);
        try {
            var body = Map.of(
                    "model", MODEL,
                    "temperature", 0.3,
                    "max_tokens", 100,
                    "messages", List.of(
                            Map.of("role", "system", "content",
                                    "You are a bilingual dictionary. Respond with valid JSON only."),
                            Map.of("role", "user", "content", prompt)
                    )
            );

            String raw = http.post()
                    .body(body)
                    .retrieve()
                    .body(GroqResponse.class)
                    .choices().get(0).message().content();

            String json = raw.replaceAll("(?s)```json\\s*|```\\s*", "").strip();
            json = json.replaceAll(",\\s*([}\\]])", "$1");

            var node = mapper.readTree(json);
            var arr = node.path("translations");
            if (arr.isMissingNode() || !arr.isArray() || arr.isEmpty()) {
                return fallback(lemma);
            }

            List<TranslationOption> result = new java.util.ArrayList<>();
            for (var item : arr) {
                String text = item.asText("").strip();
                if (!text.isEmpty()) result.add(new TranslationOption(text, "ai"));
            }
            return result.isEmpty() ? fallback(lemma) : result;

        } catch (Exception e) {
            log.warn("Groq translation failed for '{}': {} — falling back to mock", lemma, e.getMessage());
            return fallback(lemma);
        }
    }

    private List<TranslationOption> fallback(String lemma) {
        return List.of(new TranslationOption("(" + lemma + ")", "ai"));
    }

    record GroqResponse(List<Choice> choices) {
        record Choice(Message message) {}
        record Message(String content) {}
    }
}
