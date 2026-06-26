package com.wordforge.lists;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface WordListRepository extends JpaRepository<WordList, Long> {
    List<WordList> findByUserId(Long userId);
}
