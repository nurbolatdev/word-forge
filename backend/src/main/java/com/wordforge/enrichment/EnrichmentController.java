package com.wordforge.enrichment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/enrichment")
@Validated
class EnrichmentController {

    private final WordEnrichmentService service;

    EnrichmentController(WordEnrichmentService service) {
        this.service = service;
    }

    @GetMapping("/{wordId}")
    EnrichmentDto get(
            @PathVariable Long wordId,
            @RequestParam @NotBlank @Size(min = 2, max = 5) String targetLang) {
        return service.get(wordId, targetLang);
    }

    @PostMapping
    EnrichmentDto enrich(
            @RequestParam @NotNull Long wordId,
            @RequestParam @NotBlank @Size(max = 200) String lemma,
            @RequestParam @NotBlank @Size(min = 2, max = 5) String sourceLang,
            @RequestParam @NotBlank @Size(min = 2, max = 5) String targetLang) {
        return service.enrich(wordId, lemma, sourceLang, targetLang);
    }
}
