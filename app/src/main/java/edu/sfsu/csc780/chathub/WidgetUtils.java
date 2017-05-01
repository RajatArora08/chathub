package edu.sfsu.csc780.chathub;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

/**
 * Created by rajatar08 on 4/30/17.
 */

public class WidgetUtils {
    private static final String LOG_TAG = WidgetUtils.class.getSimpleName();


    private static int GRANTED = PackageManager.PERMISSION_GRANTED;
    private static String SYSTEM_ALERT_WINDOW = android.Manifest.permission.SYSTEM_ALERT_WINDOW;
    public static final int REQUEST_CODE = 2086;


    public static void checkScreenOverlayPermission(Activity activity) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(activity)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + activity.getPackageName()));
            activity.startActivityForResult(intent, REQUEST_CODE);
        }
    }

}
