package com.mohamadhh.goldenclover.game;
import java.util.Random;
public final class ParticleSystem {
 public static final int MAX=320;public final float[] x=new float[MAX],y=new float[MAX],vx=new float[MAX],vy=new float[MAX],life=new float[MAX],duration=new float[MAX],size=new float[MAX],angle=new float[MAX];public final int[] type=new int[MAX];private final Random rng=new Random(815);private int cursor;
 public void burst(float bx,float by,int count,int kind){for(int n=0;n<count;n++){int i=cursor++%MAX;x[i]=bx;y[i]=by;vx[i]=(rng.nextFloat()-.5f)*400;vy[i]=-180-rng.nextFloat()*400;life[i]=duration[i]=1.5f+rng.nextFloat()*2;size[i]=(kind==1?16:kind==2?15:8)+rng.nextFloat()*18;angle[i]=rng.nextFloat()*360;type[i]=kind;}}
 public void leaf(float bx,float by){int i=cursor++%MAX;x[i]=bx;y[i]=by;vx[i]=rng.nextFloat()*22-11;vy[i]=-14;life[i]=duration[i]=3;size[i]=18;type[i]=3;}
 public void ambient(){int i=cursor++%MAX;x[i]=rng.nextFloat()*720;y[i]=rng.nextFloat()*1280;vx[i]=rng.nextFloat()*10-5;vy[i]=-12;life[i]=duration[i]=3;size[i]=10;type[i]=0;}
 public void update(float dt){for(int i=0;i<MAX;i++)if(life[i]>0){life[i]-=dt;x[i]+=vx[i]*dt;y[i]+=vy[i]*dt;if(type[i]==1||type[i]==2)vy[i]+=460*dt;angle[i]+=dt*140;}}
}
