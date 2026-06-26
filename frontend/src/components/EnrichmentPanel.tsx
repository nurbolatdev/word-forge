import { useState } from 'react';
import { Enrichment, enrichmentApi } from '../api/enrichment';

interface Props {
  wordId: number;
  lemma: string;
  sourceLang: string;
  targetLang: string;
}

export function EnrichmentPanel({ wordId, lemma, sourceLang, targetLang }: Props) {
  const [data, setData] = useState<Enrichment | null>(null);
  const [loading, setLoading] = useState(false);
  const [expanded, setExpanded] = useState(false);

  async function load() {
    setLoading(true);
    try {
      const result = await enrichmentApi.enrich(wordId, lemma, sourceLang, targetLang);
      setData(result);
      setExpanded(true);
    } finally {
      setLoading(false);
    }
  }

  if (!expanded) {
    return (
      <button className="btn-ghost enrich-btn" onClick={load} disabled={loading}>
        {loading ? '…' : '✦ Enrich'}
      </button>
    );
  }

  return (
    <div className="enrichment-panel">
      <div className="enrichment-header">
        <span className="cefr-badge">{data?.cefrLevel ?? '…'}</span>
        <button className="btn-ghost enrich-close" onClick={() => setExpanded(false)}>✕</button>
      </div>
      {data && data.examples.length > 0 && (
        <ul className="example-list">
          {data.examples.map((ex) => (
            <li key={ex.id} className="example-item">
              <p className="example-text">{ex.text}</p>
              <p className="example-translation">{ex.translation}</p>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
