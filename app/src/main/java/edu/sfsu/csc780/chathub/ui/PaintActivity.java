package edu.sfsu.csc780.chathub.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import edu.sfsu.csc780.chathub.R;

/**
 * Created by rajatar08 on 4/17/17.
 */

public class PaintActivity extends AppCompatActivity {

    private PaintScreenView mPaintScreenView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.canvas_layout);

        mPaintScreenView = (PaintScreenView) findViewById(R.id.canvasView);

    }

    public void clearCanvas(View view) {

        mPaintScreenView.clearCanvas();

    }
}
