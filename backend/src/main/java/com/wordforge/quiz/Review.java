package com.wordforge.quiz;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "reviews")
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long cardId;

    @Column(nullable = false)
    private Long userId;

    private String taskType;

    @Column(nullable = false)
    private String modality = "TEXT";

    private Short grade;

    private Boolean correct;

    private Integer responseTimeMs;

    @Column(name = "is_benchmark", nullable = false)
    private boolean benchmark = false;

    private Long roundId;

    @Column(nullable = false, updatable = false)
    private OffsetDateTime reviewedAt = OffsetDateTime.now();

    protected Review() {}

    public Review(Long cardId, Long userId, Long roundId, String taskType, String modality,
                  Short grade, Boolean correct, Integer responseTimeMs, boolean benchmark) {
        this.cardId = cardId;
        this.userId = userId;
        this.roundId = roundId;
        this.taskType = taskType;
        this.modality = modality;
        this.grade = grade;
        this.correct = correct;
        this.responseTimeMs = responseTimeMs;
        this.benchmark = benchmark;
    }

    public Long getId() { return id; }
    public Long getCardId() { return cardId; }
    public Long getUserId() { return userId; }
    public Long getRoundId() { return roundId; }
    public String getTaskType() { return taskType; }
    public String getModality() { return modality; }
    public Short getGrade() { return grade; }
    public Boolean getCorrect() { return correct; }
    public Integer getResponseTimeMs() { return responseTimeMs; }
    public boolean isBenchmark() { return benchmark; }
    public OffsetDateTime getReviewedAt() { return reviewedAt; }
}
