import { api } from './client';

export interface Stats {
  totalWords: number;
  dueToday: number;
  reviewedToday: number;
  streak: number;
  totalReviews: number;
  dailyGoal: number;
}

export interface ForecastDay {
  date: string;
  count: number;
}

export const analyticsApi = {
  getStats: () => api.get<Stats>('/api/analytics/stats'),
  getForecast: () => api.get<ForecastDay[]>('/api/analytics/forecast'),
  updateGoal: (dailyGoal: number) => api.patch<void>('/api/analytics/goal', { dailyGoal }),
};
