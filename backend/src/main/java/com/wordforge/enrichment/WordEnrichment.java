package com.wordforge.enrichment;

import jakarta.persistence.*;

@Entity
@Table(name = "word_enrichments")
public class WordEnrichment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long wordId;

    private String targetLang;
    private String cefrLevel;
    private String source;
    private String mnemonic;

    @Column(nullable = false)
    private int version = 1;

    protected WordEnrichment() {}

    public WordEnrichment(Long wordId, String targetLang, String cefrLevel, String source) {
        this.wordId = wordId;
        this.targetLang = targetLang;
        this.cefrLevel = cefrLevel;
        this.source = source;
    }

    public Long getId() { return id; }
    public Long getWordId() { return wordId; }
    public String getTargetLang() { return targetLang; }
    public String getCefrLevel() { return cefrLevel; }
    public String getSource() { return source; }
    public String getMnemonic() { return mnemonic; }
    public void setMnemonic(String mnemonic) { this.mnemonic = mnemonic; }
    public int getVersion() { return version; }
}
