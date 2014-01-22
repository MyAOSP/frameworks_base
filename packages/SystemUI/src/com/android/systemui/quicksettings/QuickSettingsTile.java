package com.android.systemui.quicksettings;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.animation.Animator.AnimatorListener;
import android.app.ActivityManagerNative;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.StateListDrawable;
import android.net.Uri;
import android.os.Handler;
import android.os.RemoteException;
import android.os.UserHandle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.internal.util.cm.QSUtils;
import com.android.systemui.R;
import com.android.systemui.statusbar.phone.QuickSettingsController;
import com.android.systemui.statusbar.phone.PhoneStatusBar;
import com.android.systemui.statusbar.phone.QuickSettingsContainerView;
import com.android.systemui.statusbar.phone.QuickSettingsTileView;

import java.util.Random;

public class QuickSettingsTile implements OnClickListener {

    protected final Context mContext;
    protected QuickSettingsContainerView mContainer;
    protected QuickSettingsTileView mTile;
    protected OnClickListener mOnClick;
    protected OnLongClickListener mOnLongClick;
    protected final int mTileLayout;
    protected int mDrawable;
    protected String mLabel;
    protected PhoneStatusBar mStatusbarService;
    protected QuickSettingsController mQsc;
    protected SharedPreferences mPrefs;
    public boolean enable = false;

    public int mTileTextSize = 12;
    private Handler mHandler = new Handler();

    public QuickSettingsTile(Context context, QuickSettingsController qsc) {
        this(context, qsc, R.layout.quick_settings_tile_basic);
    }

    public QuickSettingsTile(Context context, QuickSettingsController qsc, int layout) {
        mContext = context;
        mDrawable = R.drawable.ic_notifications;
        mLabel = mContext.getString(R.string.quick_settings_label_enabled);
        mStatusbarService = qsc.mStatusBarService;
        mQsc = qsc;
        mTileLayout = layout;
        mPrefs = mContext.getSharedPreferences("quicksettings", Context.MODE_PRIVATE);
    }

    public void setupQuickSettingsTile(LayoutInflater inflater,
            QuickSettingsContainerView container) {
        mTile = (QuickSettingsTileView) inflater.inflate(
                R.layout.quick_settings_tile, container, false);
        mTile.setContent(mTileLayout, inflater);
        mContainer = container;
        mContainer.addView(mTile);
        onPostCreate();
        updateQuickSettings();
        setTileBackground();
        mTile.setOnClickListener(this);
        mTile.setOnLongClickListener(mOnLongClick);
    }

    public void switchToRibbonMode() {
        TextView tv = (TextView) mTile.findViewById(R.id.text);
        if (tv != null) {
            tv.setVisibility(View.GONE);
        }
    }

    void onPostCreate() {}

    public void onDestroy() {}

    public void onReceive(Context context, Intent intent) {}

    public void onChangeUri(ContentResolver resolver, Uri uri) {}

    public void updateResources() {
        if (mTile != null) {
            updateQuickSettings();
        }
    }

    void updateQuickSettings() {
        updateTilesPerRow();
        TextView tv = (TextView) mTile.findViewById(R.id.text);
        if (tv != null) {
            tv.setText(mLabel);
            tv.setTextSize(1, mTileTextSize);
            tv.setTextColor(QSUtils.getTileTextColor(mContext));
        }
        ImageView image = (ImageView) mTile.findViewById(R.id.image);
        if (image != null) {
            image.setImageResource(mDrawable);
        }
    }

    void startSettingsActivity(String action) {
        Intent intent = new Intent(action);
        startSettingsActivity(intent);
    }

    void startSettingsActivity(Intent intent) {
        startSettingsActivity(intent, true);
    }

