# Golden Clover X — Android prototype

A native, landscape-only fantasy slot prototype using **virtual TL credits only**. No WebView, payments, deposits, withdrawals or real-money rewards.

The original uploaded Golden Clover screenshot remains in repository history as art direction. It is **never packaged into the application**. The game renders independent original WebP scenery, symbols, panels, tree layers, coins and glows, with original PCM sound effects.

## Play

Start with 1,000 virtual TL. Tap BET to choose 5 / 10 / 20 / 35 / 50 / 75 / 100 / 150 / 200. Seven paylines pay matching symbols from the left. AUTO continues after a spin and stops at insufficient credit. SFX toggles audio. The menu explains rules and can reset demo credits while idle.

Five or more Clover / Pot symbols trigger the garden. The bonus button buys a feature for 20× the selected bet in virtual credits. Clover values lock on a 5×3 board. A new clover resets three respins; an empty respin removes one. The feature awards all held values when it ends; a full board celebrates immediately. Rare fixed virtual jackpots can appear. Grand and Major meters increment decoratively; fixed feature awards use initial values.

## Architecture

Java / Android hardware-accelerated Canvas with Choreographer frame timing. GameState coordinates independent ReelLogic, PayoutLogic, BonusState, BetState and UIState. AssetLoader decodes production textures off the UI thread. AudioManager uses SoundPool with packaged original effects. AnimationManager provides easing and bounce curves. ParticleSystem uses a fixed 320-particle pool. ResponsiveLayout fits a 1920×1080 reference viewport inside cutout-safe bounds; decorative scenery covers additional width.

Normal/win symbol textures, sequential moving reels with ghost trails and landing squash, animated tree layers, bell bounce, hold-and-respin scene, coin bursts, BIG / MEGA WIN and GRAND celebrations are rendered dynamically.

## Build and verify

JDK 17, Gradle 8.9, Android SDK 35:

```sh
./tools/test_logic.sh
gradle :app:assembleDebug :app:assembleDebugAndroidTest
python3 tools/verify_apk.py app/build/outputs/apk/debug/app-debug.apk
```

GitHub Actions runs on every push to main. It builds `Golden-Clover.apk`, lists the ZIP contents, checks every production texture/audio file against SHA-256 manifests, and uploads **Golden-Clover-APK**. Real Android instrumentation tests inject touches and capture base, bet menu, spin, bell, bonus, mega and grand scenes at 16:9, 18:9, 19.5:9 and 20:9. Logs/captures are uploaded as **Golden-Clover-Verification**.

Production assets are already committed; image generation or Python imaging packages are not required to build. `tools/prepare_assets.py` extracts and losslessly encodes original authored sprite sheets; `tools/create_audio.py` regenerates original sounds. Authoring dependencies: Pillow and NumPy. See [inspection](docs/INITIAL_INSPECTION.md) and [validation notes](docs/VALIDATION.md).

## Prototype limits

The configured payout model uses seven lines, symbol weights and a payout scale. A reproducible 200,000-spin diagnostic with seed 77225 observed **94.720% RTP, 13.908% hit frequency, 0.195% bonus frequency**. This is a demo diagnostic, not certified mathematics; rare jackpots require far longer simulation. Purchased features are excluded from that diagnostic. The visible jackpot meters are decorative and not network progressives. Target 60 FPS needs physical-device profiling; emulator captures cannot prove physical-device performance or speaker quality.
