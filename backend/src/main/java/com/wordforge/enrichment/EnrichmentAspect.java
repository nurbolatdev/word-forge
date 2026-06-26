package com.wordforge.enrichment;

import jakarta.persistence.*;

@Entity
@Table(name = "enrichment_aspects")
public class EnrichmentAspect {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long enrichmentId;

    @Column(nullable = false)
    private String aspectType;

    // Stored as JSONB; serialization handled by caller for now
    @Column(columnDefinition = "jsonb", nullable = false)
    private String content;

    protected EnrichmentAspect() {}

    public EnrichmentAspect(Long enrichmentId, String aspectType, String content) {
        this.enrichmentId = enrichmentId;
        this.aspectType = aspectType;
        this.content = content;
    }

    public Long getId() { return id; }
    public Long getEnrichmentId() { return enrichmentId; }
    public String getAspectType() { return aspectType; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}
