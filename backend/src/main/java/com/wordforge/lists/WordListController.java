package com.wordforge.lists;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lists")
class WordListController {

    private final WordListService service;

    WordListController(WordListService service) {
        this.service = service;
    }

    @GetMapping
    List<WordListDto> getAll(@RequestParam Long userId) {
        return service.getAll(userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    WordListDto create(@RequestParam Long userId, @Valid @RequestBody CreateListRequest req) {
        return service.create(userId, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void delete(@PathVariable Long id, @RequestParam Long userId) {
        service.delete(id, userId);
    }

    record CreateListRequest(
            @NotBlank @Size(max = 100) String title,
            @NotBlank @Size(min = 2, max = 5) String sourceLang,
            @NotBlank @Size(min = 2, max = 5) String targetLang
    ) {}
}
