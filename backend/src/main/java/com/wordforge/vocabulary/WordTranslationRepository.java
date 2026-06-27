package com.wordforge.vocabulary;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface WordTranslationRepository extends JpaRepository<WordTranslation, Long> {
    List<WordTranslation> findByWordIdAndTargetLang(Long wordId, String targetLang);
    Optional<WordTranslation> findByWordIdAndTargetLangAndTextAndProvider(
            Long wordId, String targetLang, String text, String provider);
    List<WordTranslation> findByTargetLang(String targetLang);

    // Prioritise same-CEFR translations as distractors; fallback to any when pool is small
    @Query(value = """
            SELECT wt.* FROM word_translations wt
            LEFT JOIN word_enrichments we
                   ON we.word_id = wt.word_id
            WHERE wt.target_lang = :targetLang
              AND wt.word_id    != :excludeWordId
            ORDER BY
              CASE WHEN we.cefr_level = :cefrLevel THEN 0
                   WHEN we.cefr_level IS NULL       THEN 2
                   ELSE                                  1 END,
              RANDOM()
            LIMIT 6
            """, nativeQuery = true)
    List<WordTranslation> findSmartDistractors(
            @Param("targetLang") String targetLang,
            @Param("excludeWordId") Long excludeWordId,
            @Param("cefrLevel") String cefrLevel);
}
