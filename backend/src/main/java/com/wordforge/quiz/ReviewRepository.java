package com.wordforge.quiz;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByCardIdOrderByReviewedAtDesc(Long cardId);
    List<Review> findByUserIdAndBenchmarkTrue(Long userId);
    List<Review> findByRoundId(Long roundId);
    boolean existsByRoundIdAndCardId(Long roundId, Long cardId);
}
