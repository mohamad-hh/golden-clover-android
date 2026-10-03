package com.mohamadhh.goldenclover.game;
public final class PayoutLogic {
 public static final int[][] LINES={{1,1,1,1,1},{0,0,0,0,0},{2,2,2,2,2},{0,1,2,1,0},{2,1,0,1,2},{0,0,1,2,2},{2,2,1,0,0},{1,0,0,0,1},{1,2,2,2,1},{0,1,1,1,0},{2,1,1,1,2},{1,0,1,2,1},{1,2,1,0,1},{0,1,0,1,0},{2,1,2,1,2},{0,2,0,2,0},{2,0,2,0,2},{0,0,2,0,0},{2,2,0,2,2},{1,1,0,1,1}};
 // Total bet multipliers for 3/4/5 matching symbols; clover/pot pay through bonus.
 public static final int ACTIVE_LINES=7;
 public static final double PAY_SCALE=5.2;
 public static final double[][] PAY={{1.5,6,30},{3,12,75},{0,0,0},{.4,1.5,8},{.4,1.5,8},{.6,2,12},{.8,3,15},{2,8,50},{0,0,0}};
 public static double evaluate(int[][] grid,int bet,boolean[][] glow){
  double total=0;for(int c=0;c<5;c++)for(int r=0;r<3;r++)glow[c][r]=false;
  for(int li=0;li<ACTIVE_LINES;li++){int[] line=LINES[li];int s=grid[0][line[0]],n=1;while(n<5&&grid[n][line[n]]==s)n++;if(n>=3&&PAY[s][n-3]>0){total+=bet*PAY[s][n-3];for(int c=0;c<n;c++)glow[c][line[c]]=true;}}
  return Math.round(total*PAY_SCALE*100)/100.0;
 }
}
