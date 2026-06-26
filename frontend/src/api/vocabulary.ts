import { api } from './client';

export interface TranslationOption {
  id: number;
  text: string;
  provider: string;
}

export interface TranslateSuggestions {
  wordId: number;
  lemma: string;
  sourceLang: string;
  targetLang: string;
  suggestions: TranslationOption[];
}

export interface AudioResponse {
  url: string | null;
  useWebSpeech: boolean;
}

export const vocabularyApi = {
  translate: (lemma: string, sourceLang: string, targetLang: string) =>
    api.get<TranslateSuggestions>(
      `/api/vocabulary/words/translate?lemma=${encodeURIComponent(lemma)}&sourceLang=${sourceLang}&targetLang=${targetLang}`
    ),

  audio: (text: string, lang: string) =>
    api.get<AudioResponse>(`/api/vocabulary/audio?text=${encodeURIComponent(text)}&lang=${lang}`),
};
