package com.wordforge.vocabulary;

import com.wordforge.common.tts.SpeechAsset;
import com.wordforge.common.tts.TextToSpeechService;
import com.wordforge.translation.TranslationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
class WordService {

    private final WordRepository wordRepo;
    private final WordTranslationRepository translationRepo;
    private final AudioAssetRepository audioRepo;
    private final TranslationService translationService;
    private final TextToSpeechService ttsService;

    WordService(WordRepository wordRepo,
                WordTranslationRepository translationRepo,
                AudioAssetRepository audioRepo,
                TranslationService translationService,
                TextToSpeechService ttsService) {
        this.wordRepo = wordRepo;
        this.translationRepo = translationRepo;
        this.audioRepo = audioRepo;
        this.translationService = translationService;
        this.ttsService = ttsService;
    }

    @Transactional
    TranslateSuggestionsDto translate(String lemma, String sourceLang, String targetLang) {
        String normalizedLemma = lemma.toLowerCase().strip();

        Word word = wordRepo.findByLemmaAndLang(normalizedLemma, sourceLang.toUpperCase())
                .orElseGet(() -> wordRepo.save(new Word(normalizedLemma, sourceLang.toUpperCase())));

        List<TranslateSuggestionsDto.TranslationOptionDto> suggestions =
                translationRepo.findByWordIdAndTargetLang(word.getId(), targetLang.toUpperCase())
                        .stream()
                        .map(t -> new TranslateSuggestionsDto.TranslationOptionDto(t.getId(), t.getText(), t.getProvider()))
                        .toList();

        if (suggestions.isEmpty()) {
            suggestions = translationService.suggest(normalizedLemma, sourceLang, targetLang)
                    .stream()
                    .map(opt -> {
                        WordTranslation saved = translationRepo.save(
                                new WordTranslation(word.getId(), targetLang.toUpperCase(), opt.text(), opt.provider()));
                        return new TranslateSuggestionsDto.TranslationOptionDto(saved.getId(), saved.getText(), saved.getProvider());
                    })
                    .toList();
        }

        return new TranslateSuggestionsDto(word.getId(), word.getLemma(), sourceLang.toUpperCase(), targetLang.toUpperCase(), suggestions);
    }

    @Transactional
    AudioDto audio(String text, String lang) {
        String normalizedLang = lang.toUpperCase();
        Optional<AudioAsset> cached = audioRepo.findByTextAndLangAndProvider(text, normalizedLang, "tts");
        if (cached.isPresent()) {
            return new AudioDto(cached.get().getUrl(), false);
        }

        SpeechAsset asset = ttsService.synthesize(text, normalizedLang);
        if (asset.url() != null) {
            audioRepo.save(new AudioAsset(text, normalizedLang, asset.provider(), asset.url()));
            return new AudioDto(asset.url(), false);
        }
        return new AudioDto(null, true);
    }
}
