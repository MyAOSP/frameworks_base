package com.android.internal.util.cm;

import java.io.File;

import android.bluetooth.BluetoothAdapter;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.display.DisplayManager;
import android.hardware.display.WifiDisplayStatus;
import android.net.ConnectivityManager;
import android.nfc.NfcAdapter;
import android.provider.Settings;
import android.telephony.TelephonyManager;

import com.android.internal.telephony.PhoneConstants;

public class QSUtils {
    private final Context mContext;

    public QSUtils(Context ctx) {
        mContext = ctx;
    }

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

    public static boolean deviceSupportsFastCharge() {
        String mFastChargePath = mContext.getResources().getString(com.android.internal.R.string.config_fastChargePath);

        return mFastChargePath != null || !mFastChargePath.isEmpty()
                    || new File(mFastChargePath).exists();
    }

    public static boolean deviceSupportsLTE() {
        return (PhoneConstants.LTE_ON_CDMA_TRUE == TelephonyManager.getDefault().getLteOnCdmaMode() ||
                        TelephonyManager.getDefault().getLteOnGsmMode() != 0);
    }
}
