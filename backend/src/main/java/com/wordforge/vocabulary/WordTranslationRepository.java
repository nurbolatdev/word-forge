package com.wordforge.vocabulary;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface WordTranslationRepository extends JpaRepository<WordTranslation, Long> {
    List<WordTranslation> findByWordIdAndTargetLang(Long wordId, String targetLang);
    Optional<WordTranslation> findByWordIdAndTargetLangAndTextAndProvider(
            Long wordId, String targetLang, String text, String provider);
}
