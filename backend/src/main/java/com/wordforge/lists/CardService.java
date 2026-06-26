package com.wordforge.lists;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
class CardService {

    private final WordListRepository listRepo;
    private final UserCardRepository cardRepo;

    CardService(WordListRepository listRepo, UserCardRepository cardRepo) {
        this.listRepo = listRepo;
        this.cardRepo = cardRepo;
    }

    List<CardDto> getCards(Long listId, Long userId) {
        return cardRepo.findByUserIdAndListId(userId, listId).stream()
                .map(CardDto::from)
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
        return CardDto.from(card);
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
        return CardDto.from(card);
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
