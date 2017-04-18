package edu.sfsu.csc780.chathub.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;

import edu.sfsu.csc780.chathub.ImageUtil;
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.paint_menu, menu);

        return true;
    }

    public void clearCanvas(View view) {

        mPaintScreenView.clearCanvas();

    }

    public void sendCanvasImage(View view) {

        View content = mPaintScreenView;
        content.setDrawingCacheEnabled(true);
        content.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        Bitmap bitmap = content.getDrawingCache();

        Uri uri = ImageUtil.savePhotoImage(this, bitmap);

        Intent intent = new Intent();
        intent.putExtra("PaintImageURI", uri.toString());
        setResult(Activity.RESULT_OK, intent);
        finish();
    }
}
