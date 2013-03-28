package com.android.systemui.quicksettings;

import java.util.Random;

import static com.android.internal.util.cm.QSUtils.getMaxColumns;
import static com.android.internal.util.cm.QSUtils.getTileTextColor;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.animation.Animator.AnimatorListener;
import android.app.ActivityManagerNative;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.content.res.Resources;
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
import android.widget.TextView;

import com.android.systemui.R;
import com.android.systemui.statusbar.BaseStatusBar;
import com.android.systemui.statusbar.phone.PhoneStatusBar;
import com.android.systemui.statusbar.phone.QuickSettingsController;
import com.android.systemui.statusbar.phone.QuickSettingsContainerView;
import com.android.systemui.statusbar.phone.QuickSettingsTileView;

public class QuickSettingsTile implements OnClickListener {

    protected final Context mContext;
    protected final ViewGroup mContainerView;
    protected final LayoutInflater mInflater;
    protected QuickSettingsTileView mTile;
    protected OnClickListener mOnClick;
    protected OnLongClickListener mOnLongClick;
    protected int mTileLayout;
    protected int mDrawable;
    protected String mLabel;
    protected BaseStatusBar mStatusbarService;
    protected QuickSettingsController mQsc;

    private Handler mHandler = new Handler();

    public int mTileTextSize = 12;

    public QuickSettingsTile(Context context, LayoutInflater inflater, QuickSettingsContainerView container, QuickSettingsController qsc) {
        mContext = context;
        mContainerView = container;
        mInflater = inflater;
        mDrawable = R.drawable.ic_notifications;
        mLabel = mContext.getString(R.string.quick_settings_label_enabled);
        mStatusbarService = qsc.mStatusBarService;
        mQsc = qsc;
        mTileLayout = R.layout.quick_settings_tile_generic;
    }

    public void setupQuickSettingsTile() {
        createQuickSettings();
        onPostCreate();
        updateQuickSettings();
        mTile.setOnClickListener(this);
        mTile.setOnLongClickListener(mOnLongClick);
    }

    void createQuickSettings() {
        mTile = (QuickSettingsTileView) mInflater.inflate(R.layout.quick_settings_tile, mContainerView, false);
        mTile.setContent(mTileLayout, mInflater);
        setTileBackground();
        mContainerView.addView(mTile);
    }

    void onPostCreate(){}

    public void onReceive(Context context, Intent intent) {}

    public void onChangeUri(ContentResolver resolver, Uri uri) {}

    public void updateResources() {
        if(mTile != null) {
            updateQuickSettings();
        }
    }

    void updateQuickSettings() {
        updateTilesPerRow();
        TextView tv = (TextView) mTile.findViewById(R.id.tile_textview);
        tv.setCompoundDrawablesWithIntrinsicBounds(0, mDrawable, 0, 0);
        tv.setText(mLabel);
        tv.setTextSize(1, mTileTextSize);
        tv.setTextColor(getTileTextColor(mContext));
    }

    void startSettingsActivity(String action){
        Intent intent = new Intent(action);
        startSettingsActivity(intent);
    }

    void startSettingsActivity(Intent intent) {
        startSettingsActivity(intent, true);
    }

    public boolean isEnabled() {
        return (Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.QUICK_SETTINGS_TILES_FLIP, 1) == 1);
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

    public void flipTile(int delay){
        final AnimatorSet anim = (AnimatorSet) AnimatorInflater.loadAnimator(
                mContext, R.anim.flip_right);
        anim.setTarget(mTile);
        anim.setDuration(200);
        anim.addListener(new AnimatorListener(){

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

    public void flipOtherTiles(final QuickSettingsTileView view, int delay){
        final AnimatorSet anim = (AnimatorSet) AnimatorInflater.loadAnimator(
                mContext, R.anim.flip_left);
        anim.setTarget(view);
        anim.setDuration(200);
        anim.addListener(new AnimatorListener(){

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

    private void doFlip() {
        int delay = 0;
        for (int x = 0; x < mContainerView.getChildCount(); x++) {
            QuickSettingsTileView tileView = (QuickSettingsTileView)mContainerView.getChildAt(x);
            delay += 100;
            flipOtherTiles(tileView, delay);
        }
    }

    @Override
    public final void onClick(View v) {
        mOnClick.onClick(v);
        ContentResolver resolver = mContext.getContentResolver();
        boolean shouldCollapse = Settings.System.getInt(resolver, Settings.System.QS_COLLAPSE_PANEL, 0) == 1;
        if (shouldCollapse) {
            mQsc.mBar.collapseAllPanels(true);
        }
        if (isEnabled()) {
            doFlip();
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
        Resources r = mContext.getResources();
        int columnCount;
        if (r.getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            columnCount = getMaxColumns(mContext, Configuration.ORIENTATION_PORTRAIT);
        } else {
            columnCount = getMaxColumns(mContext, Configuration.ORIENTATION_LANDSCAPE);
        }
        ((QuickSettingsContainerView) mContainerView).setColumnCount(columnCount);
        updateTileTextSize(columnCount);
    }

    protected void setTileBackground() {
        ContentResolver mContentResolver = mContext.getContentResolver();
        int tileBg = Settings.System.getInt(mContentResolver,
                Settings.System.QUICK_SETTINGS_BACKGROUND_STYLE, 2);
        int blueDark = Settings.System.getInt(mContentResolver,
                Settings.System.RANDOM_COLOR_ONE, android.R.color.holo_blue_dark);
        int greenDark = Settings.System.getInt(mContentResolver,
                Settings.System.RANDOM_COLOR_TWO, android.R.color.holo_green_dark);
        int redDark = Settings.System.getInt(mContentResolver,
                Settings.System.RANDOM_COLOR_THREE, android.R.color.holo_red_dark);
        int orangeDark = Settings.System.getInt(mContentResolver,
                Settings.System.RANDOM_COLOR_FOUR, android.R.color.holo_orange_dark);
        int purple = Settings.System.getInt(mContentResolver,
                Settings.System.RANDOM_COLOR_FIVE, android.R.color.holo_purple);
        int blueBright = Settings.System.getInt(mContentResolver,
                Settings.System.RANDOM_COLOR_SIX, android.R.color.holo_blue_bright);
        if (tileBg == 1) {
            int tileBgColor = Settings.System.getInt(mContentResolver,
                    Settings.System.QUICK_SETTINGS_BACKGROUND_COLOR, 0xFF000000);
            mTile.setBackgroundColor(tileBgColor);
        } else if (tileBg == 0) {
            int[] Colors = new int[] {
                blueDark,
                greenDark,
                redDark,
                orangeDark,
                purple,
                blueBright
            };
            Random generator = new Random();
            mTile.setBackgroundColor(Colors[generator.nextInt(Colors.length)]);
        } else {
            mTile.setBackgroundResource(R.drawable.qs_tile_background);
        }
    }
}
