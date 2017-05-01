package edu.sfsu.csc780.chathub.service;

import android.app.Service;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.speech.RecognizerIntent;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import edu.sfsu.csc780.chathub.R;
import edu.sfsu.csc780.chathub.ui.MainActivity;

/**
 * Created by rajatar08 on 4/30/17.
 */

public class FloatingWidgetService extends Service {
    private static final String LOG_TAG = FloatingWidgetService.class.getSimpleName();

    private View mFloatingWidget;
    private WindowManager mWindowManager;
    private View mFloatingView;
    private View mExpandedView;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mFloatingWidget = LayoutInflater.from(this).inflate(R.layout.floating_widget, null);

        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );

        params.gravity = Gravity.TOP | Gravity.LEFT;
        params.x = 0;
        params.y = 100;

        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        mWindowManager.addView(mFloatingWidget, params);

        mFloatingView = mFloatingWidget.findViewById(R.id.floating_view);
        mExpandedView = mFloatingWidget.findViewById(R.id.expanded_view);

        ImageView iconView = (ImageView) mFloatingWidget.findViewById(R.id.floating_icon);

        iconView.setOnTouchListener(new View.OnTouchListener() {

            private WindowManager.LayoutParams newParams = params;
            int x,y;
            float newX, newY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        x = newParams.x;
                        y = newParams.y;
                        newX = event.getRawX();
                        newY = event.getRawY();
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        newParams.x = (int) (x + (event.getRawX() - newX));
                        newParams.y = (int) (y + (event.getRawY() - newY));
                        mWindowManager.updateViewLayout(mFloatingWidget, newParams);
                        return true;

                    case MotionEvent.ACTION_UP:
                        int xUp = (int) (event.getRawX() - newX);
                        int yUp = (int) (event.getRawY() - newY);

                        if (xUp < 10 && yUp < 10) {
                            if (mFloatingView.getVisibility() == View.VISIBLE) {
                                mFloatingView.setVisibility(View.GONE);
                                mExpandedView.setVisibility(View.VISIBLE);
                            }
                        }
                        return true;
                }

                return false;
            }

        });

        ImageView closeButton = (ImageView) mFloatingWidget.findViewById(R.id.close_button);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopSelf();
            }
        });

    }

    public void closeExpandedView(View view) {
        mExpandedView.setVisibility(View.GONE);
        mFloatingView.setVisibility(View.VISIBLE);
    }

    public void captureSpeech(View view) {
        Intent intent = new Intent(FloatingWidgetService.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("voice_message_widget", true);
        startActivity(intent);
        stopSelf();
    }

    public void startChatApp(View view) {
        Intent intent = new Intent(FloatingWidgetService.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        stopSelf();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mFloatingWidget != null)
            mWindowManager.removeView(mFloatingWidget);
    }
}
