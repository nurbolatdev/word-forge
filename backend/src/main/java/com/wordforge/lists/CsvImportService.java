package com.wordforge.lists;

import com.wordforge.vocabulary.Word;
import com.wordforge.vocabulary.WordRepository;
import com.wordforge.vocabulary.WordTranslation;
import com.wordforge.vocabulary.WordTranslationRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@Service
class CsvImportService {

    private final WordListRepository listRepo;
    private final UserCardRepository cardRepo;
    private final WordRepository wordRepo;
    private final WordTranslationRepository translationRepo;

    CsvImportService(WordListRepository listRepo, UserCardRepository cardRepo,
                     WordRepository wordRepo, WordTranslationRepository translationRepo) {
        this.listRepo = listRepo;
        this.cardRepo = cardRepo;
        this.wordRepo = wordRepo;
        this.translationRepo = translationRepo;
    }

    record ImportResult(int imported, int skipped) {}

    @Transactional
    ImportResult importCsv(Long listId, Long userId, MultipartFile file) {
        WordList list = listRepo.findById(listId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!list.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        int imported = 0;
        int skipped = 0;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            String line;
            while ((line = reader.readLine()) != null) {
                line = line.strip();
                if (line.isEmpty() || line.startsWith("#")) continue;

                String[] parts = line.split(",", 2);
                if (parts.length < 2) { skipped++; continue; }

                String lemma = parts[0].strip().toLowerCase();
                String translationText = parts[1].strip();
                if (lemma.isEmpty() || translationText.isEmpty()) { skipped++; continue; }

                boolean alreadyInList = cardRepo.findByUserIdAndListId(userId, listId).stream()
                        .anyMatch(c -> c.getLemma().equalsIgnoreCase(lemma));
                if (alreadyInList) { skipped++; continue; }

                Word word = wordRepo.findByLemmaAndLang(lemma, list.getSourceLang())
                        .orElseGet(() -> wordRepo.save(new Word(lemma, list.getSourceLang())));

                WordTranslation translation = translationRepo
                        .findByWordIdAndTargetLangAndTextAndProvider(
                                word.getId(), list.getTargetLang(), translationText, "manual")
                        .orElseGet(() -> translationRepo.save(
                                new WordTranslation(word.getId(), list.getTargetLang(),
                                        translationText, "manual")));

                UserCard card = new UserCard(userId, listId, word.getId(), lemma);
                card.setChosenTranslationIds(new Long[]{translation.getId()});
                cardRepo.save(card);
                imported++;
            }
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid CSV: " + e.getMessage());
        }

        return new ImportResult(imported, skipped);
    }
}
