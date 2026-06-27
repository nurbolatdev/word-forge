import { useEffect, useRef, useState } from 'react';
import { Card, listsApi, WordList } from '../api/lists';
import { TranslateSuggestions, vocabularyApi } from '../api/vocabulary';
import { AudioButton } from '../components/AudioButton';
import { EnrichmentPanel } from '../components/EnrichmentPanel';
import { TranslationPicker } from '../components/TranslationPicker';

function FlipCard({
  card, sourceLang, targetLang, onRemove,
}: {
  card: Card; sourceLang: string; targetLang: string; onRemove: (id: number) => void;
}) {
  const [flipped, setFlipped] = useState(false);
  return (
    <li className="flip-card-wrapper">
      <div
        className={`flip-card${flipped ? ' flip-card--flipped' : ''}`}
        onClick={() => setFlipped((f) => !f)}
      >
        {/* Front */}
        <div className="flip-card-face flip-card-front">
          <div className="flip-front-content">
            <div className="flip-front-word">
              <span className="card-lemma">{card.lemma}</span>
              {/* stop propagation so audio doesn't flip the card */}
              <span onClick={(e) => e.stopPropagation()}>
                <AudioButton text={card.lemma} lang={sourceLang} />
              </span>
            </div>
            <div className="flip-front-controls" onClick={(e) => e.stopPropagation()}>
              <span className="card-status">{card.status}</span>
              {card.wordId > 0 && (
                <EnrichmentPanel
                  wordId={card.wordId}
                  lemma={card.lemma}
                  sourceLang={sourceLang}
                  targetLang={targetLang}
                />
              )}
              <button
                className="btn-ghost card-remove card-remove--visible"
                onClick={(e) => { e.stopPropagation(); onRemove(card.id); }}
                title="Remove word"
              >✕</button>
            </div>
          </div>
        </div>

        {/* Back */}
        <div className="flip-card-face flip-card-back">
          <span className="flip-hint-label">{sourceLang} → {targetLang}</span>
          {card.translations.length > 0 ? (
            <ul className="flip-translations">
              {card.translations.map((t, i) => (
                <li key={i} className="flip-translation-item">
                  <AudioButton text={t} lang={targetLang} />
                  <span>{t}</span>
                </li>
              ))}
            </ul>
          ) : (
            <p className="flip-no-translation">No translation selected yet</p>
          )}
          <span className="flip-tap-hint">tap to flip back</span>
        </div>
      </div>
    </li>
  );
}

interface Props {
  list: WordList;
  onBack: () => void;
}

interface AddState {
  phase: 'idle' | 'loading' | 'picking';
  lemma: string;
  suggestions: TranslateSuggestions | null;
  selectedIds: number[];
}

