package com.mohamadhh.goldenclover.game;
import java.util.Random;
/** Original adjustable demo model; visible features follow the published Inferno description. */
public final class BonusState {
 public static final int BLAZE=1,JACKPOT=2,BOMB=4;
 public static final String[] TIERS={"GRAND","MAJOR","MAXI","MINOR","MINI"};
 public final double[] values=new double[30],prizes=new double[5];
 public final int[] jackpot=new int[30],tokens=new int[5];public final boolean[] paid=new boolean[5],bombCells=new boolean[30];
 public int mask=1,cells=15,remaining=3,count,spins,blazeCell=-1,bombMultiplier;public double total,collected,jackpotTotal,lastBomb,lastBlaze;
 public boolean grand,full;public String message="";
 public void clear(){for(int i=0;i<30;i++){values[i]=0;jackpot[i]=-1;bombCells[i]=false;}for(int i=0;i<5;i++){tokens[i]=0;paid[i]=false;}remaining=3;count=spins=0;total=collected=jackpotTotal=lastBomb=lastBlaze=0;blazeCell=-1;bombMultiplier=0;grand=full=false;message="";}
 public void lock(int i,Random random,int bet){if(i>=cells||values[i]>0)return;int v=random.nextInt(1000);double mult=v<550?.5:v<800?1:v<930?2:v<980?5:10;values[i]=mult*bet;count++;
  if((mask&JACKPOT)!=0&&random.nextFloat()<.13f){int n=random.nextInt(1000);int tier=n<1?0:n<12?1:n<92?2:n<350?3:4;jackpot[i]=tier;tokens[tier]++;if(tokens[tier]>=3&&!paid[tier]){paid[tier]=true;jackpotTotal+=prizes[tier];if(tier==0)grand=true;message=TIERS[tier]+" JACKPOT!";}}
 }
 public void begin(int[][] grid,Random random,int bet){begin(grid,random,bet,BLAZE,10000,500);}
 public void begin(int[][] grid,Random random,int bet,int features,double grandPrize,double majorPrize){mask=features;cells=(mask&JACKPOT)!=0?30:15;clear();prizes[0]=grandPrize;prizes[1]=majorPrize;prizes[2]=100*bet;prizes[3]=25*bet;prizes[4]=10*bet;for(int c=0;c<5;c++)for(int r=0;r<3;r++)if(grid[c][r]==2)lock(c*3+r,random,bet);if((mask&BLAZE)!=0&&count>0){int n=random.nextInt(count);for(int i=0;i<cells;i++)if(values[i]>0&&n--==0){blazeCell=i;break;}}calculate();}
 public boolean respin(Random random,int bet){if(remaining<=0)return false;boolean added=false;spins++;lastBomb=lastBlaze=0;bombMultiplier=0;for(int i=0;i<cells;i++)bombCells[i]=false;
  if(blazeCell>=0){lastBlaze=bet*(random.nextFloat()<.75f?.5:1.0);values[blazeCell]+=lastBlaze;message="BLAZE +$"+String.format(java.util.Locale.US,"%.2f",lastBlaze);}
  for(int i=0;i<cells;i++)if(values[i]==0&&random.nextFloat()<(cells==30?.035f:.07f)){lock(i,random,bet);added=true;}
  if((mask&BOMB)!=0&&random.nextFloat()<.22f){int board=random.nextInt(cells/15),col=random.nextInt(4),row=random.nextInt(2),v=random.nextInt(10);bombMultiplier=v<6?1:v<9?2:3;for(int c=col;c<col+2;c++)for(int r=row;r<row+2;r++){int i=board*15+c*3+r;bombCells[i]=true;lastBomb+=values[i]*bombMultiplier;}collected+=lastBomb;if(lastBomb>0)message="BOMB ×"+bombMultiplier+" +$"+String.format(java.util.Locale.US,"%.2f",lastBomb);}
  remaining=added?3:remaining-1;full=count==cells;if(full||spins>=80)remaining=0;calculate();return added;
 }
 public void calculate(){total=collected+jackpotTotal;double held=0;for(int i=0;i<cells;i++)held+=values[i];total+=held*(full?2:1);total=Math.round(total*100)/100.0;}
 public String featureName(){String s="";if((mask&BLAZE)!=0)s="BLAZE";if((mask&JACKPOT)!=0)s+=(s.isEmpty()?"":" + ")+"JACKPOT";if((mask&BOMB)!=0)s+=(s.isEmpty()?"":" + ")+"BOMB";return s;}
}
