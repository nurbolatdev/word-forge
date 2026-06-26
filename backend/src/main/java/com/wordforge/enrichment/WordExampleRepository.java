package com.wordforge.enrichment;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface WordExampleRepository extends JpaRepository<WordExample, Long> {
    List<WordExample> findByEnrichmentId(Long enrichmentId);
}
