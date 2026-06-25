package com.wordforge.scheduler;

import org.springframework.data.jpa.repository.JpaRepository;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface CardMemoryStateRepository extends JpaRepository<CardMemoryState, Long> {

    // Hot path: due-words query — uses idx_cms_user_due (user_id, next_due_at)
    List<CardMemoryState> findByUserIdAndNextDueAtLessThanEqualOrderByNextDueAtAsc(
            Long userId, OffsetDateTime cutoff);

    Optional<CardMemoryState> findByCardIdAndAspectScope(Long cardId, String aspectScope);
}
