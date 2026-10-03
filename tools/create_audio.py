#!/usr/bin/env python3
"""Original procedural casino sound design: bell partials, filtered noise, arpeggios.
Generated PCM files are committed and packaged; no runtime synthesizer or external audio.
"""
import wave,json,hashlib
from pathlib import Path
import numpy as np
root=Path(__file__).resolve().parents[1]/'app/src/main/assets/audio';root.mkdir(exist_ok=True)
SR=44100;rng=np.random.default_rng(6246)
def tone(freq,duration,volume=.3,metal=False):
 t=np.arange(int(duration*SR))/SR;env=np.minimum(t/.006,1)*np.exp(-t*(3.8 if metal else 5)/duration)
 out=np.sin(2*np.pi*freq*t)*env
 for ratio,amp in ([(2.01,.35),(2.76,.22),(4.12,.14),(5.44,.08)] if metal else [(2,.2),(3,.07)]):out+=amp*np.sin(2*np.pi*freq*ratio*t)*env*np.exp(-t*3)
 return out*volume

def write(name,data):
 data=np.tanh(data)*.85
 with wave.open(str(root/(name+'.wav')),'wb') as f:f.setnchannels(1);f.setsampwidth(2);f.setframerate(SR);f.writeframes((data*32767).astype('<i2').tobytes())
def song(name,notes,beat=.15,duration=2,volume=.18):
 data=np.zeros(int(duration*SR))
 for i,n in enumerate(notes):
  start=int(i*beat*SR);s=tone(n,min(.9,duration-i*beat),volume,True);end=min(len(data),start+len(s));data[start:end]+=s[:end-start]
 write(name,data)
write('click',tone(740,.085,.21))
t=np.arange(int(.7*SR))/SR;write('spin_start',np.sin(2*np.pi*(90*t+160*t*t))*np.minimum(t/.025,1)*np.exp(-t*5)*.3+tone(440,.7,.13))
t=np.arange(SR)/SR;noise=rng.normal(0,1,SR);filtered=np.convolve(noise,np.ones(30)/30,mode='same');loop=(.1*np.sin(2*np.pi*72*t)+filtered*.3)*( .4+.6*np.sin(np.pi*12*t)**2 );loop*=np.minimum(t/.025,1)*np.minimum((1-t)/.025,1);write('reel_loop',loop)
for i in range(5):write('reel_stop_'+str(i+1),tone(190+i*55,.28,.38,True)+tone(74,.28,.14))
song('normal_win',[523,659,784,1047],.13,1.5)
song('medium_win',[392,523,659,784,1047,1319],.14,2.1)
song('big_win',[262,330,392,523,659,784,1047,1319,1568,1047,1319,1568,2093],.2,4.5,.21)
song('bonus_trigger',[330,392,494,659,784,988,1319],.18,3)
song('bell_drop',[220,440,660,880,220,440],.18,2.6,.24)
song('respin_reset',[659,784,1047,1319],.11,1.2)
song('jackpot',[262,330,392,523,330,392,523,659,392,523,659,784,523,659,784,1047,1319,1568,2093],.23,6,.24)
data=np.zeros(SR*3)
for i in range(42):
 start=int(rng.uniform(0,2.65)*SR);s=tone(rng.uniform(1000,3000),.35,.07,True);end=min(len(data),start+len(s));data[start:end]+=s[:end-start]
write('coin_shower',data)
manifest={f.name:{'bytes':f.stat().st_size,'sha256':hashlib.sha256(f.read_bytes()).hexdigest()} for f in sorted(root.glob('*.wav'))};(root.parent/'audio-manifest.json').write_text(json.dumps(manifest,indent=2)+'\n');print(f'{len(manifest)} original audio cues: {sum(v["bytes"] for v in manifest.values()):,} bytes')
