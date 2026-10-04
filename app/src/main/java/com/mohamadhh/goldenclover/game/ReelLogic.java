package com.mohamadhh.goldenclover.game;
import java.util.Random;
public final class ReelLogic {
 public final int[][] symbols=new int[5][3],colors=new int[5][3],strip=new int[5][40],previous=new int[5][3];
 public final boolean[][] glow=new boolean[5][3];
 public final float[] landing=new float[5];public int spinNumber,stopped;
 // Bell, seven, clover, lemon, orange, grapes, melon, BAR (wild), plum.
 public static int symbol(Random r){int v=r.nextInt(1000);if(v<80)return 0;if(v<140)return 1;if(v<240)return 2;if(v<415)return 3;if(v<590)return 4;if(v<740)return 5;if(v<870)return 6;if(v<915)return 7;return 8;}
 public void generate(Random r){stopped=0;spinNumber++;for(int c=0;c<5;c++){for(int i=0;i<40;i++)strip[c][i]=((i*7+c*5+spinNumber)%9+9)%9;for(int y=0;y<3;y++){previous[c][y]=symbols[c][y];symbols[c][y]=symbol(r);colors[c][y]=r.nextInt(3);glow[c][y]=false;strip[c][32+y]=previous[c][y];strip[c][14+y]=symbols[c][y];}}}
 public int bonusCount(){int n=0;for(int[] col:symbols)for(int s:col)if(s==2)n++;return n;}
 public int featureMask(){int[] counts=new int[3];for(int c=0;c<5;c++)for(int r=0;r<3;r++)if(symbols[c][r]==2)counts[colors[c][r]]++;int mask=0;for(int i=0;i<3;i++)if(counts[i]>=2)mask|=1<<i;if(mask==0){for(int i=0;i<3;i++)if(counts[i]>0){mask=1<<i;break;}}return mask==0?1:mask;}
 public void update(float dt,float time){for(int c=0;c<5;c++){if(time>=.95f+c*.18f&&stopped<=c){stopped=c+1;landing[c]=1;}landing[c]=Math.max(0,landing[c]-dt*3.8f);}}
}
