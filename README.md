# WordForge

WordForge is a warm vocabulary game with a scientific spaced-repetition engine underneath.

Stage 0 contains only the project skeleton: Spring Boot backend, Vite React frontend, Docker Compose infrastructure, feature package boundaries, and mocked external service ports.

## Stack

- Java 21, Spring Boot 3, Gradle
- React, Vite, TypeScript
- PostgreSQL 16, Redis 7
- Docker Compose
- ArchUnit for modular-monolith boundaries

## Local Run

```bash
docker compose up --build
```

Then open:

- Frontend: http://localhost:5173
- Backend health: http://localhost:8080/health

## Local Development

Backend:

```bash
./gradlew :backend:bootRun
```

Frontend:

```bash
cd frontend
npm install
npm run dev
```

Checks:

```bash
./gradlew :backend:test
cd frontend && npm run build
```

## Stage 0 Scope

- Backend app skeleton with modules: `identity`, `lists`, `vocabulary`, `translation`, `enrichment`, `scheduler`, `quiz`, `analytics`, `common`
- Dev mocks for translation, TTS, and enrichment
- `/health` endpoint through Spring Boot Actuator
- Docker Compose for app, frontend, PostgreSQL, and Redis
- Vite React TypeScript frontend shell
- ArchUnit rule for feature-module isolation

## Git Workflow

Current stage branch:

```bash
stage-0-skeleton
```

Suggested atomic commits:

```bash
chore: init spring boot gradle project
chore: add docker compose infrastructure
feat: add module package structure and dev mocks
test: add archunit module isolation rule
chore: init vite react frontend
docs: add local run instructions
```
