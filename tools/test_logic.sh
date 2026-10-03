#!/usr/bin/env bash
set -euo pipefail
out=$(mktemp -d)
trap 'rm -rf "$out"' EXIT
if command -v javac >/dev/null; then compiler=(javac); else compiler=(java -m jdk.compiler/com.sun.tools.javac.Main); fi
"${compiler[@]}" -d "$out" app/src/main/java/com/mohamadhh/goldenclover/game/{BetState,UIState,PayoutLogic,ReelLogic,BonusState,GameState,AnimationManager,ResponsiveLayout}.java tools/LogicTest.java
java -cp "$out" LogicTest
