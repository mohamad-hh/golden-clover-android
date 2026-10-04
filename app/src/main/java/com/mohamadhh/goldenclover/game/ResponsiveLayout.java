package com.mohamadhh.goldenclover.game;
public final class ResponsiveLayout {
 public static final int WIDTH=720,HEIGHT=1280;
 public float scale=1,left,top;
 public void fit(int w,int h,int l,int t,int r,int b){scale=Math.max(.001f,Math.min((w-l-r)/(float)WIDTH,(h-t-b)/(float)HEIGHT));left=l+((w-l-r)-WIDTH*scale)/2;top=t+((h-t-b)-HEIGHT*scale)/2;}
 public float x(float screen){return(screen-left)/scale;}public float y(float screen){return(screen-top)/scale;}
}
