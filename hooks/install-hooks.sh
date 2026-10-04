#!/bin/sh
# Points git at the version-controlled hooks directory, so hook updates apply without reinstalling.

ROOT_DIR=$(git rev-parse --show-toplevel)
chmod +x "$ROOT_DIR"/hooks/*
git -C "$ROOT_DIR" config core.hooksPath hooks
echo "Git hooks installed from $ROOT_DIR/hooks."
