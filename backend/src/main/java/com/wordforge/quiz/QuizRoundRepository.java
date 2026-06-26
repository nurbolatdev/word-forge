package com.wordforge.quiz;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface QuizRoundRepository extends JpaRepository<QuizRound, Long> {
    Optional<QuizRound> findFirstByUserIdAndFinishedAtIsNullOrderByStartedAtDesc(Long userId);
    List<QuizRound> findByUserIdOrderByStartedAtDesc(Long userId);
}
