import { api } from './client';

export type QuizModality = 'MCQ' | 'TYPING' | 'CLOZE';
export type QuizDirection = 'EN_RU' | 'RU_EN';

export interface QuizRound {
  id: number;
  userId: number;
  cardIds: number[];
  totalCards: number;
  answeredCards: number;
  finished: boolean;
  modality: QuizModality;
  direction: QuizDirection;
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
  promptText: string;
  direction: QuizDirection;
  questionIndex: number;
  totalCards: number;
  modality: QuizModality;
  clozeText: string | null;
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
  startRound: (cardIds: number[], modality: QuizModality = 'MCQ', direction: QuizDirection = 'EN_RU') =>
    api.post<QuizRound>('/api/quiz/rounds', { cardIds, modality, direction }),

  getQuestion: (roundId: number) =>
    api.get<QuizQuestion>(`/api/quiz/rounds/${roundId}/question`),

  submitAnswer: (roundId: number, body: {
    cardId: number;
    chosenTranslationId?: number;
    typedAnswer?: string;
    responseTimeMs: number;
  }) => api.post<AnswerResult>(`/api/quiz/rounds/${roundId}/answer`, body),
};
