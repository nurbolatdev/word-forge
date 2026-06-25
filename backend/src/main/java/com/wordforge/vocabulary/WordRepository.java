package com.wordforge.vocabulary;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface WordRepository extends JpaRepository<Word, Long> {
    Optional<Word> findByLemmaAndLang(String lemma, String lang);
    List<Word> findByLangOrderByFrequencyRankAsc(String lang);
}
