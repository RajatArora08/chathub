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
import android.widget.Toast;

import java.util.ArrayList;

import edu.sfsu.csc780.chathub.ImageUtil;

/**
 * Created by rajatar08 on 4/17/17.
 *
 * This class is a Custom View Class for the Blank paint Canvas
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

        mPaint = setPaintAttributes(Color.BLACK);

        mPath = new Path();

    }

    public Paint setPaintAttributes(int color) {

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(color);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeWidth(10f);

        return paint;
    }

    public void changeColor(int color) {
        mPaint = setPaintAttributes(color);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawPath(mPath, mPaint);
        canvas.drawBitmap(mBitmap, 0, 0, null);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        float locX = event.getX();
        float locY = event.getY();

        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:
                mPath.moveTo(locX, locY);
                break;

            case MotionEvent.ACTION_MOVE:
                mPath.lineTo(locX, locY);
                break;

            case MotionEvent.ACTION_UP:
                mCanvas.drawPath(mPath, mPaint);
                mPath.reset();
                break;

        }

        invalidate();
        return true;
    }

    public void clearCanvas() {
        mCanvas.drawColor(Color.WHITE);
        invalidate();
        Toast.makeText(getContext(), "Screen cleared!!", Toast.LENGTH_SHORT).show();
    }
}