export function ListPage({ list, onBack }: Props) {
  const [cards, setCards] = useState<Card[]>([]);
  const [addState, setAddState] = useState<AddState>({
    phase: 'idle', lemma: '', suggestions: null, selectedIds: [],
  });
  const [error, setError] = useState('');
  const [importMsg, setImportMsg] = useState('');
  const fileInputRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    listsApi.getCards(list.id).then(setCards).catch(console.error);
  }, [list.id]);

  async function lookupTranslations() {
    const lemma = addState.lemma.trim();
    if (!lemma) return;
    setError('');
    setAddState((s) => ({ ...s, phase: 'loading' }));
    try {
      const suggestions = await vocabularyApi.translate(lemma, list.sourceLang, list.targetLang);
      setAddState((s) => ({ ...s, phase: 'picking', suggestions, selectedIds: [] }));
    } catch (e: unknown) {
      setError(e instanceof Error ? e.message : 'Error');
      setAddState((s) => ({ ...s, phase: 'idle' }));
    }
  }

  async function addWord() {
    if (!addState.suggestions || addState.selectedIds.length === 0) return;
    setError('');
    try {
      const card = await listsApi.addWord(list.id, {
        wordId: addState.suggestions.wordId,
        lemma: addState.suggestions.lemma,
      });
      const saved = await listsApi.selectTranslations(list.id, card.id, addState.selectedIds);
      setCards((prev) => [...prev, saved]);
      setAddState({ phase: 'idle', lemma: '', suggestions: null, selectedIds: [] });
    } catch (e: unknown) {
      setError(e instanceof Error ? e.message : 'Error');
    }
  }

  async function removeCard(cardId: number) {
    await listsApi.removeCard(list.id, cardId);
    setCards((prev) => prev.filter((c) => c.id !== cardId));
  }

  async function handleCsvImport(e: React.ChangeEvent<HTMLInputElement>) {
    const file = e.target.files?.[0];
    if (!file) return;
    setImportMsg('Importing…');
    setError('');
    try {
      const result = await listsApi.importCsv(list.id, file);
      setImportMsg(`Imported ${result.imported} words, skipped ${result.skipped}`);
      const refreshed = await listsApi.getCards(list.id);
      setCards(refreshed);
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : 'Import failed');
      setImportMsg('');
    } finally {
      if (fileInputRef.current) fileInputRef.current.value = '';
    }
  }

  return (
    <div className="page">
      <header className="page-header">
        <button className="btn-ghost" onClick={onBack}>← Back</button>
        <h1>{list.title}</h1>
        <span className="lang-badge">{list.sourceLang} → {list.targetLang}</span>
        <button className="btn-ghost" onClick={() => fileInputRef.current?.click()}>
          ↑ Import CSV
        </button>
        <input
          ref={fileInputRef}
          type="file"
          accept=".csv,text/csv"
          style={{ display: 'none' }}
          onChange={handleCsvImport}
        />
      </header>
      {importMsg && <p className="import-msg">{importMsg}</p>}

      <div className="add-words-section">
        <div className="add-words-row">
          <input
            className="text-input"
            placeholder="Type a word…"
            value={addState.lemma}
            disabled={addState.phase === 'loading'}
            onChange={(e) => setAddState((s) => ({ ...s, lemma: e.target.value }))}
            onKeyDown={(e) => e.key === 'Enter' && addState.phase === 'idle' && lookupTranslations()}
          />
          {addState.phase === 'idle' && (
            <button className="btn-primary" onClick={lookupTranslations}>
              Find translations
            </button>
          )}
          {addState.phase === 'loading' && <span className="spinner">…</span>}
        </div>

        {addState.phase === 'picking' && addState.suggestions && (
          <div className="translation-panel">
            <div className="translation-header">
              <strong>{addState.suggestions.lemma}</strong>
              <AudioButton text={addState.suggestions.lemma} lang={list.sourceLang} />
              <span className="hint">Pick one or more translations:</span>
            </div>
            <TranslationPicker
              options={addState.suggestions.suggestions}
              selected={addState.selectedIds}
              onChange={(ids) => setAddState((s) => ({ ...s, selectedIds: ids }))}
              lang={list.targetLang}
            />
            <div className="translation-actions">
              <button
                className="btn-primary"
                onClick={addWord}
                disabled={addState.selectedIds.length === 0}
              >
                Add to list
              </button>
              <button
                className="btn-ghost"
                onClick={() => setAddState({ phase: 'idle', lemma: '', suggestions: null, selectedIds: [] })}
              >
                Cancel
              </button>
            </div>
          </div>
        )}

        {error && <p className="error-msg">{error}</p>}
      </div>

      {cards.length === 0 && (
        <p className="empty-state">No words yet. Add your first word above.</p>
      )}

      <ul className="card-list">
        {cards.map((card) => (
          <FlipCard
            key={card.id}
            card={card}
            sourceLang={list.sourceLang}
            targetLang={list.targetLang}
            onRemove={removeCard}
          />
        ))}
      </ul>
    </div>
  );
}
