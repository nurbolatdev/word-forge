package com.wordforge.quiz;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.OffsetDateTime;

@Entity
@Table(name = "quiz_rounds")
public class QuizRound {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(name = "card_ids", columnDefinition = "bigint[]")
    @JdbcTypeCode(SqlTypes.ARRAY)
    private Long[] cardIds = new Long[0];

    @Column(nullable = false, updatable = false)
    private OffsetDateTime startedAt = OffsetDateTime.now();

    private OffsetDateTime finishedAt;

    @Column(nullable = false)
    private String modality = "MCQ";

    @Column(nullable = false)
    private String direction = "EN_RU";

    protected QuizRound() {}

    public QuizRound(Long userId, Long[] cardIds, String modality, String direction) {
        this.userId = userId;
        this.cardIds = cardIds;
        this.modality = modality;
        this.direction = direction;
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public Long[] getCardIds() { return cardIds; }
    public void setCardIds(Long[] cardIds) { this.cardIds = cardIds; }
    public OffsetDateTime getStartedAt() { return startedAt; }
    public OffsetDateTime getFinishedAt() { return finishedAt; }
    public void setFinishedAt(OffsetDateTime finishedAt) { this.finishedAt = finishedAt; }
    public String getModality() { return modality; }
    public String getDirection() { return direction; }
}
