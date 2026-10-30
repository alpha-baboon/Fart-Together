package com.example.raftrumble;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.View;

public class GameView extends View {
    private final Paint p = new Paint(3);
    private final MainActivity activity;
    private float raftX = .5f, raftY = .5f, vx = 0, vy = 0;
    private float localX = 0, localY = 0, remoteX = 0, remoteY = 0;
    private long last = System.nanoTime();
    private int score = 0;
    public GameView(Context c, MainActivity a) { super(c); activity = a; setLayerType(View.LAYER_TYPE_SOFTWARE, null); }
    void addRemote(float x, float y) { remoteX = x; remoteY = y; }
    void setRemoteState(float x, float y) { raftX = x; raftY = y; invalidate(); }
    @Override protected void onDraw(Canvas c) {
        super.onDraw(c); float w=getWidth(), h=getHeight();
        c.drawColor(0xff69c9dc);
        p.setColor(0xffb5edf0); p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(3);
        for(int i=0;i<8;i++) c.drawCircle((i*137+40)%Math.max(1,(int)w), (i*83+20)%Math.max(1,(int)h), 18+i%3*7,p);
        p.setStyle(Paint.Style.FILL);
        // islands and floating hazards
        p.setColor(0xffe8c77c); c.drawOval(new RectF(w*.08f,h*.16f,w*.22f,h*.31f),p);
        p.setColor(0xffe8c77c); c.drawOval(new RectF(w*.76f,h*.66f,w*.92f,h*.82f),p);
        p.setColor(0xffe95d48); c.drawCircle(w*.30f,h*.70f,18,p);
        p.setColor(0xfff4f0dc); c.drawRoundRect(new RectF(w*.44f,h*.40f,w*.56f,h*.60f),18,18,p);
        p.setColor(0xff8b542e); c.drawRoundRect(new RectF(raftX*w-48,raftY*h-29,raftX*w+48,raftY*h+29),18,18,p);
        p.setColor(0xffffd166); c.drawCircle(raftX*w-18,raftY*h-7,11,p);
        p.setColor(0xffef476f); c.drawCircle(raftX*w+18,raftY*h-7,11,p);
        p.setColor(0xffffffff); p.setTextSize(18); c.drawText("DRAG TO FART-PROPEL THE RAFT", 18, 28, p);
        p.setTextSize(13); c.drawText("Avoid the islands • Build momentum • Reach the golden buoy",18,48,p);
        long now=System.nanoTime(); float dt=Math.min(.04f,(now-last)/1_000_000_000f); last=now;
        float ax=localX+remoteX, ay=localY+remoteY;
        vx += ax*dt*1.9f; vy += ay*dt*1.9f;
        vx *= .985f; vy *= .985f;
        raftX += vx*dt; raftY += vy*dt;
        raftX=Math.max(.07f,Math.min(.93f,raftX)); raftY=Math.max(.12f,Math.min(.88f,raftY));
        if (activity != null) activity.publishState(raftX,raftY);
        postInvalidateDelayed(16);
    }
    @Override public boolean onTouchEvent(MotionEvent e) {
        float x=e.getX(), y=e.getY(), cx=getWidth()/2f, cy=getHeight()/2f;
        if(e.getAction()==MotionEvent.ACTION_DOWN || e.getAction()==MotionEvent.ACTION_MOVE) {
            float dx=(x-cx)/(getWidth()*.35f), dy=(y-cy)/(getHeight()*.35f);
            float len=(float)Math.sqrt(dx*dx+dy*dy); if(len>1){dx/=len;dy/=len;}
            localX=dx; localY=dy;
            if(activity!=null) activity.sendThrust(dx,dy);
            return true;
        }
        if(e.getAction()==MotionEvent.ACTION_UP || e.getAction()==MotionEvent.ACTION_CANCEL) { localX=0;localY=0;activity.sendThrust(0,0);return true; }
        return true;
    }
}
