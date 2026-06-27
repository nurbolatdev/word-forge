import { useEffect, useState } from 'react';
import { analyticsApi, ForecastDay, Stats } from '../api/analytics';

interface Props {
  onBack: () => void;
}

const GOAL_OPTIONS = [5, 10, 20, 30, 50];

export function StatsPage({ onBack }: Props) {
  const [stats, setStats] = useState<Stats | null>(null);
  const [forecast, setForecast] = useState<ForecastDay[]>([]);
  const [error, setError] = useState('');
  const [savingGoal, setSavingGoal] = useState(false);

  useEffect(() => {
    Promise.all([analyticsApi.getStats(), analyticsApi.getForecast()])
      .then(([s, f]) => { setStats(s); setForecast(f); })
      .catch((e: unknown) => setError(e instanceof Error ? e.message : 'Failed to load'));
  }, []);

  async function changeGoal(goal: number) {
    if (!stats) return;
    setSavingGoal(true);
    try {
      await analyticsApi.updateGoal(goal);
      setStats({ ...stats, dailyGoal: goal });
    } finally {
      setSavingGoal(false);
    }
  }

  const maxCount = forecast.length > 0 ? Math.max(...forecast.map(d => d.count), 1) : 1;

  function fmtDate(iso: string) {
    const d = new Date(iso);
    return d.toLocaleDateString('en', { month: 'short', day: 'numeric' });
  }

  const todayProgress = stats ? Math.min(stats.reviewedToday / stats.dailyGoal, 1) : 0;

  return (
    <div className="page">
      <header className="page-header">
        <button className="btn-ghost" onClick={onBack}>← Back</button>
        <h1>My Progress</h1>
      </header>

      {error && <p className="error-msg">{error}</p>}
      {!stats && !error && <p className="empty-state">Loading…</p>}

      {stats && (
        <>
          {/* Daily goal */}
          <section className="stats-section">
            <h2 className="stats-section-title">Daily goal</h2>
            <div className="goal-row">
              {GOAL_OPTIONS.map(g => (
                <button
                  key={g}
                  className={`goal-btn ${stats.dailyGoal === g ? 'goal-btn--active' : ''}`}
                  onClick={() => changeGoal(g)}
                  disabled={savingGoal}
                >{g}</button>
              ))}
            </div>
            <div className="progress-bar-wrap">
              <div
                className="progress-bar-fill"
                style={{ width: `${todayProgress * 100}%` }}
              />
            </div>
            <p className="progress-label">
              {stats.reviewedToday} / {stats.dailyGoal} reviewed today
            </p>
          </section>

          {/* Key stats */}
          <section className="stats-section">
            <h2 className="stats-section-title">Overview</h2>
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
                <span className="stat-value">{stats.totalReviews}</span>
                <span className="stat-label">Total reviews</span>
              </div>
            </div>
          </section>

          {/* Forecast chart */}
          <section className="stats-section">
            <h2 className="stats-section-title">Upcoming reviews — next 14 days</h2>
            {forecast.length === 0
              ? <p className="empty-state">No cards scheduled yet.</p>
              : (
                <div className="forecast-chart">
                  {forecast.map(day => (
                    <div key={day.date} className="forecast-bar-col">
                      <span className="forecast-count">{day.count}</span>
                      <div
                        className="forecast-bar"
                        style={{ height: `${Math.max((day.count / maxCount) * 120, 4)}px` }}
                      />
                      <span className="forecast-date">{fmtDate(day.date)}</span>
                    </div>
                  ))}
                </div>
              )
            }
          </section>
        </>
      )}
    </div>
  );
}
