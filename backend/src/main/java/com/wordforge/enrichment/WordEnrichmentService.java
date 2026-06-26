package com.wordforge.enrichment;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
class WordEnrichmentService {

    private final EnrichmentService enrichmentService;
    private final WordEnrichmentRepository enrichmentRepo;
    private final WordExampleRepository exampleRepo;

    WordEnrichmentService(EnrichmentService enrichmentService,
                          WordEnrichmentRepository enrichmentRepo,
                          WordExampleRepository exampleRepo) {
        this.enrichmentService = enrichmentService;
        this.enrichmentRepo = enrichmentRepo;
        this.exampleRepo = exampleRepo;
    }

    EnrichmentDto get(Long wordId, String targetLang) {
        WordEnrichment enrichment = enrichmentRepo
                .findByWordIdAndTargetLang(wordId, targetLang.toUpperCase())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No enrichment — call POST /api/enrichment first"));
        return EnrichmentDto.from(enrichment, exampleRepo.findByEnrichmentId(enrichment.getId()));
    }

    @Transactional
    EnrichmentDto enrich(Long wordId, String lemma, String sourceLang, String targetLang) {
        String tLang = targetLang.toUpperCase();

        return enrichmentRepo.findByWordIdAndTargetLang(wordId, tLang)
                .map(existing -> EnrichmentDto.from(existing,
                        exampleRepo.findByEnrichmentId(existing.getId())))
                .orElseGet(() -> {
                    EnrichmentResult result = enrichmentService.enrich(lemma, sourceLang, tLang);
                    WordEnrichment saved = enrichmentRepo.save(
                            new WordEnrichment(wordId, tLang, result.cefrLevel(), "mock-llm"));
                    List<WordExample> examples = result.examples().stream()
                            .map(ex -> exampleRepo.save(new WordExample(
                                    saved.getId(), ex.text(), ex.translation(), (short) 1)))
                            .toList();
                    return EnrichmentDto.from(saved, examples);
                });
    }
}
