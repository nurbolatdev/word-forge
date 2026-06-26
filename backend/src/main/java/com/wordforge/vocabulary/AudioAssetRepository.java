package com.wordforge.vocabulary;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AudioAssetRepository extends JpaRepository<AudioAsset, Long> {
    Optional<AudioAsset> findByTextAndLangAndProvider(String text, String lang, String provider);
}
