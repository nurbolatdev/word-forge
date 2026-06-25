package com.wordforge.enrichment;

import jakarta.persistence.*;

@Entity
@Table(name = "examples")
public class WordExample {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long enrichmentId;

    @Column(nullable = false)
    private String text;

    private String translation;

    private Short difficulty;

    protected WordExample() {}

    public WordExample(Long enrichmentId, String text, String translation, Short difficulty) {
        this.enrichmentId = enrichmentId;
        this.text = text;
        this.translation = translation;
        this.difficulty = difficulty;
    }

    public Long getId() { return id; }
    public Long getEnrichmentId() { return enrichmentId; }
    public String getText() { return text; }
    public String getTranslation() { return translation; }
    public Short getDifficulty() { return difficulty; }
}
