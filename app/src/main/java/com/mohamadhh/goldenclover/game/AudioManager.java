package com.mohamadhh.goldenclover.game;
import android.content.Context;import android.media.AudioAttributes;import android.media.SoundPool;import android.content.res.AssetFileDescriptor;
import java.io.IOException;import java.util.Set;import java.util.Collections;import java.util.HashSet;
public final class AudioManager {
 public static final String[] NAMES={"click","spin_start","reel_loop","reel_stop_1","reel_stop_2","reel_stop_3","reel_stop_4","reel_stop_5","normal_win","medium_win","big_win","coin_shower","bonus_trigger","bell_drop","respin_reset","jackpot"};
 private final int[] ids=new int[NAMES.length];private final Set<Integer> ready=Collections.synchronizedSet(new HashSet<Integer>());private final SoundPool pool;private boolean enabled=true;private int loop;private boolean wantLoop;
 public AudioManager(Context c)throws IOException{pool=new SoundPool.Builder().setMaxStreams(10).setAudioAttributes(new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build()).build();pool.setOnLoadCompleteListener((p,id,status)->{if(status==0)ready.add(id);});for(int i=0;i<NAMES.length;i++)try(AssetFileDescriptor fd=c.getAssets().openFd("audio/"+NAMES[i]+".wav")){ids[i]=pool.load(fd,1);}}
 public int readyCount(){return ready.size();}
 public void enabled(boolean on){enabled=on;if(!on)stopLoop();}
 public void play(int i){if(enabled&&ready.contains(ids[i]))pool.play(ids[i],.8f,.8f,1,0,1);}
 public void loop(boolean on){wantLoop=on;if(!on){stopLoop();return;}if(enabled&&loop==0&&ready.contains(ids[2]))loop=pool.play(ids[2],.22f,.22f,0,-1,1);}
 private void stopLoop(){if(loop!=0)pool.stop(loop);loop=0;}
 public void events(int e){if((e&GameState.CLICK)!=0)play(0);if((e&GameState.START)!=0)play(1);for(int c=0;c<5;c++)if((e&(GameState.STOP0<<c))!=0)play(3+c);if((e&GameState.NORMAL)!=0)play(8);if((e&GameState.MEDIUM)!=0)play(9);if((e&GameState.BIG)!=0)play(10);if((e&GameState.COINS)!=0)play(11);if((e&GameState.TRIGGER)!=0)play(12);if((e&GameState.DROP)!=0)play(13);if((e&GameState.RESET)!=0)play(14);if((e&GameState.JACKPOT)!=0)play(15);}
 public void pause(){pool.autoPause();stopLoop();}public void resume(){pool.autoResume();}public void release(){pool.release();}
}
