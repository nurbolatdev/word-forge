-- ============================================================
-- WordForge baseline schema  (spec Part III)
-- ============================================================

CREATE TABLE users (
    id            BIGSERIAL    PRIMARY KEY,
    email         TEXT         NOT NULL UNIQUE,
    password_hash TEXT         NOT NULL,
    plan          TEXT         NOT NULL DEFAULT 'free',
    ui_theme      TEXT         NOT NULL DEFAULT 'light',
    native_lang   TEXT,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- Word lists: primary organisational unit for the user (spec R12)
CREATE TABLE word_lists (
    id          BIGSERIAL   PRIMARY KEY,
    user_id     BIGINT      NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title       TEXT        NOT NULL,
    source_lang TEXT        NOT NULL,
    target_lang TEXT        NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Global word catalog – shared across users to save cache budget (spec R11)
CREATE TABLE words (
    id             BIGSERIAL PRIMARY KEY,
    lemma          TEXT      NOT NULL,
    lang           TEXT      NOT NULL,
    part_of_speech TEXT,
    frequency_rank INTEGER,
    UNIQUE (lemma, lang)
);

-- Translation variants offered by translate-API (spec R9)
CREATE TABLE word_translations (
    id          BIGSERIAL PRIMARY KEY,
    word_id     BIGINT    NOT NULL REFERENCES words(id) ON DELETE CASCADE,
    target_lang TEXT      NOT NULL,
    text        TEXT      NOT NULL,
    provider    TEXT      NOT NULL,
    UNIQUE (word_id, target_lang, text, provider)
);

-- TTS audio cache; url=NULL means client-side Web Speech API (spec R11)
CREATE TABLE audio_assets (
    id       BIGSERIAL PRIMARY KEY,
    text     TEXT      NOT NULL,
    lang     TEXT      NOT NULL,
    provider TEXT      NOT NULL,
    url      TEXT,
    UNIQUE (text, lang, provider)
);

-- LLM enrichment cache: examples, collocations, nuances (spec R10)
CREATE TABLE word_enrichments (
    id          BIGSERIAL PRIMARY KEY,
    word_id     BIGINT    NOT NULL REFERENCES words(id) ON DELETE CASCADE,
    target_lang TEXT,
    cefr_level  TEXT,
    source      TEXT,
    version     INT       NOT NULL DEFAULT 1,
    UNIQUE (word_id, target_lang, cefr_level, version)
);

CREATE TABLE enrichment_aspects (
    id             BIGSERIAL PRIMARY KEY,
    enrichment_id  BIGINT    NOT NULL REFERENCES word_enrichments(id) ON DELETE CASCADE,
    aspect_type    TEXT      NOT NULL,
    content        JSONB     NOT NULL
);

CREATE TABLE examples (
    id            BIGSERIAL PRIMARY KEY,
    enrichment_id BIGINT    NOT NULL REFERENCES word_enrichments(id) ON DELETE CASCADE,
    text          TEXT      NOT NULL,
    translation   TEXT,
    difficulty    SMALLINT
);

-- Personal card: word in a user list with chosen translations (spec R9, R12)
-- chosen_translation_ids  = which word_translations the user accepted as correct answers
CREATE TABLE user_cards (
    id                      BIGSERIAL   PRIMARY KEY,
    user_id                 BIGINT      NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    list_id                 BIGINT      NOT NULL REFERENCES word_lists(id) ON DELETE CASCADE,
    word_id                 BIGINT      NOT NULL REFERENCES words(id),
    chosen_translation_ids  BIGINT[]    NOT NULL DEFAULT '{}',
    emotional_salience      REAL        NOT NULL DEFAULT 0,
    status                  TEXT        NOT NULL DEFAULT 'PENDING_ENRICHMENT',
    created_at              TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (user_id, list_id, word_id)
);

-- FSRS memory state per card (spec R4: 1:N for multi-aspect, MVP N=1)
-- user_id is denormalized for fast due-query without join (spec Part III note)
CREATE TABLE card_memory_states (
    id                  BIGSERIAL    PRIMARY KEY,
    card_id             BIGINT       NOT NULL REFERENCES user_cards(id) ON DELETE CASCADE,
    user_id             BIGINT       NOT NULL,
    aspect_scope        TEXT         NOT NULL DEFAULT 'ALL',
    stability           DOUBLE PRECISION,
    difficulty          DOUBLE PRECISION,
    last_review_at      TIMESTAMPTZ,
    next_due_at         TIMESTAMPTZ  NOT NULL,
    reps                INT          NOT NULL DEFAULT 0,
    fsrs_params_version INT          NOT NULL DEFAULT 1,
    UNIQUE (card_id, aspect_scope)
);

-- Review log: source of truth for analytics, tuning, and north-star benchmark (spec R6)
CREATE TABLE reviews (
    id               BIGSERIAL    PRIMARY KEY,
    card_id          BIGINT       NOT NULL REFERENCES user_cards(id),
    user_id          BIGINT       NOT NULL,
    task_type        TEXT,
    modality         TEXT         NOT NULL DEFAULT 'TEXT',
    grade            SMALLINT,
    correct          BOOLEAN,
    response_time_ms INT,
    is_benchmark     BOOLEAN      NOT NULL DEFAULT false,
    reviewed_at      TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- Quiz session: up to 10 cards per round (spec R13)
CREATE TABLE quiz_rounds (
    id          BIGSERIAL    PRIMARY KEY,
    user_id     BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    card_ids    BIGINT[]     NOT NULL DEFAULT '{}',
    started_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    finished_at TIMESTAMPTZ
);

-- ============================================================
-- Critical indexes
-- ============================================================

-- Due-query: primary hot path – find cards to review (spec Part III note)
CREATE INDEX idx_cms_user_due ON card_memory_states (user_id, next_due_at);

-- List membership queries
CREATE INDEX idx_user_cards_user_list ON user_cards (user_id, list_id);

-- Review history per card
CREATE INDEX idx_reviews_card ON reviews (card_id);

-- Word lookup by language
CREATE INDEX idx_words_lang ON words (lang);

-- Translation lookup
CREATE INDEX idx_word_translations_word ON word_translations (word_id);
