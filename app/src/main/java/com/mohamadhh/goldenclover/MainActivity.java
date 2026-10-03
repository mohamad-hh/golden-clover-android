package com.mohamadhh.goldenclover;
import android.app.Activity;import android.os.Bundle;import android.view.View;import android.view.WindowManager;
public final class MainActivity extends Activity {
 private GoldenCloverView game;
 @Override public void onCreate(Bundle b){super.onCreate(b);getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);if(android.os.Build.VERSION.SDK_INT>=28){WindowManager.LayoutParams p=getWindow().getAttributes();p.layoutInDisplayCutoutMode=WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;getWindow().setAttributes(p);}immersive();game=new GoldenCloverView(this);setContentView(game);}
 private void immersive(){getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN|View.SYSTEM_UI_FLAG_HIDE_NAVIGATION|View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY|View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION|View.SYSTEM_UI_FLAG_LAYOUT_STABLE);}
 @Override public void onWindowFocusChanged(boolean f){super.onWindowFocusChanged(f);if(f)immersive();}
 @Override protected void onPause(){if(game!=null)game.pause();super.onPause();}
 @Override protected void onResume(){super.onResume();if(game!=null)game.resume();}
 @Override protected void onDestroy(){if(game!=null)game.dispose();super.onDestroy();}
 @Override public void onBackPressed(){if(game.closePopup())return;super.onBackPressed();}
}
