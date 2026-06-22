#!/usr/bin/env sh
set -eu

GRADLE_VERSION="8.14.3"
GRADLE_USER_HOME="${GRADLE_USER_HOME:-"$PWD/.gradle"}"
GRADLE_DIR="$GRADLE_USER_HOME/bootstrap/gradle-$GRADLE_VERSION"
GRADLE_ZIP="$GRADLE_USER_HOME/bootstrap/gradle-$GRADLE_VERSION-bin.zip"
GRADLE_URL="https://services.gradle.org/distributions/gradle-$GRADLE_VERSION-bin.zip"

if [ ! -x "$GRADLE_DIR/bin/gradle" ]; then
  mkdir -p "$GRADLE_USER_HOME/bootstrap"
  if [ ! -f "$GRADLE_ZIP" ]; then
    if command -v curl >/dev/null 2>&1; then
      curl -L "$GRADLE_URL" -o "$GRADLE_ZIP"
    elif command -v wget >/dev/null 2>&1; then
      wget "$GRADLE_URL" -O "$GRADLE_ZIP"
    else
      echo "curl or wget is required to bootstrap Gradle $GRADLE_VERSION" >&2
      exit 1
    fi
  fi
  unzip -q "$GRADLE_ZIP" -d "$GRADLE_USER_HOME/bootstrap"
fi

exec "$GRADLE_DIR/bin/gradle" "$@"
