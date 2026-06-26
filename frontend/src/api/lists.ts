import { api } from './client';

export interface WordList {
  id: number;
  userId: number;
  title: string;
  sourceLang: string;
  targetLang: string;
  wordCount: number;
  createdAt: string;
}

export interface Card {
  id: number;
  listId: number;
  userId: number;
  wordId: number;
  lemma: string;
  chosenTranslationIds: number[];
  status: string;
  createdAt: string;
}

export const listsApi = {
  getAll: () =>
    api.get<WordList[]>('/api/lists'),

  create: (body: { title: string; sourceLang: string; targetLang: string }) =>
    api.post<WordList>('/api/lists', body),

  delete: (listId: number) =>
    api.delete(`/api/lists/${listId}`),

  getCards: (listId: number) =>
    api.get<Card[]>(`/api/lists/${listId}/cards`),

  addWord: (listId: number, body: { wordId: number; lemma: string }) =>
    api.post<Card>(`/api/lists/${listId}/cards`, body),

  selectTranslations: (listId: number, cardId: number, translationIds: number[]) =>
    api.patch<Card>(`/api/lists/${listId}/cards/${cardId}/translations`, { translationIds }),

  removeCard: (listId: number, cardId: number) =>
    api.delete(`/api/lists/${listId}/cards/${cardId}`),
};
