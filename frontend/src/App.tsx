const modules = [
  'identity',
  'lists',
  'vocabulary',
  'translation',
  'enrichment',
  'scheduler',
  'quiz',
  'analytics',
];

export function App() {
  return (
    <main className="shell">
      <section className="hero-card">
        <p className="eyebrow">Stage 0 · skeleton</p>
        <h1>WordForge</h1>
        <p className="lead">
          Scientific memory engine inside, warm playful vocabulary game outside.
        </p>

        <div className="status-grid" aria-label="Stage 0 modules">
          {modules.map((moduleName) => (
            <span className="module-pill" key={moduleName}>
              {moduleName}
            </span>
          ))}
        </div>

        <a className="health-link" href="/health">
          Check backend health
        </a>
      </section>
    </main>
  );
}
