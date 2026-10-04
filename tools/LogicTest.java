import com.mohamadhh.goldenclover.game.*;
import java.util.Random;
import java.util.HashSet;
import java.util.Arrays;
public final class LogicTest {
 static int checks;
 static void check(boolean b,String s){checks++;if(!b)throw new AssertionError(s);}
 static void tick(GameState g,float seconds){for(int i=0;i<Math.ceil(seconds/.02f);i++)g.update(.02f);}
 static int[][] seedGrid(){int[][] grid=new int[5][3];for(int c=0;c<5;c++){Arrays.fill(grid[c],3);grid[c][c%3]=2;}return grid;}
 public static void main(String[] args){
  GameState g=new GameState(145);check(g.credit==2000,"initial demo balance");check(g.selectBet(35)&&g.bet.value==35,"bet selection");check(!g.selectBet(37),"invalid bet rejected");check(g.spin()&&g.credit==1965,"exactly one spin debit");check(!g.spin(),"busy double tap ignored");check(!g.selectBet(100),"cannot change bet during spin");tick(g,2.1f);check(g.reels.stopped==5,"five reels stop");
  g.resetSession();check(g.credit==2000&&g.phase==GameState.Phase.IDLE&&g.win==0&&!g.ui.auto,"foreground entry resets credit and in-flight play");
  GameState low=new GameState(3);low.credit=.5;low.ui.auto=true;check(!low.spin()&&!low.ui.auto&&low.credit==.5,"insufficient balance stops auto without debit");
  GameState auto=new GameState(4);auto.toggleAuto();check(auto.ui.auto&&auto.phase==GameState.Phase.SPIN,"auto starts play");auto.toggleAuto();check(!auto.ui.auto,"auto may be stopped during spin");
  check(PayoutLogic.LINES.length==25&&PayoutLogic.ACTIVE_LINES==25,"25 lines");HashSet<String> lines=new HashSet<>();for(int[] line:PayoutLogic.LINES){check(lines.add(Arrays.toString(line)),"unique line");for(int row:line)check(row>=0&&row<3,"line within reels");}
  int[][] grid={{1,3,4},{7,4,3},{1,6,8},{1,8,0},{1,0,3}};boolean[][] glow=new boolean[5][3];check(PayoutLogic.evaluate(grid,5,glow)>=100,"BAR substitutes for seven");check(glow[4][0],"line win highlights");
  for(int mask=1;mask<=7;mask++){GameState demo=new GameState(mask);double before=demo.credit;check(demo.demoBonus(mask)&&demo.credit==before,"free demo has no debit, mask "+mask);check(demo.bonus.count==5&&demo.bonus.remaining==3,"five starting clovers");check(demo.bonus.cells==((mask&2)!=0?30:15),"jackpot doubles board");check(demo.phase==GameState.Phase.BOUNCE,"feature intro");tick(demo,2.3f);check(demo.phase==GameState.Phase.BONUS,"intro transitions to bonus");tick(demo,250);check(demo.phase==GameState.Phase.IDLE,"feature always completes");check(Math.abs(demo.credit-before-demo.win)<.001,"final payout credited once");}
  Random none=new Random(){@Override public float nextFloat(){return 1;}};
  BonusState b=new BonusState();b.begin(seedGrid(),new Random(44),5,1,10000,500);int selected=b.blazeCell;double prior=b.values[selected];b.respin(none,5);check(b.values[selected]>prior&&b.lastBlaze>0,"Blaze grows selected held clover");check(b.remaining==2,"empty spin uses one respin");b.respin(none,5);b.respin(none,5);check(b.remaining==0&&b.total>0,"no new value ends feature");
  b.begin(seedGrid(),new Random(44),5,2,11111,777);check(b.prizes[0]==11111&&b.prizes[1]==777,"progressive snapshot matches meters");b.tokens[4]=2;Random mini=new Random(){@Override public float nextFloat(){return 0;}@Override public int nextInt(int bound){return bound-1;}};b.lock(1,mini,5);check(b.paid[4]&&b.jackpotTotal==50,"three mini tokens award jackpot");b.lock(2,mini,5);check(b.jackpotTotal==50,"same jackpot paid once per feature");
  b.clear();b.mask=4;b.cells=15;for(int i=0;i<15;i++){b.values[i]=10;b.count++;}Random bomb=new Random(){@Override public float nextFloat(){return 0;}@Override public int nextInt(int bound){return 0;}};b.respin(bomb,5);check(b.lastBomb==40&&b.collected==40,"Bomb pays a two-by-two region");check(b.total==340&&b.full&&b.remaining==0,"full board doubles held values, not collected Bomb payment");
  b.begin(seedGrid(),new Random(3),5,4,10000,500);b.remaining=1;Random add=new Random(){int calls;@Override public float nextFloat(){return calls++==0?0:1;}@Override public int nextInt(int bound){return bound-1;}};check(b.respin(add,5)&&b.remaining==3,"new clover resets remaining spins");
  ResponsiveLayout l=new ResponsiveLayout();int[][] sizes={{720,1280},{720,1440},{720,1560},{720,1600},{1080,2400}};for(int[] s:sizes){l.fit(s[0],s[1],0,32,0,24);check(l.left>=0&&l.top>=32&&l.left+720*l.scale<=s[0]+.01&&l.top+1280*l.scale<=s[1]-24+.01,"cutout-safe portrait "+s[1]);check(Math.abs(l.x(l.left+360*l.scale)-360)<.01&&Math.abs(l.y(l.top+1178*l.scale)-1178)<.01,"inverse touch mapping");}
  GameState trigger=new GameState(4);trigger.spin();int[][] seeded=seedGrid();for(int c=0;c<5;c++)System.arraycopy(seeded[c],0,trigger.reels.symbols[c],0,3);tick(trigger,2.1f);check(trigger.phase==GameState.Phase.BOUNCE,"five clovers naturally trigger");
  System.out.println("PASS: "+checks+" gameplay/layout assertions");
  Random random=new Random(77225);ReelLogic reels=new ReelLogic();BonusState sim=new BonusState();int hits=0,features=0,n=400000;double lineTotal=0,bonusTotal=0,sum2=0;int[] masks=new int[8];
  for(int i=0;i<n;i++){reels.generate(random);double pay=PayoutLogic.evaluate(reels.symbols,5,reels.glow);lineTotal+=pay;if(reels.bonusCount()>=5){features++;int mask=reels.featureMask();masks[mask]++;sim.begin(reels.symbols,random,5,mask,10000,500);while(sim.remaining>0)sim.respin(random,5);pay+=sim.total;bonusTotal+=sim.total;}if(pay>0)hits++;sum2+=pay*pay;}
  double mean=(lineTotal+bonusTotal)/n,variance=sum2/n-mean*mean;
  System.out.printf(java.util.Locale.US,"Seed 77225; %,d spins; RTP %.3f%% (lines %.3f%% + features %.3f%%); hit %.3f%%; feature %.3f%%; approximate 95%% interval ±%.3f points.%n",n,mean/5*100,lineTotal/(n*5)*100,bonusTotal/(n*5)*100,hits*100.0/n,features*100.0/n,1.96*Math.sqrt(variance/n)/5*100);
  System.out.println("Feature masks 1..7: "+Arrays.toString(masks)+"; custom demo mathematics, free previews excluded.");
 }
}
