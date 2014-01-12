package com.android.systemui.quicksettings;

import android.content.Context;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.UserHandle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.systemui.BatteryMeterView;
import com.android.systemui.BatteryCircleMeterView;
import com.android.systemui.R;
import com.android.systemui.statusbar.phone.QuickSettingsContainerView;
import com.android.systemui.statusbar.phone.QuickSettingsController;
import com.android.systemui.statusbar.policy.BatteryController;
import com.android.systemui.statusbar.policy.BatteryController.BatteryStateChangeCallback;

public class BatteryTile extends QuickSettingsTile implements BatteryStateChangeCallback {

    private BatteryController mController;

    private int mBatteryLevel = 0;
    private boolean mPluggedIn;
    private BatteryMeterView battery;
    private BatteryCircleMeterView circleBattery;

    public BatteryTile(Context context, QuickSettingsController qsc, BatteryController controller) {
        super(context, qsc, R.layout.quick_settings_tile_battery);

        mController = controller;

        mOnClick = new OnClickListener() {
            @Override
            public void onClick(View v) {
                startSettingsActivity(Intent.ACTION_POWER_USAGE_SUMMARY);
            }
        };

        mOnLongClick = new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.setClassName("com.android.settings",
                        "com.android.settings.Settings$SystemSettingsActivity");
                startSettingsActivity(intent);
                return true;
            }
        };
        qsc.registerObservedContent(Settings.System.getUriFor(
                Settings.System.STATUS_BAR_BATTERY), this);
        qsc.registerObservedContent(Settings.System.getUriFor(
                Settings.System.STATUS_BAR_BATTERY_COLOR), this);
        qsc.registerObservedContent(Settings.System.getUriFor(
                Settings.System.STATUS_BAR_BATTERY_TEXT_COLOR), this);
        qsc.registerObservedContent(Settings.System.getUriFor(
                Settings.System.STATUS_BAR_BATTERY_TEXT_CHARGING_COLOR), this);
        qsc.registerObservedContent(Settings.System.getUriFor(
                Settings.System.STATUS_BAR_CIRCLE_BATTERY_ANIMATIONSPEED), this);
    }

    @Override
    void onPostCreate() {
        updateTile();
        mController.addStateChangedCallback(this);
        super.onPostCreate();
    }

    @Override
    public void onDestroy() {
        mController.removeStateChangedCallback(this);
        super.onDestroy();
    }

    @Override
    public void onChangeUri(ContentResolver resolver, Uri uri) {
        battery = (BatteryMeterView) mTile.findViewById(R.id.battery);
        circleBattery = (BatteryCircleMeterView) mTile.findViewById(R.id.circle_battery);
        if (circleBattery != null) {
            circleBattery.updateSettings();
        }
        if (battery != null) {
            battery.updateSettings();
        }
        updateResources();
    }

    @Override
    public void onBatteryLevelChanged(int level, boolean pluggedIn) {
        mBatteryLevel = level;
        mPluggedIn = pluggedIn;
        updateResources();
    }

    @Override
    public void updateResources() {
        updateTile();
        super.updateResources();
    }

    private synchronized void updateTile() {
        int batteryStyle = Settings.System.getIntForUser(mContext.getContentResolver(),
                Settings.System.STATUS_BAR_BATTERY, 0, UserHandle.USER_CURRENT);
        boolean batteryHasPercent = batteryStyle == BatteryMeterView.BATTERY_STYLE_ICON_PERCENT
                || batteryStyle == BatteryMeterView.BATTERY_STYLE_PERCENT
                || batteryStyle == BatteryMeterView.BATTERY_STYLE_CIRCLE_PERCENT
                || batteryStyle == BatteryMeterView.BATTERY_STYLE_DOTTED_CIRCLE_PERCENT;
        boolean statusBarBatteryHidden = batteryStyle == BatteryMeterView.BATTERY_STYLE_HIDDEN;

        if (statusBarBatteryHidden) {
            battery.setBatteryStyle(BatteryMeterView.BATTERY_STYLE_NORMAL);
            battery.setVisibility(View.VISIBLE);
        }

        if (mBatteryLevel == 100) {
            mLabel = mContext.getString(R.string.quick_settings_battery_charged_label);
        } else {
            if (!batteryHasPercent) {
                mLabel = mPluggedIn
                    ? mContext.getString(R.string.quick_settings_battery_charging_label,
                            mBatteryLevel)
                    : mContext.getString(R.string.status_bar_settings_battery_meter_format,
                            mBatteryLevel);
            } else {
                mLabel = mPluggedIn
                    ? mContext.getString(R.string.quick_settings_battery_charging)
                    : mContext.getString(R.string.quick_settings_battery_discharging);
            }
        }
    }

    @Override
    public void switchToRibbonMode() {
        TextView tv = (TextView) mTile.findViewById(R.id.text);
        if (tv != null) {
            tv.setVisibility(View.GONE);
        }
        int margin = mContext.getResources().getDimensionPixelSize(
                R.dimen.qs_tile_ribbon_icon_margin);
        View batteryMeter = mTile.findViewById(R.id.battery);
        if (batteryMeter != null) {
            MarginLayoutParams params = (MarginLayoutParams) batteryMeter.getLayoutParams();
            params.topMargin = params.bottomMargin = margin;
            batteryMeter.setLayoutParams(params);
        }
        View batteryCircle = mTile.findViewById(R.id.circle_battery);
        if (batteryCircle != null) {
            MarginLayoutParams params = (MarginLayoutParams) batteryCircle.getLayoutParams();
            params.topMargin = params.bottomMargin = margin;
            batteryCircle.setLayoutParams(params);
        }
    }

    @Override
    void updateQuickSettings() {
        TextView tv = (TextView) mTile.findViewById(R.id.text);
        tv.setText(mLabel);
    }
}
