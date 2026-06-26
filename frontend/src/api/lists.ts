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
  getAll: (userId: number) =>
    api.get<WordList[]>(`/api/lists?userId=${userId}`),

  create: (userId: number, body: { title: string; sourceLang: string; targetLang: string }) =>
    api.post<WordList>(`/api/lists?userId=${userId}`, body),

  delete: (listId: number, userId: number) =>
    api.delete(`/api/lists/${listId}?userId=${userId}`),

  getCards: (listId: number, userId: number) =>
    api.get<Card[]>(`/api/lists/${listId}/cards?userId=${userId}`),

  addWord: (listId: number, userId: number, body: { wordId: number; lemma: string }) =>
    api.post<Card>(`/api/lists/${listId}/cards?userId=${userId}`, body),

  selectTranslations: (listId: number, cardId: number, userId: number, translationIds: number[]) =>
    api.patch<Card>(`/api/lists/${listId}/cards/${cardId}/translations?userId=${userId}`, { translationIds }),

  removeCard: (listId: number, cardId: number, userId: number) =>
    api.delete(`/api/lists/${listId}/cards/${cardId}?userId=${userId}`),
};
