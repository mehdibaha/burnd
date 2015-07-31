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
import android.view.animation.OvershootInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

import com.insa.burnd.R;

import trikita.log.Log;

// ImageView used to draw the compass
//C'est ici qu'on gère toute la dynamique de la boussole(forces de frottement et de rappel, Acc, Vitesse, angle, etc...)
//dont le but est de la bouger de manière "naturelle" vers la direction désirée.
//Ainsi, il suffit de modifier l'input (Voir méthode update) et le view amènera naturellement la boussole vers l'angle désiré.
public class RedView extends ImageView implements ValueAnimator.AnimatorUpdateListener{
    private Bitmap b;
    private int bPosW;
    private int bPosH;
    private int cPosW;
    private int cPosH;
    private int cR;
    private int color;
    private Paint p;
    private float ang;
    private boolean search;
    private ValueAnimator valueAn;
    private ValueAnimator rotateAn;
    private final float TOTAL_DISTANCE = 60;
    private final float INTERVAL_DISTANCE = 5;
    private final long ANIMATION_TIME = 2000;
    private final long ROTATION_TIME = 2000;
    private final int INITIAL_COLOR = getResources().getColor(R.color.primary_light);
    private final int START_COLOR = getResources().getColor(R.color.primary_dark);
    private final int FINAL_COLOR = getResources().getColor(R.color.primary);
    private int counter = 0;
    private ArgbEvaluator painter;



    public RedView(Context ctx, AttributeSet atts){
        super(ctx, atts);
        b = BitmapFactory.decodeResource(getResources(), R.drawable.love_compass);
        color = INITIAL_COLOR;
        p = new Paint();
        ang = 0;
        search = false;
        painter = new ArgbEvaluator();
        valueAn = new ValueAnimator();
        rotateAn = new ValueAnimator();
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
        Log.d("drawing");
        setRotation((float) Math.toDegrees(ang));
        p.setColor(color);
        c.drawCircle(cPosW, cPosH, (float)(cR*1.4), p);
        p.setColor(Color.WHITE);
        c.drawCircle(cPosW, cPosH, cR, p);
        c.drawBitmap(b,bPosW,bPosH,null);
    }

    //On update la direction
    public void updateBearing(final float degrees){
        if(search){
            //Start animation to get there if there's no animation running;
            if(!rotateAn.isStarted() && ang != -degrees){
                rotateAn = new ValueAnimator();
                rotateAn.setFloatValues(ang, -degrees);
                rotateAn.setStartDelay(0);
                rotateAn.setInterpolator(new OvershootInterpolator());
                rotateAn.setDuration(ROTATION_TIME);
                rotateAn.addUpdateListener(this);
                rotateAn.start();
                Log.d("Rotated.");
            }
        }
    }

    //On update la distance
    public void updateDistance(final float distance){
        if(search){
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
                                , (int) painter.evaluate(1 - ((INTERVAL_DISTANCE * counter) / (TOTAL_DISTANCE - INTERVAL_DISTANCE)), START_COLOR, FINAL_COLOR));
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
    }


    //Cette fonction est invoquée lorsque l'on appuie sur start
    public void startStopSearch(){
        if(!search){
            search = true;
        }else{
            search = false;
            reset();
            ang = 0;
            color = INITIAL_COLOR;
        }
    }

    private void reset(){
        ValueAnimator oldCol = valueAn;
        ValueAnimator oldAng = rotateAn;
        valueAn = new ValueAnimator();
        valueAn.setStartDelay(0);
        valueAn.setIntValues(color
                , INITIAL_COLOR);
        valueAn.setEvaluator(new ArgbEvaluator());
        valueAn.setDuration(ANIMATION_TIME);
        valueAn.addUpdateListener(this);
        rotateAn = new ValueAnimator();
        rotateAn.setFloatValues(ang, 0);
        rotateAn.setStartDelay(0);
        rotateAn.setInterpolator(new OvershootInterpolator());
        rotateAn.setDuration(ROTATION_TIME);
        rotateAn.addUpdateListener(this);
        oldCol.cancel();
        valueAn.start();
        oldAng.cancel();
        rotateAn.start();
    }

    @Override
    public void onAnimationUpdate(ValueAnimator va){
        if(va == valueAn){
            color = (int) va.getAnimatedValue();
            postInvalidate();
        }else{
            ang = (float) va.getAnimatedValue();
            postInvalidate();
        }
    }

    public Boolean getSearch(){
        return search;
    }

}
