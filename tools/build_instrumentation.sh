#!/usr/bin/env bash
set -euo pipefail
TASK_SDK_ROOT=${TASK_SDK_ROOT:-/workspace/android-tools}
TASK_PLATFORM_JAR="$TASK_SDK_ROOT/platforms/android-35/android.jar"
TASK_BUILD_TOOLS="$TASK_SDK_ROOT/build-tools/android-15"
: "${TASK_KEYSTORE:?Set TASK_KEYSTORE to the same private development keystore as the app}"
: "${TASK_KEYSTORE_PASSWORD:?Set the development keystore password in your environment}"
TASK_OUT=app/build/native-test
mkdir -p "$TASK_OUT/classes" "$TASK_OUT/dex"
cat > "$TASK_OUT/AndroidManifest.xml" <<'MANIFEST'
<manifest xmlns:android="http://schemas.android.com/apk/res/android" package="com.mohamadhh.goldenclover.test">
 <application android:label="Inferno Verification" android:debuggable="true"/>
 <instrumentation android:name="com.mohamadhh.goldenclover.SmokeInstrumentation" android:targetPackage="com.mohamadhh.goldenclover" android:functionalTest="true"/>
</manifest>
MANIFEST
"$TASK_BUILD_TOOLS/aapt2" link -o "$TASK_OUT/resources.apk" --manifest "$TASK_OUT/AndroidManifest.xml" -I "$TASK_PLATFORM_JAR" --min-sdk-version 23 --target-sdk-version 35
rg --files app/src/androidTest/java -g '*.java' > "$TASK_OUT/sources.txt"
java -m jdk.compiler/com.sun.tools.javac.Main -source 8 -target 8 -classpath "$TASK_PLATFORM_JAR:app/build/native/classes.jar" -d "$TASK_OUT/classes" @"$TASK_OUT/sources.txt"
java -m jdk.jartool/sun.tools.jar.Main --create --file "$TASK_OUT/classes.jar" -C "$TASK_OUT/classes" .
"$TASK_BUILD_TOOLS/d8" --lib "$TASK_PLATFORM_JAR" --classpath app/build/native/classes.jar --min-api 23 --output "$TASK_OUT/dex" "$TASK_OUT/classes.jar"
cp "$TASK_OUT/resources.apk" "$TASK_OUT/unsigned.apk"
python3 - "$TASK_OUT" <<'PY'
import sys,zipfile,pathlib
root=pathlib.Path(sys.argv[1])
with zipfile.ZipFile(root/'unsigned.apk','a',zipfile.ZIP_DEFLATED) as z:
 for p in (root/'dex').glob('*.dex'):z.write(p,p.name)
PY
"$TASK_BUILD_TOOLS/zipalign" -f -p 4 "$TASK_OUT/unsigned.apk" "$TASK_OUT/aligned.apk"
"$TASK_BUILD_TOOLS/apksigner" sign --ks "$TASK_KEYSTORE" --ks-pass env:TASK_KEYSTORE_PASSWORD --key-pass env:TASK_KEYSTORE_PASSWORD --out "$TASK_OUT/Inferno-Test.apk" "$TASK_OUT/aligned.apk"
