import { useState } from 'react';
import { WordList } from './api/lists';
import { HomePage } from './pages/HomePage';
import { ListPage } from './pages/ListPage';

export function App() {
  const [currentList, setCurrentList] = useState<WordList | null>(null);

  if (currentList) {
    return <ListPage list={currentList} onBack={() => setCurrentList(null)} />;
  }
  return <HomePage onSelectList={setCurrentList} />;
}
