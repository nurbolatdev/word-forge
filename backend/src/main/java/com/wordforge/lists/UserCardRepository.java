package com.wordforge.lists;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UserCardRepository extends JpaRepository<UserCard, Long> {
    List<UserCard> findByUserIdAndListId(Long userId, Long listId);
    Optional<UserCard> findByUserIdAndListIdAndWordId(Long userId, Long listId, Long wordId);
    List<UserCard> findByUserId(Long userId);
}
