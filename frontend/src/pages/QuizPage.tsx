import { useEffect, useRef, useState } from 'react';
import { listsApi } from '../api/lists';
import { AnswerResult, QuizDirection, QuizModality, QuizQuestion, quizApi } from '../api/quiz';
import { AudioButton } from '../components/AudioButton';

interface Props {
  onBack: () => void;
}

type Phase = 'mode-select' | 'loading' | 'empty' | 'question' | 'result' | 'done';

export function QuizPage({ onBack }: Props) {
  const [phase, setPhase] = useState<Phase>('mode-select');
  const [modality, setModality] = useState<QuizModality>('MCQ');
  const [direction, setDirection] = useState<QuizDirection>('EN_RU');
  const [question, setQuestion] = useState<QuizQuestion | null>(null);
  const [result, setResult] = useState<AnswerResult | null>(null);
  const [roundId, setRoundId] = useState<number | null>(null);
  const [typedAnswer, setTypedAnswer] = useState('');
  const [error, setError] = useState('');
  const startTime = useRef<number>(0);
  const inputRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    if (phase === 'question' && (question?.modality === 'TYPING' || question?.modality === 'CLOZE')) {
      inputRef.current?.focus();
    }
  }, [phase, question]);

  async function loadRound(selectedModality: QuizModality, selectedDirection: QuizDirection) {
    setPhase('loading');
    setError('');
    try {
      const allLists = await listsApi.getAll();
      const allCards = (await Promise.all(
        allLists.map((l) => listsApi.getCards(l.id))
      )).flat().filter((c) => c.chosenTranslationIds.length > 0);

      if (allCards.length === 0) { setPhase('empty'); return; }

      const cardIds = allCards.map((c) => c.id);
      const round = await quizApi.startRound(cardIds, selectedModality, selectedDirection);
      setRoundId(round.id);
      const q = await quizApi.getQuestion(round.id);
      setQuestion(q);
      startTime.current = Date.now();
      setTypedAnswer('');
      setPhase('question');
    } catch (e: unknown) {
      setError(e instanceof Error ? e.message : 'Error');
      setPhase('empty');
    }
  }

  async function answerMcq(translationId: number) {
    if (!roundId || !question) return;
    const responseTimeMs = Date.now() - startTime.current;
    try {
      const res = await quizApi.submitAnswer(roundId, {
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

  async function answerTyping() {
    if (!roundId || !question || !typedAnswer.trim()) return;
    const responseTimeMs = Date.now() - startTime.current;
    try {
      const res = await quizApi.submitAnswer(roundId, {
        cardId: question.cardId,
        typedAnswer: typedAnswer.trim(),
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
      setTypedAnswer('');
      setResult(null);
      setPhase('question');
    }
  }

  const promptLang = question?.direction === 'RU_EN' ? 'RU' : 'EN';
  const answerLabel = question?.direction === 'RU_EN' ? 'English word…' : 'Translation…';

  /* ── Mode select screen ── */
  if (phase === 'mode-select') {
    return (
      <div className="quiz-shell">
        <header className="quiz-header">
          <button className="btn-ghost" onClick={onBack}>← Back</button>
        </header>
        <div className="quiz-center">
          <h2 className="mode-select-title">Choose practice mode</h2>

          {/* Direction toggle */}
          <div className="direction-toggle">
            <button
              className={`direction-btn ${direction === 'EN_RU' ? 'direction-btn--active' : ''}`}
              onClick={() => setDirection('EN_RU')}
            >
              🇬🇧 EN → RU
            </button>
            <button
              className={`direction-btn ${direction === 'RU_EN' ? 'direction-btn--active' : ''}`}
              onClick={() => setDirection('RU_EN')}
            >
              🇷🇺 RU → EN
            </button>
          </div>

          <div className="mode-cards">
            <button className="mode-card" onClick={() => { setModality('MCQ'); loadRound('MCQ', direction); }}>
              <span className="mode-card-icon">☑</span>
              <span className="mode-card-name">Multiple choice</span>
              <span className="mode-card-desc">Pick the correct answer from 4 options</span>
            </button>
            <button className="mode-card" onClick={() => { setModality('TYPING'); loadRound('TYPING', direction); }}>
              <span className="mode-card-icon">⌨</span>
              <span className="mode-card-name">Typing</span>
              <span className="mode-card-desc">Type the answer from memory</span>
            </button>
            <button
              className="mode-card"
              onClick={() => { setModality('CLOZE'); loadRound('CLOZE', direction); }}
              title="Cloze is always EN→RU"
            >
              <span className="mode-card-icon">📝</span>
              <span className="mode-card-name">Cloze</span>
              <span className="mode-card-desc">Fill in the missing word in a sentence</span>
              <span className="mode-card-note">Always EN→RU</span>
            </button>
          </div>
        </div>
      </div>
    );
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
        <span className="modality-badge">
          {modality === 'TYPING' ? '⌨ Typing' : modality === 'CLOZE' ? '📝 Cloze' : '☑ MCQ'}
        </span>
        <span className="direction-badge">
          {question?.direction === 'RU_EN' ? '🇷🇺→🇬🇧' : '🇬🇧→🇷🇺'}
        </span>
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
          {/* Show word prompt for MCQ and TYPING only — CLOZE hides it intentionally */}
          {question.modality !== 'CLOZE' && (
            <div className={`quiz-drum ${phase === 'question' ? 'quiz-drum--enter' : ''}`}>
              <span className="quiz-lemma">{question.promptText}</span>
              <AudioButton text={question.promptText} lang={promptLang} />
            </div>
          )}

          {/* MCQ mode */}
          {question.modality === 'MCQ' && (
            <>
              <p className="quiz-prompt">
                {question.direction === 'RU_EN' ? 'Choose the English word:' : 'Choose the correct translation:'}
              </p>
              <div className="quiz-options">
                {question.options.map((opt) => {
                  let cls = 'quiz-option';
                  if (phase === 'result' && result) {
                    if (opt.translationId === result.correctTranslationId) cls += ' quiz-option--correct';
                    else if (!result.correct && opt.translationId !== result.correctTranslationId) cls += ' quiz-option--wrong';
                  }
                  return (
                    <button
                      key={opt.translationId}
                      className={cls}
                      onClick={() => phase === 'question' && answerMcq(opt.translationId)}
                      disabled={phase === 'result'}
                    >
                      {opt.text}
                    </button>
                  );
                })}
              </div>
            </>
          )}

          {/* Cloze mode */}
          {question.modality === 'CLOZE' && (
            <>
              <p className="quiz-prompt">Fill in the missing word:</p>
              <p className="cloze-sentence">
                {(question.clozeText ?? 'Fill in: ___').split('___').map((part, i, arr) => (
                  <span key={i}>
                    {part}
                    {i < arr.length - 1 && (
                      phase === 'question'
                        ? <span className="cloze-blank" />
                        : <strong className={result?.correct ? 'cloze-word--correct' : 'cloze-word--wrong'}>
                            {typedAnswer || '___'}
                          </strong>
                    )}
                  </span>
                ))}
              </p>
              <div className="typing-answer-row">
                <input
                  ref={inputRef}
                  className={`typing-input ${phase === 'result' ? (result?.correct ? 'typing-input--correct' : 'typing-input--wrong') : ''}`}
                  value={typedAnswer}
                  onChange={e => setTypedAnswer(e.target.value)}
                  onKeyDown={e => e.key === 'Enter' && phase === 'question' && answerTyping()}
                  disabled={phase === 'result'}
                  placeholder="Fill in the blank…"
                  autoComplete="off"
                />
                {phase === 'question' && (
                  <button className="btn-primary" onClick={answerTyping} disabled={!typedAnswer.trim()}>
                    Check
                  </button>
                )}
              </div>
              {phase === 'result' && result && !result.correct && (
                <p className="typing-correct-hint">Answer: <strong>{result.correctTranslationText}</strong></p>
              )}
            </>
          )}

          {/* Typing mode */}
          {question.modality === 'TYPING' && (
            <>
              <p className="quiz-prompt">
                {question.direction === 'RU_EN' ? 'Type the English word:' : 'Type the translation:'}
              </p>
              <div className="typing-answer-row">
                <input
                  ref={inputRef}
                  className={`typing-input ${phase === 'result' ? (result?.correct ? 'typing-input--correct' : 'typing-input--wrong') : ''}`}
                  value={typedAnswer}
                  onChange={e => setTypedAnswer(e.target.value)}
                  onKeyDown={e => e.key === 'Enter' && phase === 'question' && answerTyping()}
                  disabled={phase === 'result'}
                  placeholder={answerLabel}
                  autoComplete="off"
                />
                {phase === 'question' && (
                  <button className="btn-primary" onClick={answerTyping} disabled={!typedAnswer.trim()}>
                    Check
                  </button>
                )}
              </div>
              {phase === 'result' && result && !result.correct && (
                <p className="typing-correct-hint">Correct: <strong>{result.correctTranslationText}</strong></p>
              )}
            </>
          )}

          {phase === 'result' && result && (
            <div className={`quiz-feedback ${result.correct ? 'quiz-feedback--correct' : 'quiz-feedback--wrong'}`}>
              <span>{result.correct ? '✓ Correct!' : '✗ Wrong'}</span>
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
          <button className="btn-primary" onClick={() => setPhase('mode-select')}>New round</button>
          <button className="btn-ghost" onClick={onBack}>Back to Lists</button>
        </div>
      )}

      {error && <p className="error-msg" style={{ textAlign: 'center' }}>{error}</p>}
    </div>
  );
}
