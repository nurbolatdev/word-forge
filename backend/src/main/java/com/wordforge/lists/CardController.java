package com.wordforge.lists;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lists/{listId}/cards")
class CardController {

    private final CardService service;

    CardController(CardService service) {
        this.service = service;
    }

    @GetMapping
    List<CardDto> getCards(@PathVariable Long listId, @RequestAttribute Long userId) {
        return service.getCards(listId, userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    CardDto addWord(@PathVariable Long listId,
                   @RequestAttribute Long userId,
                   @Valid @RequestBody AddWordRequest req) {
        return service.addWord(listId, userId, req);
    }

    @PatchMapping("/{cardId}/translations")
    CardDto selectTranslations(@PathVariable Long listId,
                               @PathVariable Long cardId,
                               @RequestAttribute Long userId,
                               @Valid @RequestBody SelectTranslationsRequest req) {
        return service.selectTranslations(cardId, userId, req.translationIds());
    }

    @DeleteMapping("/{cardId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void removeCard(@PathVariable Long listId,
                    @PathVariable Long cardId,
                    @RequestAttribute Long userId) {
        service.removeCard(cardId, userId);
    }

    record AddWordRequest(
            @NotNull Long wordId,
            @NotBlank @Size(max = 200) String lemma
    ) {}

    record SelectTranslationsRequest(
            @NotNull @Size(min = 1) List<Long> translationIds
    ) {}
}
