import { useEffect, useState } from 'react';
import { analyticsApi, Stats } from '../api/analytics';
import { listsApi, WordList } from '../api/lists';

interface Props {
  onSelectList: (list: WordList) => void;
  onStartQuiz: () => void;
  onShowStats: () => void;
}

export function HomePage({ onSelectList, onStartQuiz, onShowStats }: Props) {
  const [lists, setLists] = useState<WordList[]>([]);
  const [stats, setStats] = useState<Stats | null>(null);
  const [creating, setCreating] = useState(false);
  const [title, setTitle] = useState('');
  const [error, setError] = useState('');

  useEffect(() => {
    listsApi.getAll().then(setLists).catch(console.error);
    analyticsApi.getStats().then(setStats).catch(console.error);
  }, []);

  async function createList() {
    if (!title.trim()) return;
    setError('');
    try {
      const list = await listsApi.create({
        title: title.trim(),
        sourceLang: 'EN',
        targetLang: 'RU',
      });
      setLists((prev) => [...prev, list]);
      setTitle('');
      setCreating(false);
    } catch (e: unknown) {
      setError(e instanceof Error ? e.message : 'Error');
    }
  }

  async function deleteList(id: number) {
    await listsApi.delete(id);
    setLists((prev) => prev.filter((l) => l.id !== id));
  }

  const progress = stats ? Math.min(stats.reviewedToday / stats.dailyGoal, 1) : 0;

  return (
    <div className="page">
      <header className="page-header">
        <h1>My Lists</h1>
        <button className="btn-ghost" onClick={onShowStats}>Stats</button>
        <button className="btn-accent" onClick={onStartQuiz}>Practice ▶</button>
        <button className="btn-primary" onClick={() => setCreating(true)}>+ New list</button>
      </header>

      {stats && (
        <div className="home-goal-bar" onClick={onShowStats} title="View progress">
          <div className="home-goal-meta">
            <span>{stats.streak > 0 ? `🔥 ${stats.streak}-day streak` : 'Start your streak today'}</span>
            <span>{stats.reviewedToday} / {stats.dailyGoal} today</span>
          </div>
          <div className="progress-bar-wrap">
            <div className="progress-bar-fill" style={{ width: `${progress * 100}%` }} />
          </div>
        </div>
      )}

      {creating && (
        <div className="create-list-form">
          <input
            className="text-input"
            placeholder="List name (e.g. Travel)"
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            onKeyDown={(e) => e.key === 'Enter' && createList()}
            autoFocus
          />
          <button className="btn-primary" onClick={createList}>Create</button>
          <button className="btn-ghost" onClick={() => setCreating(false)}>Cancel</button>
          {error && <p className="error-msg">{error}</p>}
        </div>
      )}

      {lists.length === 0 && !creating && (
        <p className="empty-state">No lists yet. Create your first one!</p>
      )}

      <ul className="list-grid">
        {lists.map((list) => (
          <li key={list.id} className="list-card" onClick={() => onSelectList(list)}>
            <div className="list-card-title">{list.title}</div>
            <div className="list-card-meta">
              {list.sourceLang} → {list.targetLang} · {list.wordCount} words
            </div>
            <button
              className="btn-ghost list-card-delete"
              onClick={(e) => { e.stopPropagation(); deleteList(list.id); }}
            >
              ✕
            </button>
          </li>
        ))}
      </ul>
    </div>
  );
}
