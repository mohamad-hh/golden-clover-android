package com.mohamadhh.goldenclover.game;
public final class AnimationManager {
 public static float ease(float t){t=Math.max(0,Math.min(1,t));return 1-(1-t)*(1-t)*(1-t);}
 public static float bounce(float t){t=Math.max(0,Math.min(1,t));if(t<1/2.75f)return 7.5625f*t*t;if(t<2/2.75f){t-=1.5f/2.75f;return 7.5625f*t*t+.75f;}if(t<2.5f/2.75f){t-=2.25f/2.75f;return 7.5625f*t*t+.9375f;}t-=2.625f/2.75f;return 7.5625f*t*t+.984375f;}
}
