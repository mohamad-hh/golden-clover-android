package com.mohamadhh.goldenclover.game;
import java.util.Random;
public final class ReelLogic {
 public final int[][] symbols=new int[5][3];
 public final boolean[][] glow=new boolean[5][3];
 public final float[] landing=new float[5];
 public int stopped;
 public static int symbol(Random r){int v=r.nextInt(1000);if(v<125)return 0;if(v<205)return 1;if(v<253)return 2;if(v<443)return 3;if(v<633)return 4;if(v<783)return 5;if(v<913)return 6;if(v<983)return 7;return 8;}
 public void generate(Random r){stopped=0;for(int c=0;c<5;c++)for(int y=0;y<3;y++){symbols[c][y]=symbol(r);glow[c][y]=false;}}
 public int bonusCount(){int n=0;for(int[] col:symbols)for(int s:col)if(s==2||s==8)n++;return n;}
 public void update(float dt,float time){for(int c=0;c<5;c++){if(time>=1.25f+c*.22f&&stopped<=c){stopped=c+1;landing[c]=1;}landing[c]=Math.max(0,landing[c]-dt*2.8f);}}
}
