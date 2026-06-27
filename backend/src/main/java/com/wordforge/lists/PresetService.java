package com.wordforge.lists;

import com.wordforge.vocabulary.WordRepository;
import com.wordforge.vocabulary.WordTranslationRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
class PresetService {

    record PresetDef(String id, String name, String description,
                     String sourceLang, Integer maxRank, String partOfSpeech) {}

    record PresetDto(String id, String name, String description, int wordCount) {}

    private static final List<PresetDef> PRESETS = List.of(
            new PresetDef("top20",  "Top 20 — Most Common",    "The 20 most frequent English words",        "EN", 20,  null),
            new PresetDef("top60",  "Top 60 — Essential EN",   "All 60 seeded high-frequency words",        "EN", 60,  null),
            new PresetDef("nouns",  "English Nouns",            "Common English nouns from frequency list",  "EN", null, "noun"),
            new PresetDef("verbs",  "English Verbs",            "Common English verbs from frequency list",  "EN", null, "verb"),
            new PresetDef("adjs",   "English Adjectives",       "Common English adjectives",                 "EN", null, "adjective")
    );

    private final WordListRepository listRepo;
    private final UserCardRepository cardRepo;
    private final WordRepository wordRepo;
    private final WordTranslationRepository translationRepo;

    PresetService(WordListRepository listRepo, UserCardRepository cardRepo,
                  WordRepository wordRepo, WordTranslationRepository translationRepo) {
        this.listRepo = listRepo;
        this.cardRepo = cardRepo;
        this.wordRepo = wordRepo;
        this.translationRepo = translationRepo;
    }

    List<PresetDto> listPresets() {
        return PRESETS.stream().map(p -> {
            int count = countWords(p);
            return new PresetDto(p.id(), p.name(), p.description(), count);
        }).toList();
    }

    @Transactional
    WordListDto importPreset(String presetId, Long userId, String targetLang) {
        PresetDef preset = PRESETS.stream()
                .filter(p -> p.id().equals(presetId))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Unknown preset"));

        WordList list = listRepo.save(
                new WordList(userId, preset.name(), preset.sourceLang(), targetLang.toUpperCase()));

        var words = resolveWords(preset);
        String tLang = targetLang.toUpperCase();

        for (var word : words) {
            boolean exists = cardRepo.findByUserIdAndListId(userId, list.getId()).stream()
                    .anyMatch(c -> c.getWordId().equals(word.getId()));
            if (exists) continue;

            UserCard card = new UserCard(userId, list.getId(), word.getId(), word.getLemma());

            translationRepo.findByWordIdAndTargetLang(word.getId(), tLang)
                    .stream().findFirst()
                    .ifPresent(t -> card.setChosenTranslationIds(new Long[]{t.getId()}));

            cardRepo.save(card);
        }

        int wordCount = cardRepo.findByUserIdAndListId(userId, list.getId()).size();
        return WordListDto.from(list, wordCount);
    }

    private int countWords(PresetDef p) {
        return resolveWords(p).size();
    }

    private List<com.wordforge.vocabulary.Word> resolveWords(PresetDef p) {
        var all = wordRepo.findByLangOrderByFrequencyRankAsc(p.sourceLang());
        return all.stream()
                .filter(w -> p.partOfSpeech() == null || p.partOfSpeech().equals(w.getPartOfSpeech()))
                .filter(w -> p.maxRank() == null || (w.getFrequencyRank() != null && w.getFrequencyRank() <= p.maxRank()))
                .toList();
    }
}
