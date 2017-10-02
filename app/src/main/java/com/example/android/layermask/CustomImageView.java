package com.example.android.layermask;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PorterDuff;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.support.v4.view.VelocityTrackerCompat;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.widget.Toast;

/**
 * Created by jkongthong on 10/2/17.
 */

public class CustomImageView extends AppCompatImageView {
    //private static final int SHRINK_RADIUS_BY;
    private final String TAG = CustomImageView.class.getSimpleName();
    private static final int STROKE_WIDTH = 20; //set as necessary.
    private static final int SCALE_FACTOR = 2;

    private Context context;
    private Paint mPaint;
    private Paint mPaintStroke;
    private PorterDuffXfermode mPorterDuffXfermode;
    private Canvas mCircleCanvas;
    private Bitmap mCircleBitmap;
    private Canvas mCircleCanvasBorder;
    private Bitmap mCircleBitmapBorder;
    private Canvas mCoverCanvas;
    private Bitmap mCoverScreenBitmap;
    private Paint mCoverPaint;
    private int circleWidth,circleHeight;
    private int circleWidthBorder,circleHeightBorder; //not necessary
    private float circleRadius;
    private float x;
    private float y;
    private int width;
    private int height;
    private boolean hasTouchedImage;

    //TIMER
    private CountDownTimer timer;
    private Long PEEK_TIME;
    private static final Long PEEK_INTERVAL = 100L;
    private boolean isTimerExpired;
    private boolean timerStarted;
    private ValueAnimator anim;


    private Listener listener;
    public interface Listener {
        void onTimerEnd();
    }

    public CustomImageView(Context context) {
        super(context);
        this.context = context;
        init();
    }

    public CustomImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    public CustomImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init();
    }

    public void init(){
        if (VERSION.SDK_INT == VERSION_CODES.M) { //on M it cuts this in half so make it 10 seconds. L seemed ok. and N+ seemed ok. huh?
            PEEK_TIME = 10000L;
        }
        else {
            PEEK_TIME = 5000L;
        }
        //INITIALIZE VARIABLES
        hasTouchedImage = false;
        isTimerExpired = false;
        timerStarted = false;
        x=y=0;

        //INITIALIZE PAINTS
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.FILL);

        mPaint.setColor(Color.BLUE); //this color is arbitrary
        mPorterDuffXfermode = new PorterDuffXfermode(PorterDuff.Mode.XOR); //For transparency.  Add different Modes here if you want to experiment.
        //mPaint.setXfermode(mPorterDuffXfermode); //for some reason, only works onDraw and we have to make it immediately null again.. huh?

        mCoverPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCoverPaint.setColor(Color.BLACK);
        mCoverPaint.setStyle(Style.FILL_AND_STROKE);

