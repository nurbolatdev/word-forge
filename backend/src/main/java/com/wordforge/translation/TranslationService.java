package com.wordforge.translation;

import java.util.List;

public interface TranslationService {
    List<TranslationOption> suggest(String lemma, String sourceLanguage, String targetLanguage);
}
