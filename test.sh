#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
mkdir -p out
find src test -name '*.java' | sort > sources.txt
javac --release 17 -encoding UTF-8 -Xlint:all -d out @sources.txt
java -cp out library.LibraryTests
