package com.example.android.layermask.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by jkongthong on 10/2/17.
 */

public class CustomImageView extends AppCompatImageView {
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
        //INITIALIZE VARIABLES
        hasTouchedImage = false;
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

        mPaintStroke = new Paint();
        mPaintStroke.setColor(Color.WHITE);
        mPaintStroke.setStrokeWidth(STROKE_WIDTH);
        mPaintStroke.setStyle(Paint.Style.STROKE);

        setLayerType(LAYER_TYPE_HARDWARE, mPaint);  // important for speed.
        setLayerType(LAYER_TYPE_HARDWARE, mPaintStroke);
        setLayerType(LAYER_TYPE_HARDWARE, mCoverPaint);

        this.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                performClick();
                hasTouchedImage = true;
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        x = event.getX();
                        y = event.getY();
                        invalidate();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        x = event.getX();
                        y = event.getY();
                        invalidate();
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
                return true;
            }
        });
    }

    @Override
    public boolean performClick() { //looks like this is an Android Studio 3.0 requirement suggestion.
        super.performClick();
        return true;
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
}
