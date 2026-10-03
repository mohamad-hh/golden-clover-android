#!/usr/bin/env bash
set -euo pipefail
mkdir -p qa
adb shell settings put secure immersive_mode_confirmations confirmed
for spec in '16x9 720x1280' '18x9 720x1440' '19.5x9 720x1560' '20x9 720x1600'; do
  read -r preset size <<< "$spec"
  adb shell wm size "$size"
  adb shell wm density 240
  adb shell am force-stop com.mohamadhh.goldenclover
  adb shell am instrument -w -e preset "$preset" com.mohamadhh.goldenclover.test/com.mohamadhh.goldenclover.SmokeInstrumentation | tee "qa/$preset-test.log"
  adb logcat -d > "qa/$preset-logcat.txt"
  adb pull /sdcard/Android/data/com.mohamadhh.goldenclover/files/qa/ qa/
  grep -q 'PASS: Android' "qa/$preset-test.log"
done
adb shell wm size reset
