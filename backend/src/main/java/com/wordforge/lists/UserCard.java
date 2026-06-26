package com.wordforge.lists;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.OffsetDateTime;

@Entity
@Table(name = "user_cards")
public class UserCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long listId;

    // Cross-module reference to vocabulary.Word – kept as plain ID (ArchUnit boundary)
    @Column(nullable = false)
    private Long wordId;

    // Denormalized for display without cross-module JOIN (see V3 migration)
    @Column(nullable = false)
    private String lemma = "";

    @Column(name = "chosen_translation_ids", columnDefinition = "bigint[]")
    @JdbcTypeCode(SqlTypes.ARRAY)
    private Long[] chosenTranslationIds = new Long[0];

    @Column(nullable = false)
    private double emotionalSalience = 0.0;

    @Column(nullable = false)
    private String status = "PENDING_ENRICHMENT";

    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    protected UserCard() {}

    public UserCard(Long userId, Long listId, Long wordId, String lemma) {
        this.userId = userId;
        this.listId = listId;
        this.wordId = wordId;
        this.lemma = lemma;
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public Long getListId() { return listId; }
    public Long getWordId() { return wordId; }
    public String getLemma() { return lemma; }
    public Long[] getChosenTranslationIds() { return chosenTranslationIds; }
    public void setChosenTranslationIds(Long[] chosenTranslationIds) { this.chosenTranslationIds = chosenTranslationIds; }
    public double getEmotionalSalience() { return emotionalSalience; }
    public void setEmotionalSalience(double emotionalSalience) { this.emotionalSalience = emotionalSalience; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
