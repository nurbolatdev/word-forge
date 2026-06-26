package com.wordforge.lists;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "word_lists")
public class WordList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String sourceLang;

    @Column(nullable = false)
    private String targetLang;

    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    protected WordList() {}

    public WordList(Long userId, String title, String sourceLang, String targetLang) {
        this.userId = userId;
        this.title = title;
        this.sourceLang = sourceLang;
        this.targetLang = targetLang;
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getSourceLang() { return sourceLang; }
    public String getTargetLang() { return targetLang; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
