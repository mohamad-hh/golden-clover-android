package com.mohamadhh.goldenclover.game;
public final class BetState {
 public static final int[] VALUES={5,10,20,35,50,75,100,150,200};
 public int value=20;
 public boolean select(int v){for(int b:VALUES)if(b==v){value=v;return true;}return false;}
}
