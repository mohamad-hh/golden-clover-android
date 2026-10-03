package com.mohamadhh.goldenclover.game;
import java.util.Random;
public final class GameState {
 public enum Phase{IDLE,SPIN,WIN,BOUNCE,BONUS,RESPIN,BONUS_END}
 public Phase phase=Phase.IDLE;public float time,clock;public double credit=1000,win,grand=1245681,major=70584;
 public final BetState bet=new BetState(); public final UIState ui=new UIState();
 public final ReelLogic reels=new ReelLogic();public final BonusState bonus=new BonusState();
 public final Random random;public int events;public int tree;private double baseWin;
 public static final int CLICK=1,START=2,STOP0=4,NORMAL=128,MEDIUM=256,BIG=512,TRIGGER=1024,DROP=2048,RESET=4096,JACKPOT=8192,COINS=16384;
 public GameState(long seed){random=new Random(seed);reels.generate(random);reels.stopped=5;}
 public boolean busy(){return phase!=Phase.IDLE;}
 public boolean spin(){if(busy())return false;if(credit<bet.value){ui.auto=false;notice("INSUFFICIENT VIRTUAL CREDIT");return false;}credit-=bet.value;win=0;reels.generate(random);enter(Phase.SPIN);events|=START;return true;}
 public boolean selectBet(int v){return !busy()&&bet.select(v);}
 public void toggleAuto(){ui.auto=!ui.auto;if(ui.auto&&!busy())spin();}
 public boolean buyBonus(){if(busy())return false;if(credit<20*bet.value){notice("BONUS COSTS 20 × BET");return false;}credit-=20*bet.value;win=0;for(int c=0;c<5;c++)for(int r=0;r<3;r++)reels.symbols[c][r]=3;for(int i=0;i<5;i++)reels.symbols[i][i%3]=2;beginBonus();return true;}
 public void notice(String s){ui.message=s;ui.messageTime=3;}
 private void enter(Phase p){phase=p;time=0;}
 private void beginBonus(){bonus.begin(reels.symbols,random,bet.value);tree=random.nextInt(3);enter(Phase.BOUNCE);events|=TRIGGER;}
 public void update(float dt){clock+=dt;time+=dt;grand+=dt*.27;major+=dt*.08;ui.messageTime=Math.max(0,ui.messageTime-dt);ui.popup=ui.betMenu?Math.min(1,ui.popup+dt*6):0;
  switch(phase){
   case IDLE:if(ui.auto&&time>.7f)spin();break;
   case SPIN:int old=reels.stopped;reels.update(dt,time);for(int c=old;c<reels.stopped;c++)events|=STOP0<<c;if(time>2.65f){baseWin=PayoutLogic.evaluate(reels.symbols,bet.value,reels.glow);if(reels.bonusCount()>=5){credit+=baseWin;beginBonus();}else{win=baseWin;credit+=win;if(win>0){events|=win>=bet.value*10?BIG:win>=bet.value*3?MEDIUM:NORMAL;events|=COINS;enter(Phase.WIN);}else enter(Phase.IDLE);}}break;
   case WIN:if(time>(win>=bet.value*10?4.5f:2.0f))enter(Phase.IDLE);break;
   case BOUNCE:if(time-dt<1.85f&&time>=1.85f)events|=DROP;if(time>3.2f){enter(Phase.BONUS);events|=COINS;}break;
   case BONUS:if(time>1.2f){enter(Phase.RESPIN);events|=START;}break;
   case RESPIN:if(time>1.4f){boolean added=bonus.respin(random,bet.value);events|=STOP0;if(added){events|=RESET|COINS;}if(bonus.remaining==0){win=bonus.total;credit+=win;events|=bonus.grand?JACKPOT:BIG;events|=COINS;enter(Phase.BONUS_END);}else enter(Phase.BONUS);}break;
   case BONUS_END:if(time>(bonus.grand?9:5))enter(Phase.IDLE);break;
  }
 }
 public int consumeEvents(){int e=events;events=0;return e;}
}
