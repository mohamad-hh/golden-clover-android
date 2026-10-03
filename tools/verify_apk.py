#!/usr/bin/env python3
"""Fail if any real graphics/audio asset is absent, empty or changed in the APK."""
import sys,zipfile,json,hashlib
from pathlib import Path
apk=Path(sys.argv[1]);assert apk.is_file(),'APK missing';assert apk.stat().st_size>0,'APK empty'
root=Path(__file__).resolve().parents[1]/'app/src/main/assets'
with zipfile.ZipFile(apk) as z:
 names=set(z.namelist());verified=0;payload=0
 for manifest,prefix in [('graphics-manifest.json',''),('audio-manifest.json','audio/')]:
  expected=json.loads((root/manifest).read_text());assert expected,'Manifest empty'
  for name,spec in expected.items():
   path='assets/'+prefix+name;assert path in names,'Missing '+path;data=z.read(path);assert len(data)==spec['bytes'],'Size mismatch '+path;assert hashlib.sha256(data).hexdigest()==spec['sha256'],'Checksum mismatch '+path;verified+=1;payload+=len(data)
  assert 'assets/'+manifest in names,'Missing packaged manifest'
 required=['assets/art/background.webp','assets/art/reel_frame.webp','assets/art/logo.webp','assets/art/tree_blue.webp','assets/art/tree_red.webp','assets/art/tree_green.webp','assets/audio/jackpot.wav','assets/audio/reel_loop.wav']
 for path in required:assert path in names,'Missing required '+path
 assert not any(n.endswith('index.html') or 'file_00000000a0588246ba8d5de301606f3d.png' in n for n in names),'Legacy screenshot/WebView assets must not ship'
 print(f'APK VERIFIED: {apk.name}: {apk.stat().st_size:,} bytes ({apk.stat().st_size/1048576:.2f} MiB)')
 print(f'{verified} independent graphics/audio files, {payload:,} packaged asset bytes; SHA-256 verified')
