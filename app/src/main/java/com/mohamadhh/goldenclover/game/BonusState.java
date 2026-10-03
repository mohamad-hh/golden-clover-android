package com.mohamadhh.goldenclover.game;
import java.util.Random;
public final class BonusState {
 public final double[] values=new double[15]; public final int[] jackpot=new int[15];
 public int remaining=3,count; public double total; public boolean grand,full;
 public void clear(){for(int i=0;i<15;i++){values[i]=0;jackpot[i]=-1;}remaining=3;count=0;total=0;grand=false;full=false;}
 public void lock(int i,Random random,int bet){if(values[i]>0)return;int v=random.nextInt(1000);int[] amounts={5,10,20,50,100,200,300,500};int p=v<550?0:v<780?1:v<895?2:v<955?3:v<980?4:v<990?5:v<996?6:7;values[i]=amounts[p]*bet/20.0;int j=random.nextInt(100000000);if(j<1){jackpot[i]=0;values[i]=1245681;grand=true;}else if(j<12){jackpot[i]=1;values[i]=70584;}else if(j<1800){jackpot[i]=2;values[i]=1250;}else if(j<11000){jackpot[i]=3;values[i]=500;}count++;}
 public void begin(int[][] grid,Random random,int bet){clear();for(int c=0;c<5;c++)for(int r=0;r<3;r++)if(grid[c][r]==2||grid[c][r]==8)lock(c*3+r,random,bet);}
 public boolean respin(Random random,int bet){boolean added=false;for(int i=0;i<15;i++)if(values[i]==0&&random.nextFloat()<.075f){lock(i,random,bet);added=true;}remaining=added?3:remaining-1;full=count==15;if(full)remaining=0;total=0;for(double v:values)total+=v;return added;}
}
