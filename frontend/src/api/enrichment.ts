import { api } from './client';

export interface EnrichmentExample {
  id: number;
  text: string;
  translation: string;
}

export interface Enrichment {
  wordId: number;
  targetLang: string;
  cefrLevel: string;
  examples: EnrichmentExample[];
}

export const enrichmentApi = {
  get: (wordId: number, targetLang: string) =>
    api.get<Enrichment>(`/api/enrichment/${wordId}?targetLang=${targetLang}`),

  enrich: (wordId: number, lemma: string, sourceLang: string, targetLang: string) =>
    api.post<Enrichment>(
      `/api/enrichment?wordId=${wordId}&lemma=${encodeURIComponent(lemma)}&sourceLang=${sourceLang}&targetLang=${targetLang}`,
      {}
    ),
};
