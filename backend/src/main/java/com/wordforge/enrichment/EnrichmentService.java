package com.wordforge.enrichment;

public interface EnrichmentService {
    EnrichmentResult enrich(String lemma, String sourceLanguage, String targetLanguage);
}
