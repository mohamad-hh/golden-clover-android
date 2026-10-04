# Clover Inferno Demo — Android

An offline, native Android demo inspired by APEX Clover Inferno. English interface, portrait layout, original fire-and-clover artwork and original casino sound effects. Uses virtual demo dollars only.

Every launch or return from the background starts a fresh **$2,000** session. No balance is restored from an earlier session. Sound preference is retained.

## Play

- Tap **SPIN**. Match symbols from the left on **25 paylines**; BAR substitutes for regular symbols.
- Tap **BET** to select $1 / $2 / $5 / $10 / $20 / $35 / $50 / $75 / $100. Default bet is $5.
- Five clovers trigger a feature. The colors present determine which features are combined.
- **BLAZE** grows one selected held clover every respin.
- **JACKPOT** opens two 5×3 boards. Three tokens of the same kind pay GRAND / MAJOR / MAXI / MINOR / MINI once per feature.
- **BOMB** pays clovers in a random 2×2 area with a ×1 / ×2 / ×3 multiplier. Held values remain on the board.
- New clovers reset the counter to three respins. A full board doubles held values; collected Bomb payouts and jackpot awards are not doubled.
- **DEMO** previews BLAZE, JACKPOT, BOMB or all three together for free, without deducting credit.
- **AUTO** continues until turned off or credit is insufficient. **RULES** explains play and resets the session to $2,000. **SOUND** toggles effects.

The bonus previews are an added convenience for this demo, not a claimed feature of the original cabinet. Tree progress bars are a visual collection indicator; they do not guarantee a trigger.

## Mathematics

This is a custom demo model, not the original APEX probability table. `ReelLogic`, `PayoutLogic` and `BonusState` contain the adjustable weights and payouts.

A fixed-seed **400,000-spin** diagnostic observed **96.010% total RTP**, **43.481% hit frequency** and **1.305% feature frequency** (about one in 77 spins). The approximate sampling interval for RTP was ±1.123 percentage points. This diagnostic excludes free previews and uses $10,000 / $500 progressive snapshots. It is neither a per-session guarantee nor certified casino mathematics.

GRAND starts at $10,000 and MAJOR at $500. Paid spins contribute 0.5% / 0.2% of the bet to their meters; feature awards use the displayed amount captured at feature entry. MAXI / MINOR / MINI pay 100× / 25× / 10× the selected bet. Progressive meters reset on a matching payout or a new session. There is no shared/network jackpot.

## Build

Standard build: JDK 17+, Gradle 8.9, Android SDK platform/build-tools 35.

```sh
./tools/test_logic.sh
gradle :app:assembleDebug :app:assembleDebugAndroidTest
python3 tools/verify_apk.py app/build/outputs/apk/debug/app-debug.apk
```

A local build without Gradle is also supported using standard Android build tools:

```sh
export TASK_KEYSTORE=/path/to/private/development.keystore
# Set TASK_KEYSTORE_PASSWORD locally; never put it in repository files.
TASK_SDK_ROOT=/path/to/android-sdk ./tools/build_native.sh
```

For the minimal builder, SDK directories are `platforms/android-35` and `build-tools/android-15` (the latter is the extraction directory in Google's build-tools 35 ZIP). Set `TASK_KEYSTORE` and `TASK_KEYSTORE_PASSWORD` locally before the minimal build. The key is created if absent, using the environment password; existing keys must use the androiddebugkey alias. Signing keys and passwords are never committed.

GitHub Actions builds the app, checks gameplay, verifies packaged asset checksums and runs Android input/rendering instrumentation at four portrait aspect ratios. Download **Clover-Inferno-Demo-APK** from the successful workflow run.

## Artwork, audio and references

The red background and three jeweled clover trees are original generated artwork. Fruit, bell, clover, coin and celebration assets are original independent textures. BAR and plum are drawn natively. All 16 packaged sound effects are original bell, reel, coin and fanfare sounds; no audio was extracted from the commercial game.

The original screenshot in repository history is never included in the APK. Artwork is similar in theme, not an official APEX asset pack. This app is an independent demo and is not an official APEX product.

Research references:

- [APEX Clover Link Elements](https://www.apex-gaming.com/products/clover-link-elements/)
- [APEX brochure, Inferno features on page 3](https://www.apex-gaming.com/wp-content/uploads/CL-Elements.pdf)
- [Novomatic Spain Clover Inferno](https://novomatic-spain.com/salones-bingos/clover-link-elements/mix-clover-link-elements/clover-inferno)
- [Blaze gameplay video](https://www.youtube.com/watch?v=V9iNKmFaIo8)
- [Bomb gameplay video](https://www.youtube.com/shorts/gZzYRZ-aqdM)
- [Combined bonus gameplay video](https://www.youtube.com/watch?v=qbM98nq9cY8)

The video links were discovered through search; their playback was unavailable in the research environment. Exact commercial respin rules, paytable and probability weights were not available. See [validation](docs/VALIDATION.md) for what was actually tested.
