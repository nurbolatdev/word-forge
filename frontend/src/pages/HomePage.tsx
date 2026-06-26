import { useEffect, useState } from 'react';
import { listsApi, WordList } from '../api/lists';

interface Props {
  onSelectList: (list: WordList) => void;
  onStartQuiz: () => void;
}

export function HomePage({ onSelectList, onStartQuiz }: Props) {
  const [lists, setLists] = useState<WordList[]>([]);
  const [creating, setCreating] = useState(false);
  const [title, setTitle] = useState('');
  const [error, setError] = useState('');

  useEffect(() => {
    listsApi.getAll().then(setLists).catch(console.error);
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

  return (
    <div className="page">
      <header className="page-header">
        <h1>My Lists</h1>
        <button className="btn-accent" onClick={onStartQuiz}>Practice ▶</button>
        <button className="btn-primary" onClick={() => setCreating(true)}>+ New list</button>
      </header>

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
