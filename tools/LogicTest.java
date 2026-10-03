import com.mohamadhh.goldenclover.game.*;
import java.util.Random;
public final class LogicTest {
 static int checks;
 static void check(boolean b,String s){checks++;if(!b)throw new AssertionError(s);}
 static void tick(GameState g,float seconds){for(int i=0;i<Math.ceil(seconds/.02f);i++)g.update(.02f);}
 public static void main(String[] args){
  GameState g=new GameState(145);check(g.credit==1000,"initial balance");check(g.selectBet(35)&&g.bet.value==35,"bet selection");check(!g.selectBet(37),"reject invalid bet");check(g.spin()&&g.credit==965,"spin debit");check(!g.spin(),"busy double tap");check(!g.selectBet(100),"bet cannot change during spin");tick(g,3);check(g.reels.stopped==5,"all reels stop");
  GameState low=new GameState(3);low.credit=4;low.ui.auto=true;check(!low.spin()&&!low.ui.auto&&low.credit==4,"insufficient disables auto without debit");
  GameState auto=new GameState(4);auto.toggleAuto();check(auto.ui.auto&&auto.phase==GameState.Phase.SPIN,"auto starts spin");auto.toggleAuto();check(!auto.ui.auto,"auto toggles off during spin");
  int[][] grid={{1,3,4},{1,4,3},{1,6,7},{1,7,0},{1,0,3}};boolean[][] glow=new boolean[5][3];check(PayoutLogic.evaluate(grid,20,glow)>=1500,"five sevens pays");check(glow[4][0],"winning symbol highlighted");
  GameState bonus=new GameState(15);bonus.credit=10000;check(bonus.buyBonus()&&bonus.bonus.count==5&&bonus.bonus.remaining==3,"bonus initializes 5 held values");check(bonus.phase==GameState.Phase.BOUNCE,"bounce precedes board");tick(bonus,3.4f);check(bonus.phase==GameState.Phase.BONUS,"bonus transition");
  BonusState b=new BonusState();b.clear();Random add=new Random(){@Override public float nextFloat(){return 0;}@Override public int nextInt(int bound){return bound-1;}};b.remaining=1;check(b.respin(add,20)&&b.full&&b.count==15&&b.remaining==0,"full board celebration");
  b.clear();Random none=new Random(){@Override public float nextFloat(){return 1;}};b.values[0]=20;b.count=1;check(!b.respin(none,20)&&b.remaining==2,"no clover decrements");b.respin(none,20);b.respin(none,20);check(b.remaining==0&&b.total==20,"bonus end sums locked values");
  b.clear();b.remaining=1;Random one=new Random(){int calls;@Override public float nextFloat(){return calls++==0?0:1;}@Override public int nextInt(int bound){return bound-1;}};check(b.respin(one,20)&&b.remaining==3&&b.count==1,"new clover resets respins");
  ResponsiveLayout l=new ResponsiveLayout();int[][] sizes={{1920,1080},{2160,1080},{2340,1080},{2400,1080}};for(int[] s:sizes){l.fit(s[0],s[1],38,0,38,24);check(l.left>=38&&l.top>=0&&l.left+1920*l.scale<=s[0]-38+.01&&l.top+1080*l.scale<=s[1]-24+.01,"safe viewport "+s[0]);check(Math.abs(l.x(l.left+568*l.scale)-568)<.01,"touch mapping");}
  GameState big=new GameState(4);big.selectBet(20);big.spin();for(int c=0;c<5;c++)for(int r=0;r<3;r++)big.reels.symbols[c][r]=1;tick(big,2.8f);check(big.phase==GameState.Phase.WIN&&big.win>=200,"big win sequence");check((big.consumeEvents()&GameState.BIG)!=0,"big win audio event");
  GameState trigger=new GameState(4);trigger.spin();for(int c=0;c<5;c++)for(int r=0;r<3;r++)trigger.reels.symbols[c][r]=3;for(int c=0;c<5;c++)trigger.reels.symbols[c][0]=2;tick(trigger,2.8f);check(trigger.phase==GameState.Phase.BOUNCE,"five bonus symbols trigger");tick(trigger,120);check(trigger.phase==GameState.Phase.IDLE,"bonus eventually ends");
  System.out.println("PASS: "+checks+" gameplay/layout assertions");
  Random random=new Random(77225);ReelLogic reels=new ReelLogic();BonusState bonusSim=new BonusState();int hits=0,features=0;double total=0;int n=200000;
  for(int i=0;i<n;i++){reels.generate(random);double pay=PayoutLogic.evaluate(reels.symbols,20,reels.glow);if(reels.bonusCount()>=5){features++;bonusSim.begin(reels.symbols,random,20);while(bonusSim.remaining>0)bonusSim.respin(random,20);pay+=bonusSim.total;}if(pay>0)hits++;total+=pay;}
  System.out.printf("Seeded %d-spin diagnostic: RTP %.3f%%, hit %.3f%%, bonus %.3f%% (not certified)\n",n,total/(n*20)*100,hits*100.0/n,features*100.0/n);
 }
}
