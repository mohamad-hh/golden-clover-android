package com.mohamadhh.goldenclover.game;
import android.content.res.AssetManager;
import android.graphics.Bitmap;import android.graphics.BitmapFactory;
import java.io.InputStream;import java.io.IOException;import java.util.HashMap;
public final class AssetLoader {
 public static final String[] SYMBOLS={"bell","seven","clover","lemon","orange","grapes","watermelon","diamond","pot"};
 public final Bitmap[] symbols=new Bitmap[9],wins=new Bitmap[9];
 private final HashMap<String,Bitmap> art=new HashMap<>();
 public void load(AssetManager am)throws IOException {
  for(int i=0;i<9;i++){symbols[i]=read(am,"symbols/"+SYMBOLS[i]+".webp");wins[i]=read(am,"symbols/"+SYMBOLS[i]+"_win.webp");}
  String[] names={"background","inferno_background","reel_frame","reel_cell","grand_panel","major_panel","minor_panel","mini_panel","logo","bet_panel","credit_panel","win_panel","spin_button","auto_button","sound_button","menu_button","bonus_button","tree_trunk","tree_blue","tree_red","tree_green","clover_clusters","glow","spark","coin","bonus_panel","bonus_bell","rays"};
  for(String n:names)art.put(n,read(am,"art/"+n+".webp"));
 }
 private Bitmap read(AssetManager am,String p)throws IOException{try(InputStream in=am.open(p)){BitmapFactory.Options o=new BitmapFactory.Options();o.inScaled=false;Bitmap b=BitmapFactory.decodeStream(in,null,o);if(b==null)throw new IOException("Invalid texture: "+p);return b;}}
 public Bitmap get(String n){return art.get(n);}
 public void release(){for(Bitmap b:symbols)if(b!=null)b.recycle();for(Bitmap b:wins)if(b!=null)b.recycle();for(Bitmap b:art.values())b.recycle();art.clear();}
}
