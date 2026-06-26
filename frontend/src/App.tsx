import { useState } from 'react';
import { WordList } from './api/lists';
import { HomePage } from './pages/HomePage';
import { ListPage } from './pages/ListPage';
import { QuizPage } from './pages/QuizPage';
import AuthPage from './pages/AuthPage';

type Screen = 'home' | 'list' | 'quiz';

export function App() {
  const [token, setToken] = useState<string | null>(() => localStorage.getItem('wf_token'));
  const [screen, setScreen] = useState<Screen>('home');
  const [currentList, setCurrentList] = useState<WordList | null>(null);

  if (!token) {
    return <AuthPage onAuth={() => setToken(localStorage.getItem('wf_token'))} />;
  }

  if (screen === 'quiz') {
    return <QuizPage onBack={() => setScreen('home')} />;
  }
  if (screen === 'list' && currentList) {
    return <ListPage list={currentList} onBack={() => setScreen('home')} />;
  }
  return (
    <HomePage
      onSelectList={(list) => { setCurrentList(list); setScreen('list'); }}
      onStartQuiz={() => setScreen('quiz')}
    />
  );
}
