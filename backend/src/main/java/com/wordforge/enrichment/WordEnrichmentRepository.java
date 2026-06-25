package com.wordforge.enrichment;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface WordEnrichmentRepository extends JpaRepository<WordEnrichment, Long> {
    Optional<WordEnrichment> findByWordIdAndTargetLangAndCefrLevel(
            Long wordId, String targetLang, String cefrLevel);
}
