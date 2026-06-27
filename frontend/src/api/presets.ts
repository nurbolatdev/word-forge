import { api } from './client';
import { WordList } from './lists';

export interface Preset {
  id: string;
  name: string;
  description: string;
  wordCount: number;
}

export const presetsApi = {
  list: () => api.get<Preset[]>('/api/presets'),
  import: (presetId: string, targetLang = 'RU') =>
    api.post<WordList>(`/api/presets/${presetId}/import?targetLang=${targetLang}`, {}),
};
