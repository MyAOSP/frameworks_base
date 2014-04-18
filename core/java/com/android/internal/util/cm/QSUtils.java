package com.android.internal.util.cm;

import android.bluetooth.BluetoothAdapter;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.StateListDrawable;
import android.hardware.Camera;
import android.hardware.display.DisplayManager;
import android.hardware.display.WifiDisplayStatus;
import android.net.ConnectivityManager;
import android.nfc.NfcAdapter;
import android.os.UserHandle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.View;

import com.android.internal.telephony.PhoneConstants;

import java.util.Random;

public class QSUtils {
    public static boolean deviceSupportsImeSwitcher(Context ctx) {
        Resources res = ctx.getResources();
        return res.getBoolean(com.android.internal.R.bool.config_show_cmIMESwitcher);
    }

    public static boolean deviceSupportsUsbTether(Context ctx) {
        ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        return (cm.getTetherableUsbRegexs().length != 0);
    }

    public static boolean deviceSupportsWifiDisplay(Context ctx) {
        DisplayManager dm = (DisplayManager) ctx.getSystemService(Context.DISPLAY_SERVICE);
        return (dm.getWifiDisplayStatus().getFeatureState() != WifiDisplayStatus.FEATURE_STATE_UNAVAILABLE);
    }

    public static boolean deviceSupportsMobileData(Context ctx) {
        ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.isNetworkSupported(ConnectivityManager.TYPE_MOBILE);
    }

    public static boolean deviceSupportsBluetooth() {
        return (BluetoothAdapter.getDefaultAdapter() != null);
    }

    public static boolean systemProfilesEnabled(ContentResolver resolver) {
        return (Settings.System.getInt(resolver, Settings.System.SYSTEM_PROFILES_ENABLED, 1) == 1);
    }

    public static boolean deviceSupportsPerformanceProfiles(Context ctx) {
        Resources res = ctx.getResources();
        String perfProfileProp = res.getString(
                com.android.internal.R.string.config_perf_profile_prop);
        return !TextUtils.isEmpty(perfProfileProp);
    }

    public static boolean expandedDesktopEnabled(ContentResolver resolver) {
        return Settings.System.getIntForUser(resolver, Settings.System.EXPANDED_DESKTOP_STYLE,
                0, UserHandle.USER_CURRENT_OR_SELF) != 0;
    }

    public static boolean deviceSupportsNfc(Context ctx) {
        return NfcAdapter.getDefaultAdapter(ctx) != null;
    }

    public static boolean deviceSupportsLte(Context ctx) {
        final TelephonyManager tm = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
        return (tm.getLteOnCdmaMode() == PhoneConstants.LTE_ON_CDMA_TRUE) || tm.getLteOnGsmMode() != 0;
    }

    public static boolean deviceSupportsDockBattery(Context ctx) {
        Resources res = ctx.getResources();
        //return res.getBoolean(com.android.internal.R.bool.config_hasDockBattery);
        return false;
    }

    public static boolean deviceSupportsCamera() {
        return Camera.getNumberOfCameras() > 0;
    }

    public static boolean deviceSupportsGps(Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS);
    }

    public static boolean deviceSupportsTorch(Context context) {
        return context.getResources().getBoolean(com.android.internal.R.bool.config_enableTorch);
    }

    public static boolean adbEnabled(ContentResolver resolver) {
        return (Settings.Global.getInt(resolver, Settings.Global.ADB_ENABLED, 0)) == 1;
    }

    public static int getMaxColumns(Context ctx) {
        Resources res = ctx.getResources();
        int colCount = res.getInteger(com.android.internal.R.integer.config_quickSettingsColumns);
        boolean isPortrait =
                res.getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;

        if (isPortrait) {
            colCount = Settings.System.getInt(ctx.getContentResolver(),
                    Settings.System.QUICK_SETTINGS_NUM_COLUMNS_PORT, colCount);
        } else {
            colCount = Settings.System.getInt(ctx.getContentResolver(),
                    Settings.System.QUICK_SETTINGS_NUM_COLUMNS_LAND, colCount);
        }
        return colCount;
    }

    public static int getTileTextColor(Context ctx) {
        int tileTextColor = Settings.System.getInt(ctx.getContentResolver(),
                Settings.System.QUICK_SETTINGS_TEXT_COLOR, 0xFFFFFFFF);
        return tileTextColor;
    }

    public static int updateTileTextSize(int column) {
        int mTileTextSize = 12;
        // adjust Tile Text Size based on column count
        switch (column) {
            case 7:
                mTileTextSize = 8;
                break;
            case 6:
                mTileTextSize = 8;
                break;
            case 5:
                mTileTextSize = 9;
                break;
            case 4:
                mTileTextSize = 10;
                break;
            case 3:
            default:
                mTileTextSize = 12;
                break;
            case 2:
                mTileTextSize = 14;
                break;
            case 1:
                mTileTextSize = 16;
                break;
        }
        return mTileTextSize;
    }

    public static void setTileBackground(Context ctx, View v, boolean useStates) {
        ContentResolver resolver = ctx.getContentResolver();
        StateListDrawable sld = new StateListDrawable();
        ColorDrawable pcd = new ColorDrawable(
                com.android.internal.R.drawable.notification_item_background_color_pressed);
        ColorDrawable cd;
        int tileBg = Settings.System.getInt(resolver,
                Settings.System.QUICK_SETTINGS_BACKGROUND_STYLE, 2);
        int blue = Settings.System.getInt(resolver,
                Settings.System.RANDOM_COLOR_ONE, com.android.internal.R.color.holo_blue_dark);
        int green = Settings.System.getInt(resolver,
                Settings.System.RANDOM_COLOR_TWO, com.android.internal.R.color.holo_green_dark);
        int red = Settings.System.getInt(resolver,
                Settings.System.RANDOM_COLOR_THREE, com.android.internal.R.color.holo_red_dark);
        int orange = Settings.System.getInt(resolver,
                Settings.System.RANDOM_COLOR_FOUR, com.android.internal.R.color.holo_orange_dark);
        int purple = Settings.System.getInt(resolver,
                Settings.System.RANDOM_COLOR_FIVE, com.android.internal.R.color.holo_purple);
        int blueBright = Settings.System.getInt(resolver,
                Settings.System.RANDOM_COLOR_SIX, com.android.internal.R.color.holo_blue_bright);
        switch (tileBg) {
            case 0:
                int[] colors = new int[] {blue, green, red, orange, purple, blueBright};
                Random generator = new Random();
                cd = new ColorDrawable(colors[generator.nextInt(colors.length)]);
                if (useStates) {
                    sld.addState(new int[] {com.android.internal.R.attr.state_pressed}, pcd);
                    sld.addState(new int[] {}, cd);
                    v.setBackground(sld);
                } else {
                    v.setBackground(cd);
                }
                break;
            case 1:
                int tileBgColor = Settings.System.getInt(resolver,
                        Settings.System.QUICK_SETTINGS_BACKGROUND_COLOR, 0xFF000000);
                cd = new ColorDrawable(tileBgColor);
                if (useStates) {
                    sld.addState(new int[] {com.android.internal.R.attr.state_pressed}, pcd);
                    sld.addState(new int[] {}, cd);
                    v.setBackground(sld);
                } else {
                    v.setBackground(cd);
                }
                break;
            case 2:
            default:
                v.setBackgroundResource(com.android.internal.R.drawable.qs_tile_background);
                break;
        }
    }
}
