package com.android.systemui.quicksettings;

import static com.android.internal.util.cm.QSUtils.getTileTextColor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.TextView;

import com.android.systemui.R;
import com.android.systemui.statusbar.phone.QuickSettingsController;
import com.android.systemui.statusbar.phone.QuickSettingsContainerView;

public class FastChargeTile extends QuickSettingsTile {
    private final String TAG = "FastChargeTile";

    String mFastChargePath;

    public FastChargeTile(Context context, LayoutInflater inflater,
            QuickSettingsContainerView container, QuickSettingsController qsc) {
        super(context, inflater, container, qsc);
        Resources res = mContext.getResources();
        mFastChargePath = res.getString(com.android.internal.R.string.config_fastChargePath);
        mTileLayout = R.layout.quick_settings_tile_fcharge;

        mOnClick = new OnClickListener() {
            @Override
            public void onClick(View v) {
                setFastCharge(isFastChargeOn() ? false : true);
                updateFastChargeTile();
                if (isEnabled()) {
                    flipTile(0);
                }
            }
        };

        mOnLongClick = new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                // Nothing to do here
                return true;
            }
        };
    }

    @Override
    void onPostCreate() {
        updateFastChargeTile();
        super.onPostCreate();
    }

    @Override
    void updateQuickSettings() {
        updateTilesPerRow();
        TextView tv = (TextView) mTile.findViewById(R.id.fcharge_textview);
        tv.setCompoundDrawablesWithIntrinsicBounds(0, mDrawable, 0, 0);
        tv.setText(mLabel);
        tv.setTextSize(1, mTileTextSize);
        tv.setTextColor(getTileTextColor(mContext));
    }

    @Override
    public void updateTilesPerRow() {
        super.updateTilesPerRow();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        updateFastChargeTile();
    }

    private void updateFastChargeTile() {
        boolean fchargeOn = isFastChargeOn();
        mDrawable = fchargeOn ? R.drawable.ic_qs_fcharge_on : R.drawable.ic_qs_fcharge_off;
        mLabel = fchargeOn ? mContext.getString(R.string.quick_settings_fcharge_on) :
                    mContext.getString(R.string.quick_settings_fcharge_off);

        updateQuickSettings();
    }

    private void setFastCharge(boolean on) {
        try {
            File fastCharge = new File(mFastChargePath);
            FileWriter fwriter = new FileWriter(fastCharge);
            BufferedWriter bwriter = new BufferedWriter(fwriter);
            bwriter.write(on ? "1" : "0");
            bwriter.close();
        } catch (IOException e) {
            Log.e(TAG, "Couldn't write fast_charge file");
        }
    }

    private boolean isFastChargeOn() {
        FileReader reader = null;
        BufferedReader breader = null;
        try {
            File file = new File(mFastChargePath);
            reader = new FileReader(file);
            breader = new BufferedReader(reader);
            return (breader.readLine().equals("1"));
        } catch (IOException e) {
            Log.e(TAG, "Couldn't read fast_charge file");
            return false;
        } finally {
            try {
                reader.close();
                breader.close();
            } catch (IOException e) {
                // ignore
            } catch (NullPointerException e) {

            }
        }

    }
}
