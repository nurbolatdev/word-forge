package com.wordforge.vocabulary;

import jakarta.persistence.*;

@Entity
@Table(name = "audio_assets")
public class AudioAsset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String text;

    @Column(nullable = false)
    private String lang;

    // tts provider name or "webspeech"
    @Column(nullable = false)
    private String provider;

    // NULL = synthesise on client via Web Speech API (free fallback, spec R11)
    private String url;

    protected AudioAsset() {}

    public AudioAsset(String text, String lang, String provider, String url) {
        this.text = text;
        this.lang = lang;
        this.provider = provider;
        this.url = url;
    }

    public Long getId() { return id; }
    public String getText() { return text; }
    public String getLang() { return lang; }
    public String getProvider() { return provider; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
}
