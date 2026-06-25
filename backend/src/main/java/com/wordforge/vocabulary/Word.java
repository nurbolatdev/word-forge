package com.wordforge.vocabulary;

import jakarta.persistence.*;

@Entity
@Table(name = "words")
public class Word {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String lemma;

    @Column(nullable = false)
    private String lang;

    private String partOfSpeech;

    private Integer frequencyRank;

    protected Word() {}

    public Word(String lemma, String lang) {
        this.lemma = lemma;
        this.lang = lang;
    }

    public Long getId() { return id; }
    public String getLemma() { return lemma; }
    public String getLang() { return lang; }
    public String getPartOfSpeech() { return partOfSpeech; }
    public void setPartOfSpeech(String partOfSpeech) { this.partOfSpeech = partOfSpeech; }
    public Integer getFrequencyRank() { return frequencyRank; }
    public void setFrequencyRank(Integer frequencyRank) { this.frequencyRank = frequencyRank; }
}
