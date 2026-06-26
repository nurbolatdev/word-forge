const BASE = '/api/auth';

export interface TokenResponse {
  token: string;
  userId: number;
  email: string;
}

async function post<T>(url: string, body: unknown): Promise<T> {
  const res = await fetch(url, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  });
  if (!res.ok) {
    const text = await res.text().catch(() => '');
    throw new Error(`${res.status} ${text}`);
  }
  return res.json();
}

export const authApi = {
  register: (email: string, password: string, nativeLang?: string) =>
    post<TokenResponse>(`${BASE}/register`, { email, password, nativeLang }),

  login: (email: string, password: string) =>
    post<TokenResponse>(`${BASE}/login`, { email, password }),
};
