import { api } from './client';

export interface Stats {
  totalWords: number;
  dueToday: number;
  reviewedToday: number;
  streak: number;
  totalReviews: number;
}

export const analyticsApi = {
  getStats: () => api.get<Stats>('/api/analytics/stats'),
};
