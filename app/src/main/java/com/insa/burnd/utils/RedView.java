package com.insa.burnd.utils;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.insa.burnd.R;

import trikita.log.Log;

// ImageView used to draw the compass
//C'est ici qu'on gère toute la dynamique de la boussole(forces de frottement et de rappel, Acc, Vitesse, angle, etc...)
//dont le but est de la bouger de manière "naturelle" vers la direction désirée.
//Ainsi, il suffit de modifier l'input (Voir méthode update) et le view amènera naturellement la boussole vers l'angle désiré.
public class RedView extends ImageView implements ValueAnimator.AnimatorUpdateListener{
    private final ImageView red = this;
    private Bitmap b;
    private float degrees;
    private int bPosW;
    private int bPosH;
    private int cPosW;
    private int cPosH;
    private int cR;
    private int color;
    private Paint p;
    private float aAcc;
    private float aVel;
    private float ang;
    private final float elastFactor = 10;
    private final float friction = 2;
    private float force;
    private final float mass = 5;
    private boolean search;
    private final double dt = 0.001;
    private ValueAnimator valueAn;
    private final float TOTAL_DISTANCE = 60;
    private final float INTERVAL_DISTANCE = 5;
    private final long ANIMATION_TIME = 2000;
    private final int INITIAL_COLOR = Color.rgb(255,150,150);
    private final int START_COLOR = Color.rgb(255,125,125);
    private final int FINAL_COLOR = Color.rgb(232,0,0);
    private int counter = 0;
    private ArgbEvaluator painter;



    public RedView(Context ctx, AttributeSet atts){
        super(ctx, atts);
        b = BitmapFactory.decodeResource(getResources(), R.drawable.love_compass);
        color = INITIAL_COLOR;
        p = new Paint();
        p.setColor(Color.WHITE);
        aAcc = 0;
        aVel = 0;
        ang = 0;
        search = false;
        degrees = 0;
        painter = new ArgbEvaluator();
        valueAn = new ValueAnimator();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        //On détermine les positions des éléments de la RedView ici.


        int size;
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        int widthWithoutPadding = width - getPaddingLeft() - getPaddingRight();
        int heightWithoutPadding = height - getPaddingTop() - getPaddingBottom();

        if (widthWithoutPadding > heightWithoutPadding) {
            size = heightWithoutPadding;
        } else {
            size = widthWithoutPadding;
        }
        setMeasuredDimension(size + getPaddingLeft() + getPaddingRight(), size + getPaddingTop() + getPaddingBottom());
        int realHeight = size + getPaddingTop() + getPaddingBottom();
        int realWidth = size + getPaddingLeft() + getPaddingRight();
        cPosH = realHeight/2;
        cPosW = realWidth/2;
        bPosH = (realHeight-b.getHeight())/2;
        bPosW = (realWidth-b.getWidth())/2;
        cR = (int) (b.getHeight()*0.7);
        setPivotX(cPosW);
        setPivotY(cPosH);
    }

    @Override
    protected void onDraw(@NonNull Canvas c){
        c.drawColor(color);
        c.drawCircle(cPosW, cPosH, cR, p);
        c.drawBitmap(b,bPosW,bPosH,null);
    }

    public void updateBearing(final float degrees){
        this.degrees = degrees;
    }
    //On update la direction

    //On update la distance
    public void updateDistance(final float distance){
        //La logique qui suit permet de modifier de manière continue et animée la couleur afin de la rendre de plus en plus rouge plus on s'approche
        if(distance > TOTAL_DISTANCE){
            if(!valueAn.isStarted()){
                valueAn = new ValueAnimator();
                valueAn.setIntValues(color, Color.rgb(255,150,150));
                valueAn.setStartDelay(0);
                valueAn.setEvaluator(new ArgbEvaluator());
                valueAn.setDuration(ANIMATION_TIME);
                valueAn.addUpdateListener(this);
                valueAn.start();
            }
            counter = (int) (TOTAL_DISTANCE/INTERVAL_DISTANCE);
        }
        while(counter<TOTAL_DISTANCE/INTERVAL_DISTANCE){
            if(counter*INTERVAL_DISTANCE <=distance && distance <= (counter+1)*INTERVAL_DISTANCE){
                if(!valueAn.isRunning()){
                    valueAn = new ValueAnimator();
                    valueAn.setStartDelay(0);
                    valueAn.setIntValues(color
                                         ,(int)painter.evaluate(1-((INTERVAL_DISTANCE*counter)/(TOTAL_DISTANCE-INTERVAL_DISTANCE)),START_COLOR,FINAL_COLOR));
                    valueAn.setEvaluator(new ArgbEvaluator());
                    valueAn.setDuration(ANIMATION_TIME);
                    valueAn.addUpdateListener(this);
                    valueAn.start();
                }
                counter = (int)(TOTAL_DISTANCE/INTERVAL_DISTANCE) +1;
            }
            counter++;
        }
        counter = 0;
    }


    //Cette fonction est invoquée lorsque l'on appuie sur start
    public void startStopSearch(){
        if(!search){
            search = true;
            //On recalcule régulièrement les paramètres dynamiques.
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    while(search){
                        force = -elastFactor*(degrees+ (float)Math.toRadians(red.getRotation())) - friction*aVel;
                        aAcc = force/mass;
                        aVel += aAcc*dt;
                        ang += aVel*dt;
                        Log.d("Angle : " , ang);
                        try{
                            Thread.sleep(1);
                        }catch(InterruptedException e){
                            e.printStackTrace();
                        }
                        red.post(new Runnable() {
                            @Override
                            public void run() {
                                updateRotation();
                            }
                        });
                    }
                }
            };
            Thread calcThread = new Thread(runnable);
            calcThread.setPriority(Thread.MIN_PRIORITY);
            calcThread.start();
        }else{
            search = false;
            force = 0;
            aAcc = 0;
            aVel = 0;
            ang = 0;
            color = INITIAL_COLOR;
            invalidate();
        }
    }

    private void updateRotation(){
        setRotation((float)Math.toDegrees(ang));
    }

    @Override
    public void onAnimationUpdate(ValueAnimator va){
        color = (int) va.getAnimatedValue();
        postInvalidate();
    }

    public Boolean getSearch(){
        return search;
    }
}
