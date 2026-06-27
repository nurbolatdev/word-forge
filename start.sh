#!/bin/bash
# WordForge dev startup script
set -e

# Load environment variables from .env.local if present
if [ -f "$(dirname "$0")/.env.local" ]; then
  export $(grep -v '^#' "$(dirname "$0")/.env.local" | xargs)
fi

ROOT="$(cd "$(dirname "$0")" && pwd)"

echo "Starting WordForge backend..."
cd "$ROOT"
./gradlew :backend:bootRun &
BACKEND_PID=$!

echo "Starting WordForge frontend..."
cd "$ROOT/frontend"
npm run dev &
FRONTEND_PID=$!

echo ""
echo "Backend : http://localhost:8080"
echo "Frontend: http://localhost:5173"
echo ""
echo "Press Ctrl+C to stop both"

trap "kill $BACKEND_PID $FRONTEND_PID 2>/dev/null; exit" INT TERM
wait
