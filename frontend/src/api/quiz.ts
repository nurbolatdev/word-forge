import { api } from './client';

export interface QuizRound {
  id: number;
  userId: number;
  cardIds: number[];
  totalCards: number;
  answeredCards: number;
  finished: boolean;
  startedAt: string;
  finishedAt: string | null;
}

export interface QuizOption {
  translationId: number;
  text: string;
}

export interface QuizQuestion {
  cardId: number;
  lemma: string;
  questionIndex: number;
  totalCards: number;
  options: QuizOption[];
}

export interface AnswerResult {
  cardId: number;
  correct: boolean;
  grade: number;
  correctTranslationId: number;
  correctTranslationText: string;
  roundFinished: boolean;
  nextQuestion: QuizQuestion | null;
}

export const quizApi = {
  startRound: (cardIds: number[]) =>
    api.post<QuizRound>('/api/quiz/rounds', { cardIds }),

  getQuestion: (roundId: number) =>
    api.get<QuizQuestion>(`/api/quiz/rounds/${roundId}/question`),

  submitAnswer: (roundId: number, body: {
    cardId: number; chosenTranslationId: number; responseTimeMs: number;
  }) => api.post<AnswerResult>(`/api/quiz/rounds/${roundId}/answer`, body),
};
