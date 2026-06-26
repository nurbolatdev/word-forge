package com.wordforge.enrichment;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface EnrichmentAspectRepository extends JpaRepository<EnrichmentAspect, Long> {
    List<EnrichmentAspect> findByEnrichmentId(Long enrichmentId);
    Optional<EnrichmentAspect> findByEnrichmentIdAndAspectType(Long enrichmentId, String aspectType);
}
