package com.mohamadhh.goldenclover;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.*;
import android.view.*;
import com.mohamadhh.goldenclover.game.*;
import java.util.Locale;

/** Native portrait renderer. Original art and audio; virtual demo dollars only. */
public final class GoldenCloverView extends View implements Choreographer.FrameCallback {
 private final GameState g=new GameState(System.nanoTime());
 private final AssetLoader assets=new AssetLoader();
 private final ResponsiveLayout layout=new ResponsiveLayout();
 private final ParticleSystem particles=new ParticleSystem();
 private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG);
 private final RectF dst=new RectF();
 private static final Typeface SERIF=Typeface.create("serif",Typeface.BOLD),SANS=Typeface.create("sans-serif-medium",Typeface.NORMAL);
 private static final int GOLD=0xffffd56c,CREAM=0xffffefc0;
 private static final int[] COLORS={0xff28caff,0xffff3c55,0xff67eb42};
 private static final String[] FEATURES={"BLAZE","JACKPOT","BOMB"};
 private static final float GX=27,GY=758,CW=133.2f,CH=94;
 private final SharedPreferences prefs;
 private AudioManager audio;
 private volatile boolean loaded,disposed;private volatile String error;
 private boolean active;private long last;private int insetL,insetT,insetR,insetB;
 private float shower,ambient;private boolean pressed;
 private final Shader gold=new LinearGradient(0,0,0,65,new int[]{0xfffff5c9,0xffffdc6a,0xffbb650f,0xffffe68a},null,Shader.TileMode.MIRROR);

 public GoldenCloverView(Context c){super(c);setFocusable(true);prefs=c.getSharedPreferences("inferno-demo",0);g.ui.sound=prefs.getBoolean("sound",true);
  setOnApplyWindowInsetsListener((v,i)->{if(android.os.Build.VERSION.SDK_INT>=28&&i.getDisplayCutout()!=null){insetL=i.getDisplayCutout().getSafeInsetLeft();insetR=i.getDisplayCutout().getSafeInsetRight();insetT=i.getDisplayCutout().getSafeInsetTop();insetB=i.getDisplayCutout().getSafeInsetBottom();}fit();return i;});
  new Thread(()->{try{assets.load(c.getAssets());if(disposed){assets.release();return;}AudioManager a=new AudioManager(c);if(disposed){a.release();assets.release();return;}audio=a;audio.enabled(g.ui.sound);loaded=true;postInvalidate();}catch(Exception e){error=e.getMessage();postInvalidate();}},"inferno-assets").start();
 }
 private void fit(){layout.fit(getWidth(),getHeight(),insetL,insetT,insetR,insetB);}
 @Override protected void onSizeChanged(int w,int h,int ow,int oh){fit();}
 public void pause(){active=false;last=0;g.ui.auto=false;Choreographer.getInstance().removeFrameCallback(this);if(audio!=null)audio.pause();prefs.edit().putBoolean("sound",g.ui.sound).apply();}
 public void resume(){if(disposed||active)return;g.resetSession();active=true;last=0;if(audio!=null)audio.resume();Choreographer.getInstance().removeFrameCallback(this);Choreographer.getInstance().postFrameCallback(this);}
 public void dispose(){disposed=true;pause();if(audio!=null)audio.release();if(loaded)assets.release();}
 public boolean closePopup(){if(g.ui.betMenu||g.ui.menu||g.ui.featureMenu){g.ui.betMenu=g.ui.menu=g.ui.featureMenu=false;return true;}return false;}
 @Override public void doFrame(long frame){if(!active||disposed)return;float dt=last==0?0:Math.min(.05f,(frame-last)/1e9f);last=frame;
  if(loaded){if(!g.ui.menu&&!g.ui.betMenu&&!g.ui.featureMenu)g.update(dt);int ev=g.consumeEvents();if(audio!=null){audio.enabled(g.ui.sound);audio.events(ev);audio.loop(g.phase==GameState.Phase.SPIN&&g.reels.stopped<5||g.phase==GameState.Phase.RESPIN);}
   if((ev&GameState.COINS)!=0)particles.burst(360,820,g.win>=g.bet.value*10?90:28,1);
   if((ev&GameState.TRIGGER)!=0)for(int i=0;i<3;i++)if((g.bonus.mask&(1<<i))!=0)particles.burst(120+i*240,590,35,2);
   if((ev&GameState.DROP)!=0)particles.burst(360,720,50,0);
   shower+=dt;if((g.phase==GameState.Phase.WIN&&g.win>=g.bet.value*10||g.phase==GameState.Phase.BONUS_END)&&shower>.15f){particles.burst(360+280*(float)Math.sin(g.clock*5),300,8,1);shower=0;}particles.update(dt);ambient+=dt;if(ambient>.18f){particles.ambient();ambient=0;}
  }invalidate();Choreographer.getInstance().postFrameCallback(this);
 }
 private static String money(double v){return String.format(Locale.US,"$%,.2f",v);}
 private void bmp(Canvas c,Bitmap b,float x,float y,float w,float h,int alpha){if(b==null)return;p.setShader(null);p.setColor(Color.WHITE);p.setAlpha(Math.max(0,Math.min(255,alpha)));dst.set(x,y,x+w,y+h);c.drawBitmap(b,null,dst,p);p.setAlpha(255);}
 private void art(Canvas c,String name,float x,float y,float w,float h){bmp(c,assets.get(name),x,y,w,h,255);}
 private void rect(Canvas c,float x,float y,float w,float h,int color,float radius){p.setShader(null);p.setColor(color);p.setStyle(Paint.Style.FILL);dst.set(x,y,x+w,y+h);c.drawRoundRect(dst,radius,radius,p);}
 private void outline(Canvas c,float x,float y,float w,float h,int color,float line,float radius){p.setShader(null);p.setColor(color);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(line);dst.set(x,y,x+w,y+h);c.drawRoundRect(dst,radius,radius,p);p.setStyle(Paint.Style.FILL);}
 private void text(Canvas c,String s,float x,float y,float size,int color,boolean bold){p.setShader(null);p.setColor(color);p.setTextSize(size);p.setTypeface(bold?SERIF:SANS);p.setTextAlign(Paint.Align.CENTER);p.setShadowLayer(3,0,2,0xee100000);c.drawText(s,x,y,p);p.clearShadowLayer();}
 private void goldText(Canvas c,String s,float x,float y,float size){p.setTextSize(size);p.setTypeface(SERIF);p.setTextAlign(Paint.Align.CENTER);p.setColor(0xff481402);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(size*.075f);c.drawText(s,x,y,p);p.setStyle(Paint.Style.FILL);p.setShader(gold);c.drawText(s,x,y,p);p.setShader(null);}
 private void panel(Canvas c,float x,float y,float w,float h,int accent){rect(c,x-3,y-3,w+6,h+6,0xffd6a442,12);rect(c,x,y,w,h,0xff170407,9);outline(c,x+4,y+4,w-8,h-8,accent,2,7);rect(c,x+8,y+8,w-16,2,0x88fff2b5,1);}
 @Override protected void onDraw(Canvas c){super.onDraw(c);c.drawColor(0xff210106);if(!loaded){text(c,error==null?"CLOVER INFERNO • LOADING":"ASSET ERROR: "+error,getWidth()/2f,getHeight()/2f,20,GOLD,true);return;}
  Bitmap bg=assets.get("inferno_background");float cover=Math.max(getWidth()/(float)bg.getWidth(),getHeight()/(float)bg.getHeight());bmp(c,bg,(getWidth()-bg.getWidth()*cover)/2,(getHeight()-bg.getHeight()*cover)/2,bg.getWidth()*cover,bg.getHeight()*cover,110);
  c.save();c.translate(layout.left,layout.top);c.scale(layout.scale,layout.scale);art(c,"inferno_background",0,0,720,1280);
  jackpots(c);boolean bonus=g.phase==GameState.Phase.BONUS||g.phase==GameState.Phase.RESPIN||g.phase==GameState.Phase.BONUS_END;
  if(bonus)bonusScene(c);else{goldText(c,"CLOVER INFERNO",360,317,42);text(c,"25 LINES  •  THREE FIERY FEATURES",360,345,15,CREAM,false);trees(c);reels(c);}
  controls(c);if(g.phase==GameState.Phase.BOUNCE)triggerScene(c);
  if(g.phase==GameState.Phase.WIN&&g.win>=g.bet.value*10||g.phase==GameState.Phase.BONUS_END)celebration(c);
  drawParticles(c);if(g.ui.betMenu)betMenu(c);if(g.ui.featureMenu)featureMenu(c);if(g.ui.menu)menu(c);c.restore();
 }
 private void jackpots(Canvas c){panel(c,25,22,670,93,0xffff4840);text(c,"GRAND",360,47,17,0xffffae7a,true);goldText(c,money(g.grand),360,99,55);
  panel(c,25,130,670,80,0xffe654ff);text(c,"MAJOR",360,153,15,0xffff93fa,true);text(c,money(g.major),360,194,43,0xfffbe2ff,true);
  for(int i=0;i<3;i++){float x=25+i*232;panel(c,x,226,206,58,i==0?GOLD:i==1?COLORS[0]:COLORS[2]);text(c,i==0?"MAXI":i==1?"MINOR":"MINI",x+103,246,12,i==0?GOLD:i==1?COLORS[0]:COLORS[2],true);text(c,money(g.bet.value*(i==0?100:i==1?25:10)),x+103,272,24,CREAM,true);}
 }
 private void trees(Canvas c){for(int i=0;i<3;i++){float x=14+i*238;int a=(int)(32+20*Math.sin(g.clock*2+i));if(g.phase==GameState.Phase.BOUNCE&&(g.bonus.mask&(1<<i))!=0)a=180;p.setShader(new RadialGradient(x+113,540,120,new int[]{(a<<24)|(COLORS[i]&0xffffff),0x00ffffff},null,Shader.TileMode.CLAMP));dst.set(x,360,x+230,708);c.drawOval(dst,p);p.setShader(null);panel(c,x+8,698,210,40,COLORS[i]);text(c,FEATURES[i],x+113,725,23,COLORS[i],true);rect(c,x+27,734,173,3,0xff330910,1);rect(c,x+27,734,173*g.treeProgress[i]/100f,3,COLORS[i],1);}}
 private void symbol(Canvas c,int s,int color,float x,float y,float w,float h,boolean win){
  if(s==7){rect(c,x+5,y+h*.3f,w-10,h*.4f,0xff06326c,3);outline(c,x+5,y+h*.3f,w-10,h*.4f,GOLD,3,3);text(c,"BAR",x+w/2,y+h*.63f,w*.31f,CREAM,true);return;}
  if(s==8){p.setShader(new RadialGradient(x+w*.4f,y+h*.34f,w*.55f,new int[]{0xffee83ff,0xff9625d1,0xff33177c},null,Shader.TileMode.CLAMP));dst.set(x+w*.19f,y+h*.19f,x+w*.83f,y+h*.85f);c.drawOval(dst,p);p.setShader(null);p.setColor(0xff78d143);p.setStrokeWidth(4);c.drawLine(x+w*.48f,y+h*.23f,x+w*.61f,y+h*.08f,p);rect(c,x+w*.36f,y+h*.29f,w*.13f,h*.055f,0x99ffffff,8);return;}
  if(s==2){bmp(c,assets.get("glow"),x,y,w,h,80);bmp(c,win?assets.wins[s]:assets.symbols[s],x+4,y+3,w-8,h-6,255);float r=Math.min(w,h)*.12f;p.setColor(COLORS[((color%3)+3)%3]);c.drawCircle(x+w*.8f,y+h*.78f,r,p);return;}
  bmp(c,win?assets.wins[s]:assets.symbols[s],x,y,w,h,255);
 }
 private void reelFrame(Canvas c,float y,float h){panel(c,21,y-8,678,h+16,GOLD);rect(c,GX,y,CW*5,h,0xff800c1b,2);p.setShader(new LinearGradient(0,y,0,y+h,new int[]{0xffa01522,0xffb63323,0xff650515},null,Shader.TileMode.CLAMP));dst.set(GX,y,GX+CW*5,y+h);c.drawRect(dst,p);p.setShader(null);}
 private void reels(Canvas c){reelFrame(c,GY,CH*3);for(int col=0;col<5;col++){float x=GX+col*CW;c.save();c.clipRect(x+3,GY+2,x+CW-3,GY+CH*3-2);boolean moving=g.phase==GameState.Phase.SPIN&&g.reels.stopped<=col;
  if(moving){float progress=Math.max(0,Math.min(1,(g.time-.12f)/(.95f+col*.18f-.12f)));float distance=g.time<.12f?-12*(float)Math.sin(g.time/.12f*Math.PI):18*CH*AnimationManager.ease(progress);int step=(int)Math.floor(distance/CH);float offset=distance-step*CH;for(int r=-1;r<4;r++){int index=Math.max(0,Math.min(39,32+r-step));symbol(c,g.reels.strip[col][index],col%3,x+15,GY+r*CH+offset+5,CW-30,CH-10,false);}}
  else for(int r=0;r<3;r++){float landing=g.reels.landing[col],pop=(float)Math.sin(landing*8)*landing*.06f;boolean glow=g.reels.glow[col][r]&&(g.phase==GameState.Phase.WIN||g.phase==GameState.Phase.BOUNCE);if(glow){rect(c,x+3,GY+r*CH+2,CW-6,CH-4,0x55ffd04a,2);outline(c,x+4,GY+r*CH+3,CW-8,CH-6,GOLD,2,3);}float w=(CW-27)*(1+pop),h=(CH-10)*(1-pop);symbol(c,g.reels.symbols[col][r],g.reels.colors[col][r],x+(CW-w)/2,GY+r*CH+(CH-h)/2,w,h,glow);}
  c.restore();p.setColor(GOLD);p.setStrokeWidth(2);c.drawLine(x+CW,GY,x+CW,GY+CH*3,p);}
 }
 private void controls(Canvas c){rect(c,0,1049,720,231,0xdc170408,0);for(int i=0;i<3;i++){float x=24+i*233;panel(c,x,1060,206,63,i==1?GOLD:0xff936423);text(c,i==0?"CREDIT":i==1?"BET  ▾":"WIN",x+103,1079,12,GOLD,true);text(c,money(i==0?g.credit:i==1?g.bet.value:g.win),x+103,1109,25,CREAM,true);}
  button(c,23,1145,112,66,g.ui.auto?"AUTO ON":"AUTO",g.ui.auto?COLORS[2]:GOLD);button(c,148,1145,112,66,"RULES",GOLD);button(c,460,1145,112,66,g.ui.sound?"SOUND":"MUTED",GOLD);button(c,584,1145,112,66,"DEMO",COLORS[0]);
  p.setShader(new RadialGradient(360,1178,68,new int[]{0xffffe999,0xffe8a736,0xff934a0c},null,Shader.TileMode.CLAMP));c.drawCircle(360,1178,pressed?57:61,p);p.setShader(null);p.setColor(GOLD);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(4);c.drawCircle(360,1178,64,p);p.setStyle(Paint.Style.FILL);text(c,g.phase==GameState.Phase.SPIN?"•••":"SPIN",360,1187,27,0xff4f1603,true);
  text(c,g.ui.messageTime>0?g.ui.message:bonusActive()?g.bonus.featureName()+" • AUTOMATIC RESPINS":"25 LINES • FIVE CLOVERS START A FEATURE",360,1250,14,CREAM,false);text(c,"DEMO DOLLARS  •  NEW SESSION $2,000",360,1274,11,0xffcbad85,false);
 }
 private boolean bonusActive(){return g.phase==GameState.Phase.BONUS||g.phase==GameState.Phase.RESPIN||g.phase==GameState.Phase.BONUS_END||g.phase==GameState.Phase.BOUNCE;}
 private void button(Canvas c,float x,float y,float w,float h,String label,int color){panel(c,x,y,w,h,color);text(c,label,x+w/2,y+h/2+6,17,color,true);}
 private void triggerScene(Canvas c){rect(c,0,293,720,748,0xaa1c0205,0);float s=.7f+.3f*AnimationManager.ease(g.time/.45f);c.save();c.scale(s,s,360,620);bmp(c,assets.get("rays"),70,320,580,580,140);goldText(c,"FEATURE TRIGGERED",360,546,35);text(c,g.bonus.featureName(),360,611,29,CREAM,true);text(c,(g.bonus.cells==30?"DOUBLE REELS • ":"")+"3 RESPINS",360,660,23,GOLD,true);text(c,"NEW CLOVERS RESET THE COUNTER",360,708,17,CREAM,false);c.restore();}
 private void bonusScene(Canvas c){rect(c,0,292,720,753,0xd01b0308,0);goldText(c,g.bonus.featureName(),360,329,g.bonus.mask==7?25:31);text(c,g.bonus.full?"FULL SCREEN • DOUBLE HELD VALUES":g.bonus.remaining+" RESPINS REMAINING",360,365,23,CREAM,true);text(c,"FEATURE TOTAL  "+money(g.bonus.total),360,401,20,GOLD,true);
  float y=g.bonus.cells==30?457:702,h=g.bonus.cells==30?86:106;int rows=g.bonus.cells==30?6:3;reelFrame(c,y,h*rows);
  for(int board=0;board<g.bonus.cells/15;board++)for(int col=0;col<5;col++)for(int row=0;row<3;row++){int i=board*15+col*3+row;float x=GX+col*CW,cy=y+(board*3+row)*h;rect(c,x+2,cy+2,CW-4,h-4,0xff821321,2);if(g.bonus.values[i]>0){boolean blaze=i==g.bonus.blazeCell,bomb=g.bonus.bombCells[i];if(blaze||bomb)outline(c,x+4,cy+4,CW-8,h-8,blaze?COLORS[0]:COLORS[2],3,4);bmp(c,assets.symbols[2],x+38,cy+2,58,h*.6f,255);text(c,money(g.bonus.values[i]),x+CW/2,cy+h*.74f,20,CREAM,true);int tier=g.bonus.jackpot[i];if(tier>=0)text(c,BonusState.TIERS[tier]+" "+Math.min(3,g.bonus.tokens[tier])+"/3",x+CW/2,cy+h*.94f,11,COLORS[1],true);}
   else if(g.phase==GameState.Phase.RESPIN){c.save();c.clipRect(x+3,cy+3,x+CW-3,cy+h-3);float offset=(g.time*540+col*40)%h;bmp(c,assets.symbols[2],x+40,cy+offset-h,52,h*.7f,110);bmp(c,assets.symbols[2],x+40,cy+offset,52,h*.7f,110);c.restore();}else text(c,"·",x+CW/2,cy+h/2+5,29,0xffb54c4c,false);}
  if(g.bonus.cells==15){text(c,"LOCK • RESPIN • COLLECT",360,456,21,GOLD,true);text(c,"BLAZE grows a clover every respin",360,496,16,(g.bonus.mask&1)!=0?COLORS[0]:0xff907174,false);text(c,"BOMB pays a random 2 × 2 area, up to ×3",360,532,16,(g.bonus.mask&4)!=0?COLORS[2]:0xff907174,false);text(c,"Held clovers stay until the feature ends",360,568,16,CREAM,false);}
  text(c,g.bonus.message,360,g.bonus.cells==30?1028:666,17,GOLD,true);
 }
 private void celebration(Canvas c){rect(c,0,300,720,740,0xcc180307,0);float s=AnimationManager.ease(g.time/.45f);c.save();c.scale(s,s,360,690);bmp(c,assets.get("rays"),-40,260,800,800,150);String title=g.bonus.grand&&g.phase==GameState.Phase.BONUS_END?"GRAND JACKPOT":g.phase==GameState.Phase.BONUS_END?"FEATURE WIN":g.win>=g.bet.value*40?"MEGA WIN":"BIG WIN";goldText(c,title,360,610,title.length()>12?39:53);double shown=g.win*AnimationManager.ease(g.time/2.5f);goldText(c,money(shown),360,703,55);text(c,"ADDED TO YOUR DEMO CREDIT",360,763,17,CREAM,false);c.restore();}
 private void drawParticles(Canvas c){for(int i=0;i<ParticleSystem.MAX;i++)if(particles.life[i]>0){float a=particles.life[i]/particles.duration[i],s=particles.size[i];c.save();c.rotate(particles.angle[i],particles.x[i],particles.y[i]);bmp(c,assets.get(particles.type[i]==1?"coin":particles.type[i]>=2?"clover_clusters":"spark"),particles.x[i]-s/2,particles.y[i]-s/2,s,s,(int)(255*Math.min(1,a*2)));c.restore();}}
 private void modal(Canvas c,String title){rect(c,0,0,720,1280,0xdd100204,0);panel(c,38,300,644,680,GOLD);goldText(c,title,360,360,31);}
 private void betMenu(Canvas c){modal(c,"SELECT YOUR BET");for(int i=0;i<9;i++){float x=65+i%3*203,y=420+i/3*135;button(c,x,y,184,103,money(BetState.VALUES[i]),g.bet.value==BetState.VALUES[i]?COLORS[2]:GOLD);}text(c,"TAP OUTSIDE TO CLOSE",360,933,15,CREAM,false);}
 private void featureMenu(Canvas c){modal(c,"TRY A FREE FEATURE");text(c,"DEMO PREVIEW • NO CREDIT DEDUCTED",360,394,15,CREAM,false);String[] names={"BLAZE","JACKPOT","BOMB","ALL THREE"};int[] masks={1,2,4,7};for(int i=0;i<4;i++)button(c,91,439+i*110,538,86,names[i],i<3?COLORS[i]:GOLD);text(c,"TAP OUTSIDE TO CLOSE",360,933,15,CREAM,false);}
 private void menu(Canvas c){modal(c,"HOW TO PLAY");String[] lines={"25 paylines pay from left to right.","Match 3, 4 or 5 symbols. BAR is wild.","Five clovers trigger a feature.","BLAZE grows one locked clover each respin.","JACKPOT doubles the reels: collect 3 of a kind.","BOMB pays a random area, up to ×3.","New clovers reset the counter to 3.","A full screen doubles held clover values.","AUTO continues until stopped or credit is low.","DEMO lets you preview any feature for free.","Return to the app for a fresh $2,000."};for(int i=0;i<lines.length;i++)text(c,lines[i],360,410+i*35,17,i==3?COLORS[0]:i==4?COLORS[1]:i==5?COLORS[2]:CREAM,false);button(c,108,827,504,82,"RESET DEMO TO $2,000",COLORS[2]);text(c,"TAP OUTSIDE TO CLOSE",360,945,14,CREAM,false);}
 private boolean hit(float x,float y,float l,float t,float w,float h){return x>=l&&x<=l+w&&y>=t&&y<=t+h;}
 @Override public boolean onTouchEvent(MotionEvent e){if(!loaded)return true;float x=layout.x(e.getX()),y=layout.y(e.getY());if(e.getAction()==MotionEvent.ACTION_DOWN){pressed=hit(x,y,292,1110,136,136);invalidate();return true;}if(e.getAction()==MotionEvent.ACTION_CANCEL){pressed=false;return true;}if(e.getAction()!=MotionEvent.ACTION_UP)return true;pressed=false;g.events|=GameState.CLICK;
  if(g.ui.betMenu){for(int i=0;i<9;i++)if(hit(x,y,65+i%3*203,420+i/3*135,184,103)){g.selectBet(BetState.VALUES[i]);g.ui.betMenu=false;return true;}if(!hit(x,y,38,300,644,680))g.ui.betMenu=false;return true;}
  if(g.ui.featureMenu){int[] masks={1,2,4,7};for(int i=0;i<4;i++)if(hit(x,y,91,439+i*110,538,86)){g.ui.featureMenu=false;g.demoBonus(masks[i]);return true;}if(!hit(x,y,38,300,644,680))g.ui.featureMenu=false;return true;}
  if(g.ui.menu){if(hit(x,y,108,827,504,82)){g.resetSession();if(audio!=null)audio.loop(false);}else if(!hit(x,y,38,300,644,680))g.ui.menu=false;return true;}
  if(hit(x,y,292,1110,136,136))g.spin();else if(hit(x,y,257,1060,206,63)&&!g.busy()){g.ui.auto=false;g.ui.betMenu=true;}else if(hit(x,y,23,1145,112,66))g.toggleAuto();else if(hit(x,y,460,1145,112,66))g.ui.sound=!g.ui.sound;else if(hit(x,y,148,1145,112,66)){g.ui.auto=false;g.ui.menu=true;}else if(hit(x,y,584,1145,112,66)&&!g.busy()){g.ui.auto=false;g.ui.featureMenu=true;}performClick();return true;
 }
 @Override public boolean performClick(){super.performClick();return true;}
}