    private void startSettingsActivity(Intent intent, boolean onlyProvisioned) {
        if (onlyProvisioned && !mStatusbarService.isDeviceProvisioned()) return;
        try {
            // Dismiss the lock screen when Settings starts.
            ActivityManagerNative.getDefault().dismissKeyguardOnNextActivity();
        } catch (RemoteException e) {
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        mContext.startActivityAsUser(intent, new UserHandle(UserHandle.USER_CURRENT));
        mStatusbarService.animateCollapsePanels();
    }

    @Override
    public void onClick(View v) {
        if (mOnClick != null) {
            mOnClick.onClick(v);
        }

        ContentResolver resolver = mContext.getContentResolver();
        boolean shouldCollapse = Settings.System.getIntForUser(resolver,
                Settings.System.QS_COLLAPSE_PANEL, 0, UserHandle.USER_CURRENT) == 1;
        if (shouldCollapse) {
            mQsc.mBar.collapseAllPanels(true);
        }
    }

    public void animateTile(int delay, boolean on){
        ContentResolver resolver = mContext.getContentResolver();
        int animationSet = Settings.System.getIntForUser(resolver,
                Settings.System.QS_ANIMATION_SET, 0, UserHandle.USER_CURRENT);
        if (animationSet != 0) {
            final AnimatorSet anim;
            if (animationSet == 1) {
                anim = (AnimatorSet) AnimatorInflater.loadAnimator(
                        mContext, on ? R.anim.tile_flip_right : R.anim.tile_flip_left);
            } else {
                anim = (AnimatorSet) AnimatorInflater.loadAnimator(
                        mContext, on ? R.anim.tile_bounce : R.anim.tile_blink);
            }

            anim.setTarget(mTile);
            anim.setDuration(200);
            anim.addListener(new AnimatorListener() {
                @Override
                public void onAnimationEnd(Animator animation) {}
                @Override
                public void onAnimationStart(Animator animation) {}
                @Override
                public void onAnimationCancel(Animator animation) {}
                @Override
                public void onAnimationRepeat(Animator animation) {}

            });

            Runnable doAnimation = new Runnable(){
                @Override
                public void run() {
                    anim.start();
                }
            };

            mHandler.postDelayed(doAnimation, delay);
        }
    }

    void updateTileTextSize(int column) {
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
    }

    public void updateTilesPerRow() {
        Resources res = mContext.getResources();
        int columnCount;
        if (res.getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            columnCount = QSUtils.getMaxColumns(mContext, Configuration.ORIENTATION_PORTRAIT);
        } else {
            columnCount = QSUtils.getMaxColumns(mContext, Configuration.ORIENTATION_LANDSCAPE);
        }
        ((QuickSettingsContainerView) mContainer).setColumnCount(columnCount);
        updateTileTextSize(columnCount);
    }

    protected void setTileBackground() {
        ContentResolver mContentResolver = mContext.getContentResolver();
        StateListDrawable states = new StateListDrawable();
        ColorDrawable cd = new ColorDrawable(0xAA505050);
        ColorDrawable colorDrawable;
        int tileBg = Settings.System.getInt(mContentResolver,
                Settings.System.QUICK_SETTINGS_BACKGROUND_STYLE, 2);
        int blue = Settings.System.getInt(mContentResolver,
                Settings.System.RANDOM_COLOR_ONE, com.android.internal.R.color.holo_blue_dark);
        int green = Settings.System.getInt(mContentResolver,
                Settings.System.RANDOM_COLOR_TWO, com.android.internal.R.color.holo_green_dark);
        int red = Settings.System.getInt(mContentResolver,
                Settings.System.RANDOM_COLOR_THREE, com.android.internal.R.color.holo_red_dark);
        int orange = Settings.System.getInt(mContentResolver,
                Settings.System.RANDOM_COLOR_FOUR, com.android.internal.R.color.holo_orange_dark);
        int purple = Settings.System.getInt(mContentResolver,
                Settings.System.RANDOM_COLOR_FIVE, com.android.internal.R.color.holo_purple);
        int blueBright = Settings.System.getInt(mContentResolver,
                Settings.System.RANDOM_COLOR_SIX, com.android.internal.R.color.holo_blue_bright);
        switch (tileBg) {
            case 0:
                int[] colors = new int[] {blue, green, red, orange, purple, blueBright};
                Random generator = new Random();
                colorDrawable = new ColorDrawable(colors[generator.nextInt(colors.length)]);
                states.addState(new int[] {com.android.internal.R.attr.state_pressed}, cd);
                states.addState(new int[] {}, colorDrawable);
                mTile.setBackground(states);
                break;
            case 1:
                int tileBgColor = Settings.System.getInt(mContentResolver,
                        Settings.System.QUICK_SETTINGS_BACKGROUND_COLOR, 0xFF000000);
                colorDrawable = new ColorDrawable(tileBgColor);
                states.addState(new int[] {com.android.internal.R.attr.state_pressed}, cd);
                states.addState(new int[] {}, colorDrawable);
                mTile.setBackground(states);
                break;
            case 2:
            default:
                mTile.setBackgroundResource(R.drawable.qs_tile_background);
                break;
        }
    }
}
