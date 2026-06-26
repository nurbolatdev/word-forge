package com.wordforge.vocabulary;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/vocabulary")
@Validated
class WordController {

    private final WordService service;

    WordController(WordService service) {
        this.service = service;
    }

    @GetMapping("/words/translate")
    TranslateSuggestionsDto translate(
            @RequestParam @NotBlank @Size(max = 200) String lemma,
            @RequestParam @NotBlank @Size(min = 2, max = 5) String sourceLang,
            @RequestParam @NotBlank @Size(min = 2, max = 5) String targetLang) {
        return service.translate(lemma, sourceLang, targetLang);
    }

    @GetMapping("/audio")
    AudioDto audio(
            @RequestParam @NotBlank @Size(max = 500) String text,
            @RequestParam @NotBlank @Size(min = 2, max = 5) String lang) {
        return service.audio(text, lang);
    }
}