//        setLayerType(LAYER_TYPE_HARDWARE, mPaint);  //this is important for speed.
//        setLayerType(LAYER_TYPE_HARDWARE, mPaintStroke);
//        setLayerType(LAYER_TYPE_HARDWARE, mCoverPaint);

        mPaintStroke = new Paint();
        mPaintStroke.setColor(Color.WHITE);
        mPaintStroke.setStrokeWidth(STROKE_WIDTH);
        mPaintStroke.setStyle(Paint.Style.STROKE);

        this.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                    //if (!isTimerExpired) {
                if (true) {
                    hasTouchedImage = true;
                    //startTimer(); //uncomment to work with a timer.
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            //if (timerStarted) {
                                x = event.getX();
                                y = event.getY();
                                invalidate();
                            //}
                            break;
                        case MotionEvent.ACTION_MOVE:
                            //MY CODE
                            //if (timerStarted) {
                                x = event.getX();
                                y = event.getY();
                                invalidate();
                            //}
                            break;
                        case MotionEvent.ACTION_CANCEL:
                        case MotionEvent.ACTION_UP:
                            x = event.getX();
                            y = event.getY();
                            hasTouchedImage = false;
                            invalidate();
                            break;
                        default:
                            break;
                    }
                } else {
                    return false;
                }
                return true;
            }
        });
    }

    public void setImageListener(CustomImageView.Listener listener){
        this.listener = listener;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        this.width = w;
        this.height = h;
        //cover the image with this
        this.mCoverScreenBitmap = Bitmap.createBitmap(w,h, Bitmap.Config.ARGB_8888);
        this.mCoverCanvas = new Canvas(mCoverScreenBitmap);

        //
        this.circleHeight = w / SCALE_FACTOR; //going to be a square
        this.circleWidth = w / SCALE_FACTOR;
        mCircleBitmap = Bitmap.createBitmap(circleWidth,circleHeight,Bitmap.Config.ARGB_8888);
        mCircleCanvas = new Canvas(mCircleBitmap);

        this.circleHeightBorder = w / SCALE_FACTOR ; //just bigger because the stroke gets cut off by canvas
        this.circleWidthBorder = w / SCALE_FACTOR;
        mCircleBitmapBorder = Bitmap.createBitmap(circleWidthBorder,circleHeightBorder, Bitmap.Config.ARGB_8888);
        mCircleCanvasBorder = new Canvas(mCircleBitmapBorder);
        circleRadius = (circleWidth /2)- STROKE_WIDTH;
        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mCoverCanvas.drawRect(0,0,width,height,mCoverPaint);
        canvas.drawBitmap(mCoverScreenBitmap,0,0,mCoverPaint);
        if(hasTouchedImage) {
            //Circle
            //mCircleCanvas.drawColor(Color.TRANSPARENT, Mode.CLEAR); //clear the canvas?  use this if circleRadius changes on action_move
            mCircleCanvas.drawCircle(circleWidth / 2, circleHeight / 2, circleRadius, mPaint);
            mPaint.setXfermode(mPorterDuffXfermode);
            mCoverCanvas.drawBitmap(mCircleBitmap, x - (circleWidth / 2), y - ((1.5f) * circleHeight), mPaint);//Draw onto mCoverCanvas not canvas
            mPaint.setXfermode(null); //needed if circleRadius changes on action_move

            //Border
           // mCircleCanvasBorder.drawColor(Color.TRANSPARENT, Mode.CLEAR); //use this if circleRadius changes on action_move
            mCircleCanvasBorder.drawCircle(circleWidthBorder / 2, circleHeightBorder / 2, circleRadius, mPaintStroke);
            mCoverCanvas.drawBitmap(mCircleBitmapBorder, x - (circleWidth / 2), y - ((1.5f) * circleHeight), mPaintStroke); //move above finger tip = approx 1.5 ? LOL!
        }
    }

    public ValueAnimator getValueAnimatorFromImage(){
        return anim;
    }
    //THIS IS TO ADD A TIMER TO IT.
    //USING A VALUEANIMATOR BECAUSE IT ORIGINALLY SHRANK FOR A PERIOD OF 5 SECONDS AND I WANTED THE ANIMATION SMOOTH.  AFTER THAT WAS AXED, I JUST KEPT AND DIDN'T CHANGE ANY PROPERTIES BUT IT STILL COUNTED DOWN
    private boolean startTimer() {
        if (!timerStarted) {
            timerStarted = true;
            anim = ValueAnimator.ofFloat(circleRadius, 0f); //for making circle smaller.
            //anim = ValueAnimator.ofFloat(0, 360f);
            anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    //this is what makes it shrink.  Remove invalidates from ontouchlisteners and let the timer invalidate instead.
                    // float val = (Float) valueAnimator.getAnimatedValue();
                    //circleRadius = val;
                    //invalidate();
                }
            });
            anim.addListener(new Animator.AnimatorListener(){
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    hasTouchedImage = false;
                    isTimerExpired = true;
                    //listener.onTimerEnd();
                    invalidate();
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    mCircleCanvasBorder .drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR); //clear the canvas?  Need to remove previous circles!!! since we initialize everything first. .e. no new objects in onDraw.
                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });

            anim.setDuration(PEEK_TIME);
            anim.start();
        }
        return true;
    }

}
