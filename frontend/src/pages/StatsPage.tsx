import { useEffect, useState } from 'react';
import { analyticsApi, Stats } from '../api/analytics';

interface Props {
  onBack: () => void;
}

export function StatsPage({ onBack }: Props) {
  const [stats, setStats] = useState<Stats | null>(null);
  const [error, setError] = useState('');

  useEffect(() => {
    analyticsApi.getStats().then(setStats).catch((e: unknown) => {
      setError(e instanceof Error ? e.message : 'Failed to load stats');
    });
  }, []);

  return (
    <div className="page">
      <header className="page-header">
        <button className="btn-ghost" onClick={onBack}>← Back</button>
        <h1>My Progress</h1>
      </header>

      {error && <p className="error-msg">{error}</p>}

      {!stats && !error && <p className="empty-state">Loading…</p>}

      {stats && (
        <div className="stats-grid">
          <div className="stat-card stat-card--streak">
            <span className="stat-value">{stats.streak}</span>
            <span className="stat-label">Day streak</span>
          </div>
          <div className="stat-card">
            <span className="stat-value">{stats.totalWords}</span>
            <span className="stat-label">Words saved</span>
          </div>
          <div className="stat-card stat-card--due">
            <span className="stat-value">{stats.dueToday}</span>
            <span className="stat-label">Due today</span>
          </div>
          <div className="stat-card">
            <span className="stat-value">{stats.reviewedToday}</span>
            <span className="stat-label">Reviewed today</span>
          </div>
          <div className="stat-card">
            <span className="stat-value">{stats.totalReviews}</span>
            <span className="stat-label">Total reviews</span>
          </div>
        </div>
      )}
    </div>
  );
}
