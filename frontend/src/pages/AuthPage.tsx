import { useState } from 'react';
import { authApi } from '../api/auth';

interface Props {
  onAuth: () => void;
}

export default function AuthPage({ onAuth }: Props) {
  const [mode, setMode] = useState<'login' | 'register'>('login');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [nativeLang, setNativeLang] = useState('ru');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  async function submit(e: React.FormEvent) {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      const res = mode === 'login'
        ? await authApi.login(email, password)
        : await authApi.register(email, password, nativeLang);
      localStorage.setItem('wf_token', res.token);
      onAuth();
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : 'Something went wrong');
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="auth-page">
      <div className="auth-card">
        <h1 className="auth-title">WordForge</h1>
        <div className="auth-tabs">
          <button
            className={`auth-tab ${mode === 'login' ? 'active' : ''}`}
            onClick={() => { setMode('login'); setError(''); }}
          >Log in</button>
          <button
            className={`auth-tab ${mode === 'register' ? 'active' : ''}`}
            onClick={() => { setMode('register'); setError(''); }}
          >Sign up</button>
        </div>

        <form onSubmit={submit} className="auth-form">
          <label className="auth-label">Email
            <input
              type="email"
              className="auth-input"
              value={email}
              onChange={e => setEmail(e.target.value)}
              required
              autoFocus
            />
          </label>
          <label className="auth-label">Password
            <input
              type="password"
              className="auth-input"
              value={password}
              onChange={e => setPassword(e.target.value)}
              required
              minLength={8}
            />
          </label>
          {mode === 'register' && (
            <label className="auth-label">Native language
              <select
                className="auth-input"
                value={nativeLang}
                onChange={e => setNativeLang(e.target.value)}
              >
                <option value="ru">Russian</option>
                <option value="en">English</option>
                <option value="kk">Kazakh</option>
                <option value="de">German</option>
                <option value="fr">French</option>
                <option value="zh">Chinese</option>
              </select>
            </label>
          )}
          {error && <p className="auth-error">{error}</p>}
          <button type="submit" className="btn auth-submit" disabled={loading}>
            {loading ? '...' : mode === 'login' ? 'Log in' : 'Create account'}
          </button>
        </form>
      </div>
    </div>
  );
}
