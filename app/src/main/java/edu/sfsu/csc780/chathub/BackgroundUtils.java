package edu.sfsu.csc780.chathub;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.preference.PreferenceManager;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * Created by rajatar08 on 4/17/17.
 */

public class BackgroundUtils {

    public static void saveWallpaper(Activity activity, Uri uri) {

        String file = "wallpaper";

        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(activity);

        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.putString("wallpaperPath",uri.toString());
        edit.commit();
    }
}
