package com.mohamadhh.goldenclover.game;
import java.util.Random;
public final class GameState {
 public enum Phase{IDLE,SPIN,WIN,BOUNCE,BONUS,RESPIN,BONUS_END}
 public Phase phase=Phase.IDLE;public float time,clock;public double credit=2000,win,grand=10000,major=500;public final BetState bet=new BetState();public final UIState ui=new UIState();public final ReelLogic reels=new ReelLogic();public final BonusState bonus=new BonusState();public final Random random;public int events,tree;public final int[] treeProgress=new int[3];
 public static final int CLICK=1,START=2,STOP0=4,NORMAL=128,MEDIUM=256,BIG=512,TRIGGER=1024,DROP=2048,RESET=4096,JACKPOT=8192,COINS=16384;
 public GameState(long seed){random=new Random(seed);reels.generate(random);reels.stopped=5;}
 public void resetSession(){phase=Phase.IDLE;time=0;credit=2000;win=0;grand=10000;major=500;ui.auto=false;ui.menu=ui.betMenu=ui.featureMenu=false;ui.messageTime=0;events=0;bonus.clear();for(int i=0;i<3;i++)treeProgress[i]=0;reels.generate(random);reels.stopped=5;}
 public boolean busy(){return phase!=Phase.IDLE;}
 public boolean spin(){if(busy())return false;if(credit<bet.value){ui.auto=false;notice("LOW CREDIT • RESET DEMO IN RULES");return false;}credit=Math.round((credit-bet.value)*100)/100.0;grand+=bet.value*.005;major+=bet.value*.002;win=0;reels.generate(random);enter(Phase.SPIN);events|=START;return true;}
 public boolean selectBet(int v){return !busy()&&bet.select(v);}
 public void toggleAuto(){if(!ui.auto&&busy()&&phase!=Phase.SPIN&&phase!=Phase.WIN)return;ui.auto=!ui.auto;if(ui.auto&&!busy())spin();}
 public boolean buyBonus(){return demoBonus(1+random.nextInt(7));}
 public boolean demoBonus(int mask){if(busy()||mask<1||mask>7)return false;ui.auto=false;win=0;for(int c=0;c<5;c++)for(int r=0;r<3;r++)reels.symbols[c][r]=3;for(int c=0;c<5;c++)reels.symbols[c][c%3]=2;beginBonus(mask);return true;}
 public void notice(String s){ui.message=s;ui.messageTime=3;}
 private void enter(Phase p){phase=p;time=0;}
 private void beginBonus(int mask){for(int c=0;c<5;c++)for(int r=0;r<3;r++)reels.glow[c][r]=reels.symbols[c][r]==2;bonus.begin(reels.symbols,random,bet.value,mask,grand,major);tree=(mask&1)!=0?0:(mask&2)!=0?1:2;enter(Phase.BOUNCE);events|=TRIGGER;}
 public void update(float dt){if(dt<0)return;clock+=dt;time+=dt;ui.messageTime=Math.max(0,ui.messageTime-dt);ui.popup=ui.betMenu||ui.featureMenu?Math.min(1,ui.popup+dt*6):0;
  switch(phase){
   case IDLE:if(ui.auto&&time>.65f)spin();break;
   case SPIN:int old=reels.stopped;reels.update(dt,time);for(int c=old;c<reels.stopped;c++)events|=STOP0<<c;if(time>1.95f){double pay=PayoutLogic.evaluate(reels.symbols,bet.value,reels.glow);credit+=pay;for(int c=0;c<5;c++)for(int r=0;r<3;r++)if(reels.symbols[c][r]==2)treeProgress[reels.colors[c][r]]=Math.min(100,treeProgress[reels.colors[c][r]]+6);if(reels.bonusCount()>=5)beginBonus(reels.featureMask());else{win=pay;if(win>0){events|=win>=bet.value*10?BIG:win>=bet.value*3?MEDIUM:NORMAL;events|=COINS;enter(Phase.WIN);}else enter(Phase.IDLE);}}break;
   case WIN:if(time>(win>=bet.value*10?3.5f:1.2f))enter(Phase.IDLE);break;
   case BOUNCE:if(time-dt<1.1f&&time>=1.1f)events|=DROP;if(time>2.2f){enter(Phase.BONUS);events|=COINS;}break;
   case BONUS:if(time>1.05f){enter(Phase.RESPIN);events|=START;}break;
   case RESPIN:if(time>1.15f){boolean added=bonus.respin(random,bet.value);events|=STOP0;if(added)events|=RESET|COINS;if(bonus.lastBomb>0)events|=DROP|COINS;if(bonus.remaining==0){win=bonus.total;credit+=win;if(bonus.paid[0])grand=10000;if(bonus.paid[1])major=500;for(int i=0;i<3;i++)if((bonus.mask&(1<<i))!=0)treeProgress[i]=0;events|=bonus.grand?JACKPOT:BIG;events|=COINS;enter(Phase.BONUS_END);}else enter(Phase.BONUS);}break;
   case BONUS_END:if(time>(bonus.grand?7:4.5f))enter(Phase.IDLE);break;
  }
 }
 public int consumeEvents(){int e=events;events=0;return e;}
}
