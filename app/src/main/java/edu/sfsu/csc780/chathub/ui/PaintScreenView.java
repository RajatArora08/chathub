package edu.sfsu.csc780.chathub.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by rajatar08 on 4/17/17.
 */

public class PaintScreenView extends View {

    private Paint mPaint;
    private Path mPath;
    private Bitmap mBitmap;
    private Canvas mCanvas;

    public PaintScreenView(Context context) {
        super(context);
    }

    public PaintScreenView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        mPaint = new Paint();
        setPaintAttributes();

        mPath = new Path();

    }

    public void setPaintAttributes() {
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.BLUE);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeWidth(10f);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawPath(mPath, mPaint);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        float locX = event.getX();
        float locY = event.getY();

        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:
                mPath.moveTo(locX, locY);
                return true;

            case MotionEvent.ACTION_MOVE:
                mPath.lineTo(locX, locY);
                break;

            case MotionEvent.ACTION_UP:
                break;

        }

        invalidate();
        return true;
    }

    public void clearCanvas() {
        mPath.reset();
        invalidate();
    }
}
