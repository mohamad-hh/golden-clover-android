#!/usr/bin/env bash
set -euo pipefail
mkdir -p qa
for spec in '16x9 1080x1920' '18x9 1080x2160' '19.5x9 1080x2340' '20x9 1080x2400'; do
  read -r preset size <<< "$spec"
  adb shell wm size "$size"
  adb shell wm density 320
  adb shell am force-stop com.mohamadhh.goldenclover
  adb shell am instrument -w -e preset "$preset" com.mohamadhh.goldenclover.test/com.mohamadhh.goldenclover.SmokeInstrumentation | tee "qa/$preset-test.log"
  adb logcat -d > "qa/$preset-logcat.txt"
  adb pull /sdcard/Android/data/com.mohamadhh.goldenclover/files/qa/ qa/
  grep -q 'PASS: Android' "qa/$preset-test.log"
done
adb shell wm size reset
