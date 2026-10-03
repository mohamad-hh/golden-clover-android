package com.mohamadhh.goldenclover;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.*;
import android.view.*;
import com.mohamadhh.goldenclover.game.*;
import java.util.Locale;

/** Hardware accelerated, layered renderer. Every game texture is independent of the reference. */
public final class GoldenCloverView extends View implements Choreographer.FrameCallback {
 private final GameState g=new GameState(System.nanoTime());
 private final AssetLoader assets=new AssetLoader();
 private final ResponsiveLayout layout=new ResponsiveLayout();
 private final ParticleSystem particles=new ParticleSystem();
 private static final Typeface SERIF=Typeface.create("serif",Typeface.BOLD),SANS=Typeface.create("sans-serif-medium",Typeface.NORMAL);
 private static final String[] JACKPOTS={"GRAND","MAJOR","MINOR","MINI"},JACKPOT_ART={"grand_panel","major_panel","minor_panel","mini_panel"};
 private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG);
 private final RectF dst=new RectF();private final Rect source=new Rect();
 private final SharedPreferences prefs;
 private AudioManager audio;
 private volatile boolean loaded,disposed;private volatile String error;
 private boolean active=true,pressed;private long last;private float ambient,cacheTime,shower,leafTime;
 private int insetL,insetT,insetR,insetB;
 private final String[] bonusLabels=new String[15];private final double[] cachedBonus=new double[15];
 private String creditText="",betText="",winText="",grandText="",majorText="";
 private final Shader gold=new LinearGradient(0,0,0,100,new int[]{0xfffff4be,0xffffd35a,0xffa65a0a,0xffffdf72},null,Shader.TileMode.MIRROR);
 private static final float GX=420,GY=278,CW=222,CH=196;
 public GoldenCloverView(Context c){super(c);setFocusable(true);prefs=c.getSharedPreferences("golden-clover-x",0);g.credit=Double.longBitsToDouble(prefs.getLong("credit",Double.doubleToLongBits(1000)));g.bet.select(prefs.getInt("bet",20));g.ui.sound=prefs.getBoolean("sound",true);setOnApplyWindowInsetsListener((v,i)->{if(android.os.Build.VERSION.SDK_INT>=28&&i.getDisplayCutout()!=null){insetL=i.getDisplayCutout().getSafeInsetLeft();insetR=i.getDisplayCutout().getSafeInsetRight();insetT=i.getDisplayCutout().getSafeInsetTop();insetB=i.getDisplayCutout().getSafeInsetBottom();}layout.fit(getWidth(),getHeight(),insetL,insetT,insetR,insetB);return i;});
  new Thread(()->{try{assets.load(c.getAssets());if(disposed){assets.release();return;}AudioManager a=new AudioManager(c);if(disposed){a.release();assets.release();return;}audio=a;audio.enabled(g.ui.sound);loaded=true;postInvalidate();}catch(Exception e){error=e.getMessage();postInvalidate();}},"asset-loader").start();Choreographer.getInstance().postFrameCallback(this);
 }
 @Override protected void onSizeChanged(int w,int h,int ow,int oh){layout.fit(w,h,insetL,insetT,insetR,insetB);}
 public void pause(){active=false;last=0;Choreographer.getInstance().removeFrameCallback(this);if(audio!=null)audio.pause();save();}
 public void resume(){if(disposed)return;active=true;last=0;if(audio!=null)audio.resume();Choreographer.getInstance().removeFrameCallback(this);Choreographer.getInstance().postFrameCallback(this);}
 private void save(){prefs.edit().putLong("credit",Double.doubleToLongBits(g.credit)).putInt("bet",g.bet.value).putBoolean("sound",g.ui.sound).apply();}
 public void dispose(){disposed=true;pause();if(audio!=null)audio.release();if(loaded)assets.release();}
 public boolean closePopup(){if(g.ui.betMenu||g.ui.menu){g.ui.betMenu=false;g.ui.menu=false;return true;}return false;}
 @Override public void doFrame(long frame){if(!active||disposed)return;float dt=last==0?0:Math.min(.05f,(frame-last)/1000000000f);last=frame;
  if(loaded){g.update(dt);int ev=g.consumeEvents();if(audio!=null){audio.enabled(g.ui.sound);audio.events(ev);audio.loop(g.phase==GameState.Phase.SPIN&&g.reels.stopped<5||g.phase==GameState.Phase.RESPIN);}
   if((ev&GameState.COINS)!=0)particles.burst(980,490,g.win>=g.bet.value*10?150:45,1);
   if((ev&GameState.TRIGGER)!=0)particles.burst(1710,385+g.tree*180,75,2);
   if((ev&GameState.DROP)!=0){particles.burst(980,540,90,0);particles.burst(980,540,75,1);}
   shower+=dt;if((g.phase==GameState.Phase.WIN&&g.win>=g.bet.value*10||g.phase==GameState.Phase.BONUS_END)&&shower>.13f){particles.burst(960+760*(float)Math.sin(g.clock*5.7f),70,g.bonus.grand?24:7,1);shower=0;}leafTime+=dt;if(leafTime>1.2f){particles.leaf(1690,310+(int)(g.clock/1.2f)%3*178);leafTime=0;}particles.update(dt);ambient+=dt;if(ambient>.1f){particles.ambient();ambient=0;}
   cacheTime-=dt;if(cacheTime<=0){cacheTime=.1f;creditText=money(g.credit);betText=money(g.bet.value);double shown=g.phase==GameState.Phase.WIN||g.phase==GameState.Phase.BONUS_END?g.win*AnimationManager.ease(g.time/3):g.win;winText=money(shown);grandText=integer(g.grand);majorText=integer(g.major);for(int i=0;i<15;i++)if(bonusLabels[i]==null||cachedBonus[i]!=g.bonus.values[i]){cachedBonus[i]=g.bonus.values[i];bonusLabels[i]=g.bonus.jackpot[i]>=0?JACKPOTS[g.bonus.jackpot[i]]:money(g.bonus.values[i]);}}
  }invalidate();Choreographer.getInstance().postFrameCallback(this);
 }
 private static String money(double value){return String.format(Locale.US,"%,.2f TL",value);}
 private static String integer(double value){return String.format(Locale.US,"%,d",(long)value);}
 private void bmp(Canvas c,Bitmap b,float x,float y,float w,float h,int alpha){if(b==null)return;p.setShader(null);p.setColor(Color.WHITE);p.setAlpha(Math.max(0,Math.min(255,alpha)));dst.set(x,y,x+w,y+h);c.drawBitmap(b,null,dst,p);p.setAlpha(255);}
 private void art(Canvas c,String name,float x,float y,float w,float h){if(name.equals("reel_frame")||name.equals("bonus_panel")){frame(c,assets.get(name),x,y,w,h);return;}bmp(c,assets.get(name),x,y,w,h,255);}
 private void frame(Canvas c,Bitmap b,float x,float y,float w,float h){p.setShader(null);p.setColor(Color.WHITE);p.setAlpha(255);int bw=b.getWidth(),bh=b.getHeight(),edge=60;float capX=43,capY=37;for(int row=0;row<3;row++)for(int col=0;col<3;col++){int sl=col==0?0:col==1?edge:bw-edge,st=row==0?0:row==1?edge:bh-edge,sr=col==0?edge:col==1?bw-edge:bw,sb=row==0?edge:row==1?bh-edge:bh;float dl=x+(col==0?0:col==1?capX:w-capX),dt=y+(row==0?0:row==1?capY:h-capY),dr=x+(col==0?capX:col==1?w-capX:w),db=y+(row==0?capY:row==1?h-capY:h);source.set(sl,st,sr,sb);dst.set(dl,dt,dr,db);c.drawBitmap(b,source,dst,p);}}

 private void rect(Canvas c,float x,float y,float w,float h,int color,float radius){p.setShader(null);p.setColor(color);p.setStyle(Paint.Style.FILL);dst.set(x,y,x+w,y+h);c.drawRoundRect(dst,radius,radius,p);}
 private void text(Canvas c,String text,float x,float y,float size,int color,boolean bold){p.setShader(null);p.setColor(color);p.setTextSize(size);p.setTypeface(bold?SERIF:SANS);p.setTextAlign(Paint.Align.CENTER);p.setShadowLayer(3,0,2,0xdd000000);c.drawText(text,x,y,p);p.clearShadowLayer();}
 private void goldText(Canvas c,String s,float x,float y,float size){p.setTextSize(size);p.setTypeface(SERIF);p.setTextAlign(Paint.Align.CENTER);p.setColor(0xff3b1903);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(size*.075f);c.drawText(s,x,y,p);p.setStyle(Paint.Style.FILL);p.setShader(gold);c.drawText(s,x,y,p);p.setShader(null);}
 @Override protected void onDraw(Canvas c){super.onDraw(c);c.drawColor(0xff03130d);if(!loaded){text(c,error==null?"GOLDEN CLOVER X • LOADING ART & AUDIO":"ASSET ERROR: "+error,getWidth()/2f,getHeight()/2f,24,0xffffd76a,true);return;}
  Bitmap bg=assets.get("background");float cover=Math.max(getWidth()/(float)bg.getWidth(),getHeight()/(float)bg.getHeight());bmp(c,bg,(getWidth()-bg.getWidth()*cover)/2,(getHeight()-bg.getHeight()*cover)/2,bg.getWidth()*cover,bg.getHeight()*cover,255);
  c.save();c.translate(layout.left,layout.top);c.scale(layout.scale,layout.scale);
  rect(c,0,0,1920,1080,0x39001008,0);
  boolean bounce=g.phase==GameState.Phase.BOUNCE;float zoom=bounce?1+.018f*(float)Math.sin(Math.min(1,g.time/3.2f)*Math.PI):1;
  c.save();c.translate(960,540);c.scale(zoom,zoom);c.translate(-960,-540);
  art(c,"logo",520,45,920,164);text(c,"THE ENCHANTED CLOVER GARDEN",980,234,20,0xffedda9b,false);
  jackpots(c);trees(c,bounce);reels(c);controls(c);c.restore();
  if(bounce)bounceScene(c);
  if(g.phase==GameState.Phase.BONUS||g.phase==GameState.Phase.RESPIN||g.phase==GameState.Phase.BONUS_END)bonusScene(c);
  if(g.phase==GameState.Phase.WIN&&g.win>=g.bet.value*10)celebration(c,false);
  if(g.phase==GameState.Phase.BONUS_END)celebration(c,g.bonus.grand);
  drawParticles(c);
  if(g.ui.betMenu)betMenu(c);if(g.ui.menu)menu(c);
  c.restore();
 }
 private void jackpots(Canvas c){for(int i=0;i<4;i++){float y=275+i*130;art(c,JACKPOT_ART[i],32,y,336,120);goldText(c,JACKPOTS[i],200,y+45,32);text(c,i==0?grandText:i==1?majorText:i==2?"1,250":"500",200,y+84,38,0xfffff3c6,true);}art(c,"bonus_button",53,814,300,98);goldText(c,"CLOVER BONUS",203,854,24);text(c,"20 × BET",203,883,22,0xffeee3b9,false);}
 private void trees(Canvas c,boolean bounce){for(int i=0;i<3;i++){float x=1605,y=280+i*178;float sway=(float)Math.sin(g.clock*1.4f+i)*1.6f;if(bounce&&g.tree==i)sway+=(float)Math.sin(g.time*55)*5;int glow=(int)(60+35*Math.sin(g.clock*2+i));if(bounce&&g.tree==i)glow=200;bmp(c,assets.get("glow"),x-26,y-26,320,230,glow);c.save();c.rotate(sway,x+135,y+168);art(c,"tree_trunk",x+48,y+67,170,122);art(c,i==0?"tree_blue":i==1?"tree_red":"tree_green",x,y,270,155);bmp(c,assets.get("clover_clusters"),x+68,y+62,125,83,120);c.restore();text(c,i==0?"SAPPHIRE":i==1?"RUBY":"EMERALD",x+137,y+185,17,0xffffe6a0,false);}}
 private void reels(Canvas c){art(c,"reel_frame",377,241,1196,662);rect(c,GX,GY,CW*5,CH*3,0xff02120c,6);
  for(int col=0;col<5;col++){float x=GX+col*CW;for(int r=0;r<3;r++)art(c,"reel_cell",x+2,GY+r*CH+2,CW-4,CH-4);c.save();c.clipRect(x+4,GY+3,x+CW-4,GY+CH*3-3);boolean moving=g.phase==GameState.Phase.SPIN&&g.reels.stopped<=col;
   if(moving){float offset=g.time<.18f?-30*AnimationManager.ease(g.time/.18f):(g.time*1650+col*110)%CH;int step=(int)(g.time*14);for(int r=-1;r<4;r++){int s=Math.floorMod(step+r+col*3,9);float y=GY+r*CH+offset;bmp(c,assets.symbols[s],x+25,y+12,CW-50,CH-24,255);bmp(c,assets.symbols[s],x+30,y-28,CW-60,CH+35,65);bmp(c,assets.symbols[s],x+30,y+40,CW-60,CH+35,42);}}
   else{for(int r=0;r<3;r++){int s=g.reels.symbols[col][r];float l=g.reels.landing[col];float pop=(float)Math.sin(l*9)*l*.09f;boolean glow=g.reels.glow[col][r]&&g.phase==GameState.Phase.WIN;float pulse=glow?1+.05f*(float)Math.sin(g.clock*7):1;float w=(CW-43)*(1+pop)*pulse,h=(CH-20)*(1-pop)*pulse;float y=GY+r*CH+(CH-h)/2+(float)Math.sin(l*8)*l*13;if(glow)bmp(c,assets.get("glow"),x-10,GY+r*CH-22,CW+20,CH+44,200);bmp(c,glow?assets.wins[s]:assets.symbols[s],x+(CW-w)/2,y,w,h,255);}}
   c.restore();p.setColor(0xffa88f40);p.setStrokeWidth(2);c.drawLine(x+CW,GY,x+CW,GY+CH*3,p);
  }
  rect(c,GX,GY,CW*5,16,0x99000000,0);rect(c,GX,GY+CH*3-16,CW*5,16,0x77000000,0);
 }
 private void controls(Canvas c){art(c,"credit_panel",34,941,381,118);art(c,"bet_panel",424,941,288,118);art(c,"win_panel",718,941,460,118);goldText(c,"CREDIT",222,979,26);text(c,creditText,222,1023,32,0xfffff7d0,true);goldText(c,"BET",568,979,26);text(c,betText,568,1023,32,0xfffff7d0,true);goldText(c,"WIN",948,979,26);text(c,winText,948,1023,32,0xfffff7d0,true);
  art(c,"auto_button",1200,959,197,89);goldText(c,g.ui.auto?"AUTO ON":"AUTO OFF",1298,1006,25);if(g.ui.auto){p.setColor(0xff62ff6b);c.drawCircle(1381,978,7,p);}
  art(c,"menu_button",1408,959,97,89);for(int i=0;i<3;i++)rect(c,1439,983+i*12,35,4,0xffffdd80,2);
  art(c,"sound_button",1512,959,97,89);text(c,g.ui.sound?"SFX":"MUTE",1560,1010,21,0xffffdf93,true);
  float size=pressed?218:230;boolean spinning=g.phase==GameState.Phase.SPIN;boolean disabled=g.busy()&&!spinning||!g.busy()&&g.credit<g.bet.value;bmp(c,assets.get("spin_button"),1764-size/2,946-size/2,size,size,disabled?100:255);
  if(spinning){c.save();c.rotate(g.clock*190,1764,946);p.setColor(0xffffefaa);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(6);dst.set(1660,842,1868,1050);c.drawArc(dst,0,270,false,p);p.setStyle(Paint.Style.FILL);c.restore();}text(c,g.ui.auto?"AUTO SPIN":spinning?"SPINNING":"SPIN",1764,1080-18,19,0xffffecaa,true);
  text(c,g.ui.messageTime>0?g.ui.message:"7 LINES • VIRTUAL CREDITS ONLY",960,927,20,0xffffedb5,false);
 }
 private void bounceScene(Canvas c){rect(c,0,0,1920,1080,0x9b00170a,0);float drop=AnimationManager.bounce((g.time-.7f)/1.2f);float y=-430+drop*830;float shake=g.time>1.8f?(float)Math.sin(g.time*24)*Math.max(0,3-g.time)*5:0;c.save();c.rotate(shake,960,y+230);bmp(c,assets.get("glow"),625,y-35,670,600,180);art(c,"bonus_bell",745,y,430,430);c.restore();if(g.time>1.8f){bmp(c,assets.get("rays"),280,5,1360,1030,(int)(150*Math.min(1,g.time-1.8f)));goldText(c,"CLOVER BONUS",960,824,92);text(c,"3 RESPINS REMAINING",960,885,36,0xffffedb0,true);}}
 private void bonusScene(Canvas c){rect(c,0,0,1920,1080,0xe3001009,0);bmp(c,assets.get("rays"),480,-100,960,900,85);goldText(c,"CLOVER BONUS",960,154,84);text(c,g.bonus.full?"FULL GARDEN!":g.bonus.remaining+" RESPINS REMAINING",960,222,36,0xffffe9a5,true);art(c,"bonus_panel",377,244,1196,662);
  for(int col=0;col<5;col++)for(int r=0;r<3;r++){int i=col*3+r;float x=GX+col*CW,y=GY+r*CH;art(c,"reel_cell",x+4,y+4,CW-8,CH-8);if(g.bonus.values[i]>0){float pop=g.phase==GameState.Phase.BONUS?AnimationManager.ease(g.time/.35f):1;c.save();c.scale(pop,pop,x+CW/2,y+CH/2);bmp(c,assets.get("glow"),x+1,y+3,CW-2,CH-6,100);art(c,"clover_clusters",x+25,y+2,CW-50,CH-10);rect(c,x+27,y+116,CW-54,53,0xdf03190d,16);text(c,bonusLabels[i]==null?"":bonusLabels[i],x+CW/2,y+153,26,0xffffe694,true);c.restore();}else if(g.phase==GameState.Phase.RESPIN){float offset=(g.time*900+col*80)%CH;c.save();c.clipRect(x+4,y+4,x+CW-4,y+CH-4);bmp(c,assets.symbols[2],x+45,y+offset-CH,132,142,100);bmp(c,assets.symbols[2],x+45,y+offset,132,142,80);c.restore();}else{text(c,"•",x+CW/2,y+CH/2+8,30,0xff386247,false);}}
  text(c,"NEW CLOVERS RESET THE COUNTER TO 3 • LOCKED VALUES ARE AWARDED TOGETHER",960,957,23,0xfff0d89a,false);text(c,"CREDIT  "+creditText+"     BET  "+betText,960,1020,25,0xfffff1c4,true);
 }
 private void celebration(Canvas c,boolean grand){float t=g.time;rect(c,0,0,1920,1080,0x9a001309,0);c.save();c.rotate(t*12,960,510);bmp(c,assets.get("rays"),290,-140,1340,1340,220);c.restore();String title=grand?"GRAND":g.bonus.full&&g.phase==GameState.Phase.BONUS_END?"FULL GARDEN":g.win>=g.bet.value*50?"MEGA WIN":"BIG WIN";float s=AnimationManager.ease(t/.5f);c.save();c.scale(s,s,960,480);goldText(c,title,960,480,grand?155:123);goldText(c,winText,960,622,82);text(c,"VIRTUAL CREDITS",960,693,24,0xffffedac,false);c.restore();}
 private void drawParticles(Canvas c){for(int i=0;i<ParticleSystem.MAX;i++)if(particles.life[i]>0){float a=particles.life[i]/particles.duration[i];float s=particles.size[i];c.save();c.rotate(particles.angle[i],particles.x[i],particles.y[i]);bmp(c,assets.get(particles.type[i]==1?"coin":particles.type[i]>=2?"clover_clusters":"spark"),particles.x[i]-s/2,particles.y[i]-s/2,s,s,(int)(255*Math.min(1,a*2)));c.restore();}}
 private void betMenu(Canvas c){rect(c,0,0,1920,1080,0xb3000806,0);art(c,"reel_frame",495,239,930,617);goldText(c,"SELECT BET",960,355,51);for(int i=0;i<9;i++){float x=585+i%3*260,y=402+i/3*123;art(c,"bet_panel",x,y,231,110);if(g.bet.value==BetState.VALUES[i]){rect(c,x+22,y+23,188,64,0x995fc529,12);p.setColor(0xffffdc61);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(3);dst.set(x+22,y+23,x+210,y+87);c.drawRoundRect(dst,12,12,p);p.setStyle(Paint.Style.FILL);}text(c,money(BetState.VALUES[i]),x+116,y+67,29,0xfffff1ba,true);}text(c,"TAP OUTSIDE TO CLOSE",960,807,21,0xffdfcfac,false);}
 private void menu(Canvas c){rect(c,0,0,1920,1080,0xde001008,0);art(c,"reel_frame",310,180,1300,710);goldText(c,"GOLDEN CLOVER X",960,286,60);text(c,"Entertainment prototype • virtual TL credits only",960,347,27,0xffffedbc,false);text(c,"7 lines pay matching symbols left to right (3, 4 or 5).",960,409,25,0xffffedbc,false);text(c,"5+ Clover / Pot symbols unlock the hold-and-respin garden.",960,461,25,0xffffedbc,false);text(c,"New values lock and reset 3 respins. No new value removes one.",960,513,25,0xffffedbc,false);text(c,"Clover values scale with your bet. MINI / MINOR / MAJOR / GRAND are fixed.",960,565,23,0xffffedbc,false);text(c,"Bonus button costs 20 × bet in virtual credits. Auto stops at low balance.",960,617,23,0xffffedbc,false);text(c,"No purchases, payments or cash rewards.",960,669,25,0xffffedbc,false);art(c,"bet_panel",740,706,440,110);text(c,"RESET DEMO CREDIT TO 1,000 TL",960,774,23,0xffffedbc,true);text(c,"TAP OUTSIDE TO CLOSE",960,850,21,0xffffedbc,false);}
 private boolean hit(float x,float y,float l,float t,float w,float h){return x>=l&&x<=l+w&&y>=t&&y<=t+h;}
 @Override public boolean onTouchEvent(android.view.MotionEvent e){if(!loaded)return true;float x=layout.x(e.getX()),y=layout.y(e.getY());if(e.getAction()==MotionEvent.ACTION_DOWN){pressed=hit(x,y,1640,825,240,240);invalidate();return true;}if(e.getAction()==MotionEvent.ACTION_CANCEL){pressed=false;return true;}if(e.getAction()!=MotionEvent.ACTION_UP)return true;pressed=false;g.events|=GameState.CLICK;
  if(g.ui.betMenu){for(int i=0;i<9;i++)if(hit(x,y,585+i%3*260,402+i/3*123,231,110)){g.selectBet(BetState.VALUES[i]);g.ui.betMenu=false;cacheTime=0;return true;}if(!hit(x,y,495,239,930,617))g.ui.betMenu=false;return true;}
  if(g.ui.menu){if(hit(x,y,740,706,440,110)&&!g.busy()){g.credit=1000;g.ui.menu=false;cacheTime=0;save();}else if(!hit(x,y,310,180,1300,710))g.ui.menu=false;return true;}
  if(hit(x,y,1640,825,240,240))g.spin();else if(hit(x,y,424,941,288,118)&&!g.busy())g.ui.betMenu=true;else if(hit(x,y,1200,959,197,89))g.toggleAuto();else if(hit(x,y,1512,959,97,89))g.ui.sound=!g.ui.sound;else if(hit(x,y,1408,959,97,89))g.ui.menu=true;else if(hit(x,y,53,814,300,98))g.buyBonus();cacheTime=0;save();performClick();return true;
 }
 @Override public boolean performClick(){super.performClick();return true;}
}
