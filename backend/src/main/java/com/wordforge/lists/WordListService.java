package com.wordforge.lists;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
class WordListService {

    private final WordListRepository listRepo;
    private final UserCardRepository cardRepo;

    WordListService(WordListRepository listRepo, UserCardRepository cardRepo) {
        this.listRepo = listRepo;
        this.cardRepo = cardRepo;
    }

    List<WordListDto> getAll(Long userId) {
        return listRepo.findByUserId(userId).stream()
                .map(list -> {
                    int count = cardRepo.findByUserIdAndListId(userId, list.getId()).size();
                    return WordListDto.from(list, count);
                })
                .toList();
    }

    @Transactional
    WordListDto create(Long userId, WordListController.CreateListRequest req) {
        WordList list = new WordList(userId, req.title(), req.sourceLang(), req.targetLang());
        listRepo.save(list);
        return WordListDto.from(list, 0);
    }

    @Transactional
    void delete(Long id, Long userId) {
        WordList list = listRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!list.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        listRepo.delete(list);
    }
}
