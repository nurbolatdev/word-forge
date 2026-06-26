import { useEffect, useRef, useState } from 'react';
import { listsApi } from '../api/lists';
import { AnswerResult, QuizQuestion, quizApi } from '../api/quiz';
import { AudioButton } from '../components/AudioButton';

const USER_ID = 1;

interface Props {
  onBack: () => void;
}

type Phase = 'loading' | 'empty' | 'question' | 'result' | 'done';

export function QuizPage({ onBack }: Props) {
  const [phase, setPhase] = useState<Phase>('loading');
  const [question, setQuestion] = useState<QuizQuestion | null>(null);
  const [result, setResult] = useState<AnswerResult | null>(null);
  const [roundId, setRoundId] = useState<number | null>(null);
  const [error, setError] = useState('');
  const startTime = useRef<number>(0);

  useEffect(() => { loadRound(); }, []);

  async function loadRound() {
    setPhase('loading');
    try {
      // Get all user cards across all lists to build quiz
      const allLists = await listsApi.getAll(USER_ID);
      const allCards = (await Promise.all(
        allLists.map((l) => listsApi.getCards(l.id, USER_ID))
      )).flat().filter((c) => c.chosenTranslationIds.length > 0);

      if (allCards.length === 0) { setPhase('empty'); return; }

      const cardIds = allCards.map((c) => c.id);
      const round = await quizApi.startRound(USER_ID, cardIds);
      setRoundId(round.id);
      const q = await quizApi.getQuestion(round.id, USER_ID);
      setQuestion(q);
      startTime.current = Date.now();
      setPhase('question');
    } catch (e: unknown) {
      setError(e instanceof Error ? e.message : 'Error');
      setPhase('empty');
    }
  }

  async function answer(translationId: number) {
    if (!roundId || !question) return;
    const responseTimeMs = Date.now() - startTime.current;
    try {
      const res = await quizApi.submitAnswer(roundId, USER_ID, {
        cardId: question.cardId,
        chosenTranslationId: translationId,
        responseTimeMs,
      });
      setResult(res);
      setPhase('result');
    } catch (e: unknown) {
      setError(e instanceof Error ? e.message : 'Error');
    }
  }

  function next() {
    if (!result) return;
    if (result.roundFinished) { setPhase('done'); return; }
    if (result.nextQuestion) {
      setQuestion(result.nextQuestion);
      startTime.current = Date.now();
      setResult(null);
      setPhase('question');
    }
  }

  return (
    <div className="quiz-shell">
      <header className="quiz-header">
        <button className="btn-ghost" onClick={onBack}>← Back</button>
        {question && (
          <span className="quiz-progress">
            {question.questionIndex + 1} / {question.totalCards}
          </span>
        )}
      </header>

      {phase === 'loading' && <div className="quiz-center"><span className="spinner">…</span></div>}

      {phase === 'empty' && (
        <div className="quiz-center">
          <p>No cards with translations yet.</p>
          <p>Add words to a list and pick translations first.</p>
          <button className="btn-primary" onClick={onBack}>Go to Lists</button>
        </div>
      )}

      {(phase === 'question' || phase === 'result') && question && (
        <div className="quiz-card">
          <div className={`quiz-drum ${phase === 'question' ? 'quiz-drum--enter' : ''}`}>
            <span className="quiz-lemma">{question.lemma}</span>
            <AudioButton text={question.lemma} lang="EN" />
          </div>

          <p className="quiz-prompt">Choose the correct translation:</p>

          <div className="quiz-options">
            {question.options.map((opt) => {
              let cls = 'quiz-option';
              if (phase === 'result' && result) {
                if (opt.translationId === result.correctTranslationId) cls += ' quiz-option--correct';
                else if (!result.correct && opt.translationId === result.correctTranslationId) cls += ' quiz-option--wrong';
              }
              return (
                <button
                  key={opt.translationId}
                  className={cls}
                  onClick={() => phase === 'question' && answer(opt.translationId)}
                  disabled={phase === 'result'}
                >
                  {opt.text}
                </button>
              );
            })}
          </div>

          {phase === 'result' && result && (
            <div className={`quiz-feedback ${result.correct ? 'quiz-feedback--correct' : 'quiz-feedback--wrong'}`}>
              <span>{result.correct ? '✓ Correct!' : `✗ Correct answer: ${result.correctTranslationText}`}</span>
              <span className="grade-badge">Grade {result.grade}/4</span>
              <button className="btn-primary" onClick={next}>
                {result.roundFinished ? 'Finish' : 'Next →'}
              </button>
            </div>
          )}
        </div>
      )}

      {phase === 'done' && (
        <div className="quiz-center quiz-done">
          <div className="quiz-done-icon">🎉</div>
          <h2>Round complete!</h2>
          <p>All cards reviewed for today.</p>
          <button className="btn-primary" onClick={loadRound}>New round</button>
          <button className="btn-ghost" onClick={onBack}>Back to Lists</button>
        </div>
      )}

      {error && <p className="error-msg" style={{ textAlign: 'center' }}>{error}</p>}
    </div>
  );
}
