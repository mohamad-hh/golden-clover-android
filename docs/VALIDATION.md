# Validation

Local Java tests (using the installed JDK compiler module) passed 31 assertions: initial balance, valid/invalid bet selection, spin debit, busy input protection, insufficient balance and auto-stop, auto toggle, actual line payouts/glow, bonus purchase and five locked cells, bounce transition, respin reset/decrement, feature end, full board, big win event, five-symbol trigger, eventual bonus completion, safe viewport bounds and inverse touch mapping for all four aspect ratios with cutout/bottom insets.

Fixed-seed 200,000-spin diagnostic: 94.720% RTP, 13.908% hit frequency, 0.195% feature frequency. This diagnostic excludes purchased features and is not a mathematical/certification guarantee.

Production assets are checksum-manifested. The CI verifier requires every independent graphic and every audio cue inside the APK with exactly matching bytes and SHA-256; legacy HTML/screenshot assets are forbidden. Native Android instrumentation checks input and actual rendered output. APK build, Android captures, final size and workflow outcome must be confirmed from Actions before completion is reported.

Visual/audio review and physical-device frame-rate profiling remain distinct from automated assertions. The emulator audio backend is disabled in CI, so successful SoundPool asset loading is checked there; audible speaker output requires a physical Android device or an emulator with an audio backend. No 60 FPS measurement has been claimed.

CI uses an AOSP API 30 emulator at 720p for four aspect ratios, while building against/targeting SDK 35. Initial API 35 Google-image runs loaded all 16 Android audio samples, but system fullscreen tutorial / Google launcher ANR dialogs intercepted test input. The harness disables the fullscreen tutorial before launch. Reducing software-renderer capture resolution preserves aspect-ratio/input verification without claiming a physical-device performance result.
