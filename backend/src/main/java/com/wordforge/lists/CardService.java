package com.wordforge.lists;

import com.wordforge.vocabulary.WordTranslationRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;

@Service
class CardService {

    private final WordListRepository listRepo;
    private final UserCardRepository cardRepo;
    private final WordTranslationRepository translationRepo;

    CardService(WordListRepository listRepo, UserCardRepository cardRepo,
                WordTranslationRepository translationRepo) {
        this.listRepo = listRepo;
        this.cardRepo = cardRepo;
        this.translationRepo = translationRepo;
    }

    List<CardDto> getCards(Long listId, Long userId) {
        return cardRepo.findByUserIdAndListId(userId, listId).stream()
                .map(card -> CardDto.from(card, resolveTranslations(card)))
                .toList();
    }

    private List<String> resolveTranslations(UserCard card) {
        if (card.getChosenTranslationIds() == null || card.getChosenTranslationIds().length == 0) {
            return List.of();
        }
        return Arrays.stream(card.getChosenTranslationIds())
                .map(id -> translationRepo.findById(id)
                        .map(t -> t.getText())
                        .orElse(null))
                .filter(t -> t != null)
                .toList();
    }

    @Transactional
    CardDto addWord(Long listId, Long userId, CardController.AddWordRequest req) {
        WordList list = listRepo.findById(listId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!list.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        boolean alreadyAdded = cardRepo.findByUserIdAndListId(userId, listId).stream()
                .anyMatch(c -> c.getLemma().equalsIgnoreCase(req.lemma()));
        if (alreadyAdded) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Word already in list");
        }

        UserCard card = new UserCard(userId, listId, req.wordId(), req.lemma().toLowerCase().strip());
        cardRepo.save(card);
        return CardDto.from(card, List.of());
    }

    @Transactional
    CardDto selectTranslations(Long cardId, Long userId, List<Long> translationIds) {
        UserCard card = cardRepo.findById(cardId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!card.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        card.setChosenTranslationIds(translationIds.toArray(Long[]::new));
        cardRepo.save(card);
        return CardDto.from(card, resolveTranslations(card));
    }

    @Transactional
    void removeCard(Long cardId, Long userId) {
        UserCard card = cardRepo.findById(cardId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!card.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        cardRepo.delete(card);
    }
}
