package com.android.internal.util.cm;

import java.io.File;

import android.bluetooth.BluetoothAdapter;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.hardware.display.DisplayManager;
import android.hardware.display.WifiDisplayStatus;
import android.net.ConnectivityManager;
import android.nfc.NfcAdapter;
import android.provider.Settings;
import android.telephony.TelephonyManager;

import com.android.internal.telephony.PhoneConstants;

public class QSUtils {

    public static boolean deviceSupportsUsbTether(Context ctx) {
        ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        return (cm.getTetherableUsbRegexs().length != 0);
    }

    public static boolean deviceSupportsWifiDisplay(Context ctx) {
        DisplayManager dm = (DisplayManager) ctx.getSystemService(Context.DISPLAY_SERVICE);
        return (dm.getWifiDisplayStatus().getFeatureState() != WifiDisplayStatus.FEATURE_STATE_UNAVAILABLE);
    }

    public static boolean deviceSupportsTelephony(Context ctx) {
        PackageManager pm = ctx.getPackageManager();
        return pm.hasSystemFeature(PackageManager.FEATURE_TELEPHONY);
    }

    public static boolean deviceSupportsBluetooth() {
        return (BluetoothAdapter.getDefaultAdapter() != null);
    }

    public static boolean systemProfilesEnabled(ContentResolver resolver) {
        return (Settings.System.getInt(resolver, Settings.System.SYSTEM_PROFILES_ENABLED, 1) == 1);
    }

    public static boolean deviceSupportsNfc(Context ctx) {
        return NfcAdapter.getDefaultAdapter(ctx) != null;
    }

    public static boolean deviceSupportsFastCharge(Context ctx) {
        String mFastChargePath = ctx.getResources().getString(com.android.internal.R.string.config_fastChargePath);
        return (new File(mFastChargePath).exists() && ctx.getResources().getBoolean(
                com.android.internal.R.bool.config_fastChargeSupport) != false);
    }

    public static boolean deviceSupportsLte(Context ctx) {
        final TelephonyManager tm = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
        return (tm.getLteOnCdmaMode() == PhoneConstants.LTE_ON_CDMA_TRUE) || tm.getLteOnGsmMode() != 0;
    }

    public static int getMaxColumns(Context ctx, int orientation) {
        int maxColumns = ctx.getResources().getInteger(
                com.android.internal.R.integer.quick_settings_num_columns);

        if (orientation == Configuration.ORIENTATION_PORTRAIT){
            maxColumns = Settings.System.getInt(ctx.getContentResolver(),
                    Settings.System.QUICK_SETTINGS_NUM_COLUMNS_PORT, maxColumns);
        } else {
            maxColumns = Settings.System.getInt(ctx.getContentResolver(),
                    Settings.System.QUICK_SETTINGS_NUM_COLUMNS_LAND, maxColumns);
        }
        return maxColumns;
    }
}
