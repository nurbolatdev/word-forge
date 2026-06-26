package com.wordforge.vocabulary;

import jakarta.persistence.*;

@Entity
@Table(name = "word_translations")
public class WordTranslation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long wordId;

    @Column(nullable = false)
    private String targetLang;

    @Column(nullable = false)
    private String text;

    // google | yandex | llm | manual
    @Column(nullable = false)
    private String provider;

    protected WordTranslation() {}

    public WordTranslation(Long wordId, String targetLang, String text, String provider) {
        this.wordId = wordId;
        this.targetLang = targetLang;
        this.text = text;
        this.provider = provider;
    }

    public Long getId() { return id; }
    public Long getWordId() { return wordId; }
    public String getTargetLang() { return targetLang; }
    public String getText() { return text; }
    public String getProvider() { return provider; }
}
