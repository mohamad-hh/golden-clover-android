package com.mohamadhh.goldenclover.game;
public final class PayoutLogic {
 public static final int[][] LINES={{1,1,1,1,1},{0,0,0,0,0},{2,2,2,2,2},{0,1,2,1,0},{2,1,0,1,2},{0,0,1,2,2},{2,2,1,0,0},{1,0,0,0,1},{1,2,2,2,1},{0,1,1,1,0},{2,1,1,1,2},{1,0,1,2,1},{1,2,1,0,1},{0,1,0,1,0},{2,1,2,1,2},{0,2,0,2,0},{2,0,2,0,2},{0,0,2,0,0},{2,2,0,2,2},{1,1,0,1,1},{1,1,2,1,1},{0,1,2,2,2},{2,1,0,0,0},{0,2,2,2,0},{2,0,0,0,2}};
 public static final int ACTIVE_LINES=25;public static final double PAY_SCALE=1.1853;
 // Per-line awards as fractions of total bet. Custom demo mathematics.
 public static final double[][] PAY={{.8,3,12},{1.2,5,20},{0,0,0},{.2,.6,3},{.2,.6,3},{.3,1,5},{.4,1.5,6},{2,8,30},{.3,1,4}};
 public static double evaluate(int[][] grid,int bet,boolean[][] glow){double total=0;for(int c=0;c<5;c++)for(int r=0;r<3;r++)glow[c][r]=false;
  for(int[] line:LINES){int bestSymbol=-1,bestCount=0;double best=0;for(int s=0;s<PAY.length;s++){if(s==2)continue;int n=0;while(n<5&&(grid[n][line[n]]==s||grid[n][line[n]]==7))n++;if(n>=3){double pay=PAY[s][n-3];if(pay>best){best=pay;bestCount=n;bestSymbol=s;}}}if(bestSymbol>=0){total+=bet*best;for(int c=0;c<bestCount;c++)glow[c][line[c]]=true;}}
  return Math.round(total*PAY_SCALE*100)/100.0;
 }
}
