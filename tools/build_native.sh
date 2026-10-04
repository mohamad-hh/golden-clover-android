#!/usr/bin/env bash
# Minimal reproducible local Android build; standard Gradle build remains supported.
set -euo pipefail
TASK_SDK_ROOT=${TASK_SDK_ROOT:-/workspace/android-tools}
TASK_PLATFORM_JAR="$TASK_SDK_ROOT/platforms/android-35/android.jar"
TASK_BUILD_TOOLS="$TASK_SDK_ROOT/build-tools/android-15"
TASK_OUT=app/build/native
mkdir -p "$TASK_OUT/classes" "$TASK_OUT/dex"
"$TASK_BUILD_TOOLS/aapt2" compile --dir app/src/main/res -o "$TASK_OUT/resources.zip"
python3 - "$TASK_OUT" <<'PYBUILD'
import pathlib,sys
root=pathlib.Path(sys.argv[1])
s=pathlib.Path('app/src/main/AndroidManifest.xml').read_text().replace('<manifest ', '<manifest package="com.mohamadhh.goldenclover" ').replace('<application ', '<application android:debuggable="true" ')
(root/'AndroidManifest.xml').write_text(s)
PYBUILD
"$TASK_BUILD_TOOLS/aapt2" link -o "$TASK_OUT/resources.apk" --manifest "$TASK_OUT/AndroidManifest.xml" -I "$TASK_PLATFORM_JAR" -A app/src/main/assets --min-sdk-version 23 --target-sdk-version 35 --version-code 3 --version-name 3.0 --auto-add-overlay -0 wav "$TASK_OUT/resources.zip"
rg --files app/src/main/java -g '*.java' > "$TASK_OUT/java-sources.txt"
java -m jdk.compiler/com.sun.tools.javac.Main -source 8 -target 8 -classpath "$TASK_PLATFORM_JAR" -d "$TASK_OUT/classes" @"$TASK_OUT/java-sources.txt"
java -m jdk.jartool/sun.tools.jar.Main --create --file "$TASK_OUT/classes.jar" -C "$TASK_OUT/classes" .
"$TASK_BUILD_TOOLS/d8" --lib "$TASK_PLATFORM_JAR" --min-api 23 --output "$TASK_OUT/dex" "$TASK_OUT/classes.jar"
cp "$TASK_OUT/resources.apk" "$TASK_OUT/unsigned.apk"
python3 - "$TASK_OUT" <<'PY'
import sys,zipfile,pathlib
root=pathlib.Path(sys.argv[1])
with zipfile.ZipFile(root/'unsigned.apk','a',zipfile.ZIP_DEFLATED) as z:
 for p in (root/'dex').glob('*.dex'):z.write(p,p.name)
PY
"$TASK_BUILD_TOOLS/zipalign" -f -p 4 "$TASK_OUT/unsigned.apk" "$TASK_OUT/aligned.apk"
# Development signing key is outside the repository and is never published.
: "${TASK_KEYSTORE:?Set TASK_KEYSTORE to a private development keystore path} "
: "${TASK_KEYSTORE_PASSWORD:?Set the development keystore password in your environment}"
if [ ! -f "$TASK_KEYSTORE" ]; then keytool -genkeypair -keystore "$TASK_KEYSTORE" -storepass:env TASK_KEYSTORE_PASSWORD -keypass:env TASK_KEYSTORE_PASSWORD -alias androiddebugkey -dname 'CN=Inferno Demo,O=Demo,C=US' -keyalg RSA -keysize 2048 -validity 10000 >/dev/null; fi
"$TASK_BUILD_TOOLS/apksigner" sign --ks "$TASK_KEYSTORE" --ks-pass env:TASK_KEYSTORE_PASSWORD --key-pass env:TASK_KEYSTORE_PASSWORD --out "$TASK_OUT/Clover-Inferno-Demo.apk" "$TASK_OUT/aligned.apk"
"$TASK_BUILD_TOOLS/apksigner" verify --verbose "$TASK_OUT/Clover-Inferno-Demo.apk"
python3 tools/verify_apk.py "$TASK_OUT/Clover-Inferno-Demo.apk"
