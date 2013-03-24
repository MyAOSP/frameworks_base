package com.android.systemui.quicksettings;

import static com.android.internal.util.cm.QSUtils.getTileTextColor;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.TextView;

import com.android.internal.telephony.Phone;

import com.android.systemui.R;
import com.android.systemui.statusbar.phone.QuickSettingsController;
import com.android.systemui.statusbar.phone.QuickSettingsContainerView;

public class LteTile extends QuickSettingsTile {

    private Context mContext;

    public LteTile(Context context, LayoutInflater inflater,
            QuickSettingsContainerView container, QuickSettingsController qsc) {
        super(context, inflater, container, qsc);
        mTileLayout = R.layout.quick_settings_tile_lte;

        mContext = context;

        mOnClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleState();
                updateResources();
            }
        };

        mOnLongClick = new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.setClassName("com.android.phone", "com.android.phone.MobileNetworkSettings");
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startSettingsActivity(intent);
                return true;
            }
        };

        qsc.registerObservedContent(Settings.Global.getUriFor(Settings.Global.PREFERRED_NETWORK_MODE), this);
    }

    @Override
    void onPostCreate() {
        updateTile();
        super.onPostCreate();
    }

    @Override
    public void onChangeUri(ContentResolver resolver, Uri uri) {
        updateResources();
    }

    @Override
    public void updateResources() {
        updateTile();
        super.updateResources();
    }

    @Override
    void updateQuickSettings() {
        updateTilesPerRow();
        TextView tv = (TextView) mTile.findViewById(R.id.lte_textview);
        tv.setCompoundDrawablesWithIntrinsicBounds(0, mDrawable, 0, 0);
        tv.setText(mLabel);
        tv.setTextSize(1, mTileTextSize);
        tv.setTextColor(getTileTextColor(mContext));
    }

    @Override
    public void updateTilesPerRow() {
        super.updateTilesPerRow();
    }

    protected void toggleState() {
        TelephonyManager tm = (TelephonyManager) mContext.getSystemService(
                Context.TELEPHONY_SERVICE);
        int network = getCurrentPreferredNetworkMode(mContext);
        switch(network) {
            case Phone.NT_MODE_GLOBAL:
            case Phone.NT_MODE_LTE_CDMA_AND_EVDO:
            case Phone.NT_MODE_LTE_GSM_WCDMA:
            case Phone.NT_MODE_LTE_CMDA_EVDO_GSM_WCDMA:
            case Phone.NT_MODE_LTE_ONLY:
            case Phone.NT_MODE_LTE_WCDMA:
                tm.toggleLTE(false);
                break;
            default:
                tm.toggleLTE(true);
                break;
        }
    }

    private synchronized void updateTile() {
        int network = getCurrentPreferredNetworkMode(mContext);
        switch(network) {
            case Phone.NT_MODE_GLOBAL:
            case Phone.NT_MODE_LTE_CDMA_AND_EVDO:
            case Phone.NT_MODE_LTE_GSM_WCDMA:
            case Phone.NT_MODE_LTE_CMDA_EVDO_GSM_WCDMA:
            case Phone.NT_MODE_LTE_ONLY:
            case Phone.NT_MODE_LTE_WCDMA:
                mDrawable = R.drawable.ic_qs_lte_on;
                mLabel = mContext.getString(R.string.quick_settings_lte);
                break;
            default:
                mDrawable = R.drawable.ic_qs_lte_off;
                mLabel = mContext.getString(R.string.quick_settings_lte_off);
                break;
        }
    }

    private static int getCurrentPreferredNetworkMode(Context context) {
        int network = -1;
        try {
            network = Settings.Global.getInt(context.getContentResolver(),
                    Settings.Global.PREFERRED_NETWORK_MODE);
        } catch (SettingNotFoundException e) {
            e.printStackTrace();
        }
        return network;
    }
}
