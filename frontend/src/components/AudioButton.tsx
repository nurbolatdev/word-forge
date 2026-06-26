import { useState } from 'react';
import { vocabularyApi } from '../api/vocabulary';

interface Props {
  text: string;
  lang: string;
}

export function AudioButton({ text, lang }: Props) {
  const [loading, setLoading] = useState(false);

  async function play() {
    setLoading(true);
    try {
      const res = await vocabularyApi.audio(text, lang);
      if (res.useWebSpeech || !res.url) {
        const utter = new SpeechSynthesisUtterance(text);
        utter.lang = lang.toLowerCase();
        window.speechSynthesis.speak(utter);
      } else {
        new Audio(res.url).play();
      }
    } finally {
      setLoading(false);
    }
  }

  return (
    <button className="audio-btn" onClick={play} disabled={loading} aria-label={`Play ${text}`}>
      {loading ? '…' : '🔊'}
    </button>
  );
}
