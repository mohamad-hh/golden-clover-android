# Validation

## Gameplay and mathematics

`tools/test_logic.sh` passed **233 assertions** covering initial $2,000 credit, foreground session reset, exact debit, double-tap protection, insufficient credit/auto-stop, all 25 unique paylines, BAR substitution, all seven feature combinations, free-preview accounting, one-versus-two boards, Blaze growth, three-token jackpots paid once, Bomb area payouts, full-board accounting, respin reset/decrement, feature completion, progressive snapshots and cutout-safe portrait touch mapping.

Seed 77225, 400,000 spins at $5: **96.010% RTP** (65.265% line wins + 30.745% features), **43.481% hit frequency**, **1.305% feature frequency**. Approximate 95% sampling interval ±1.123 RTP percentage points. Free previews are excluded; simulations use fixed $10,000 / $500 progressive snapshots. This is custom demo mathematics, not the original game's math or a certification.

## Android package

The local APK was built against SDK 35 with min SDK 23. APK signatures v1/v2/v3 verified successfully. The asset verifier checked all **62 graphic/audio files** against their SHA-256 manifests and confirmed that no cabinet screenshot or WebView page is packaged. The manifest has no Internet, account, payment or storage permissions.

## Android runtime

Android API 30 emulator instrumentation is used to check portrait rendering, all 16 SoundPool samples, injected touches, bet selection, spin/stop, low-credit auto-stop, free feature chooser, the three individual features and their combination, reset while a feature is running, and a fresh $2,000 after background/foreground transitions. Screenshots include base play, menus, spin, each bonus, all-features, large win, Grand and fresh session.

An API 30 runtime screenshot confirmed the portrait base screen, $2,000 credit, English labels, five reels and the three trees. The first full instrumentation run stopped at the sound-loading readiness check. Sound readiness tracking has since been changed to a synchronized set of completed sample IDs to handle early callbacks and cross-thread visibility; the emulator allowance was increased to 60 seconds. A complete instrumentation pass has not yet been claimed. Audible similarity and performance on a physical phone have not been measured.

The final signed APK installed successfully on API 30. A rerun in the software-only emulator stopped at the 30-second graphics-loading deadline while the loading screen was visible. No AndroidRuntime crash was reported. This rerun did not reach the audio or touch checks, so a full runtime pass remains unverified.
