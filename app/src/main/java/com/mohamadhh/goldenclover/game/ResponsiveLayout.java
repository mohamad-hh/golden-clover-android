package com.mohamadhh.goldenclover.game;
public final class ResponsiveLayout {
 public float scale=1,left,top;public void fit(int w,int h,int l,int t,int r,int b){scale=Math.min((w-l-r)/1920f,(h-t-b)/1080f);left=l+((w-l-r)-1920*scale)/2;top=t+((h-t-b)-1080*scale)/2;}
 public float x(float screen){return (screen-left)/scale;}public float y(float screen){return(screen-top)/scale;}
}
