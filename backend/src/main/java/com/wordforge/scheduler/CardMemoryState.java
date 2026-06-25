package com.wordforge.scheduler;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "card_memory_states")
public class CardMemoryState {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Cross-module reference to lists.UserCard – kept as plain ID (ArchUnit boundary)
    @Column(nullable = false)
    private Long cardId;

    // Denormalized for fast due-query without join (spec Part III note)
    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String aspectScope = "ALL";

    private Double stability;

    // Reusing field name 'fsrsDifficulty' to avoid clash with JPA reserved word
    @Column(name = "difficulty")
    private Double fsrsDifficulty;

    private OffsetDateTime lastReviewAt;

    @Column(nullable = false)
    private OffsetDateTime nextDueAt;

    @Column(nullable = false)
    private int reps = 0;

    @Column(nullable = false)
    private int fsrsParamsVersion = 1;

    protected CardMemoryState() {}

    public CardMemoryState(Long cardId, Long userId, OffsetDateTime nextDueAt) {
        this.cardId = cardId;
        this.userId = userId;
        this.nextDueAt = nextDueAt;
    }

    public Long getId() { return id; }
    public Long getCardId() { return cardId; }
    public Long getUserId() { return userId; }
    public String getAspectScope() { return aspectScope; }
    public Double getStability() { return stability; }
    public void setStability(Double stability) { this.stability = stability; }
    public Double getFsrsDifficulty() { return fsrsDifficulty; }
    public void setFsrsDifficulty(Double fsrsDifficulty) { this.fsrsDifficulty = fsrsDifficulty; }
    public OffsetDateTime getLastReviewAt() { return lastReviewAt; }
    public void setLastReviewAt(OffsetDateTime lastReviewAt) { this.lastReviewAt = lastReviewAt; }
    public OffsetDateTime getNextDueAt() { return nextDueAt; }
    public void setNextDueAt(OffsetDateTime nextDueAt) { this.nextDueAt = nextDueAt; }
    public int getReps() { return reps; }
    public void setReps(int reps) { this.reps = reps; }
    public int getFsrsParamsVersion() { return fsrsParamsVersion; }
    public void setFsrsParamsVersion(int fsrsParamsVersion) { this.fsrsParamsVersion = fsrsParamsVersion; }
}
