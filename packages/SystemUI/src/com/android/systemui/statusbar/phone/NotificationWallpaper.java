package com.android.systemui.statusbar.phone;

import java.io.File;
import java.io.IOException;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.provider.Settings;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import com.android.systemui.R;

class NotificationWallpaper extends FrameLayout {

    private final String TAG = "NotificationWallpaperUpdater";

    private ImageView mNotificationWallpaperImage;
    private Bitmap bitmapWallpaper;
    private Context mContext;

    float wallpaperAlpha = Settings.System.getFloat(getContext()
            .getContentResolver(), Settings.System.NOTIF_WALLPAPER_ALPHA, 1.0f);

    public NotificationWallpaper(Context context, AttributeSet attrs) {
        super(context);
        mContext = context;

        setNotificationBackground();
    }

    public void setNotificationBackground() {
        String notifBack = Settings.System.getString(getContext().
                getContentResolver(), Settings.System.NOTIF_BACKGROUND);

        if (notifBack != null) {
            if (!notifBack.isEmpty()) {
                try {
                    setBackgroundColor(Integer.parseInt(notifBack));
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    mNotificationWallpaperImage = new ImageView(getContext());
                    mNotificationWallpaperImage.setScaleType(ScaleType.CENTER_CROP);
                    addView(mNotificationWallpaperImage, -1, -1);
                    Context settingsContext = mContext.createPackageContext("com.baked.romcontrol", 0);
                    String wallpaperFile = settingsContext.getFilesDir() + "/notifwallpaper";
                    bitmapWallpaper = BitmapFactory.decodeFile(wallpaperFile);
                    Drawable d = new BitmapDrawable(getResources(), bitmapWallpaper);
                    d.setAlpha((int) (wallpaperAlpha * 255));
                    mNotificationWallpaperImage.setImageDrawable(d);
                } catch (NameNotFoundException e) {
                }
            }
        } else {
            setBackgroundColor(0xFF000000);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        if (bitmapWallpaper != null)
            bitmapWallpaper.recycle();

        System.gc();
        super.onDetachedFromWindow();
    }
}
