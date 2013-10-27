/*
 * Copyright (C) 2013 SlimRoms Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.systemui.statusbar;

import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Log;

import com.android.systemui.R;
import com.android.systemui.statusbar.NavigationBarView;
import com.android.systemui.statusbar.phone.PanelBar;

import java.util.List;

public class TransparencyManager {

    private static final String TAG = TransparencyManager.class.getSimpleName();

    private static final int KEYGUARD_BACKGROUND_OVERLAY_COLOR = 0x70000000;
    public static final int DEFAULT_BACKGROUND_OVERLAY_COLOR = 0x00000000;

    private NavigationBarView mNavbar;
    private PanelBar mStatusbar;

    private StyleInfo mNavbarInfo = new StyleInfo();
    private StyleInfo mStatusbarInfo = new StyleInfo();

    private final Context mContext;
    private ContentResolver resolver;

    private Handler mHandler = new Handler();

    private boolean mIsHomeShowing;
    public boolean mIsKeyguardShowing;
    private int mStatusbarAlphaMode;
    private int mNavbarAlphaMode;
    private int mUnderlayColor = DEFAULT_BACKGROUND_OVERLAY_COLOR;

    private int mLockscreenMode;
    private float mLockscreenAlpha;

    private KeyguardManager mKeyguardManager;
    private ActivityManager mActivityManager;

    // not urgently needed this class but let us keep it for
    // future use when we decide to extend the StyleInfos
    private static class StyleInfo {
        float alpha;
    }

    private final Runnable updateTransparencyRunnable = new Runnable() {
        @Override
        public void run() {
            doTransparentUpdate();
        }
    };

    public TransparencyManager(Context context) {
        mContext = context;
        resolver = mContext.getContentResolver();

        mKeyguardManager = (KeyguardManager) mContext.getSystemService(Context.KEYGUARD_SERVICE);
        mActivityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        context.registerReceiver(new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                update();
            }
        }, intentFilter);

        SettingsObserver settingsObserver = new SettingsObserver(new Handler());
        settingsObserver.observe();
    }

    public void update() {
        mHandler.removeCallbacks(updateTransparencyRunnable);
        mHandler.postDelayed(updateTransparencyRunnable, 50);
    }

    public void setNavbar(NavigationBarView n) {
        mNavbar = n;
    }

    public void setStatusbar(PanelBar s) {
        mStatusbar = s;
    }

    public void setNavBarOverlay(float alpha, int color, boolean resetNavbar) {
        if (mNavbar == null) {
            return;
        }
        if (!resetNavbar) {
            mNavbar.setBackgroundAlpha(0.0f,
                manipulateAlpha(color, alpha), -2, false);
        } else {
            mNavbar.setBackgroundAlpha(0.0f,
                DEFAULT_BACKGROUND_OVERLAY_COLOR, -2, false);
        }
    }

    private void doTransparentUpdate() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                mIsHomeShowing = isLauncherShowing();
                mIsKeyguardShowing = isKeyguardShowing();
                return null;
            }

            @Override
            protected void onPostExecute(Void v) {
                int underlay = DEFAULT_BACKGROUND_OVERLAY_COLOR;
                int overlay = DEFAULT_BACKGROUND_OVERLAY_COLOR;
                float alpha = 1;
                if (mNavbar != null) {
                    if (mNavbarAlphaMode == 2) {
                        alpha = 1;
                    } else if (mIsKeyguardShowing && mNavbarAlphaMode == 1) {
                        alpha = mLockscreenMode == 1 && mLockscreenAlpha > mNavbarInfo.alpha
                                    ? mLockscreenAlpha : mNavbarInfo.alpha;
                        overlay = KEYGUARD_BACKGROUND_OVERLAY_COLOR;
                        underlay = mUnderlayColor;
                    } else if (mIsKeyguardShowing) {
                        overlay = KEYGUARD_BACKGROUND_OVERLAY_COLOR;
                    } else if (mIsHomeShowing) {
                        alpha = mNavbarInfo.alpha;
                    }
                    mNavbar.setBackgroundAlpha(alpha, overlay, underlay,
                        mIsKeyguardShowing || mIsHomeShowing);
                }
                if (mStatusbar != null) {
                    alpha = 1;
                    if (mStatusbarAlphaMode == 2) {
                        alpha = 1;
                    } else if (mIsKeyguardShowing && mStatusbarAlphaMode == 1 || mIsHomeShowing
                            && !mIsKeyguardShowing) {
                        alpha = mStatusbarInfo.alpha;
                    }
                    mStatusbar.setBackgroundAlpha(alpha, 0, 0,
                            mIsKeyguardShowing || mIsHomeShowing);
                }
            }
        }.execute();
    }

    private boolean isLauncherShowing() {
        try {
            final List<ActivityManager.RecentTaskInfo> recentTasks =
                mActivityManager.getRecentTasksForUser(
                        1, ActivityManager.RECENT_WITH_EXCLUDED,
                        UserHandle.CURRENT.getIdentifier());
            if (recentTasks.size() > 0) {
                ActivityManager.RecentTaskInfo recentInfo = recentTasks.get(0);
                Intent intent = new Intent(recentInfo.baseIntent);
                if (recentInfo.origActivity != null) {
                    intent.setComponent(recentInfo.origActivity);
                }
                if (isCurrentHomeActivity(intent.getComponent(), null)) {
                    return true;
                }
            }
        } catch(Exception ignore) {
        }
        return false;
    }

    private boolean isKeyguardShowing() {
        if (mKeyguardManager == null) {
            return false;
        }
        return mKeyguardManager.isKeyguardLocked();
    }

    private boolean isCurrentHomeActivity(ComponentName component, ActivityInfo homeInfo) {
        if (homeInfo == null) {
            homeInfo = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
                    .resolveActivityInfo(mContext.getPackageManager(), 0);
        }
        return homeInfo != null
                && homeInfo.packageName.equals(component.getPackageName())
                && homeInfo.name.equals(component.getClassName());
    }

    class SettingsObserver extends ContentObserver {
        SettingsObserver(Handler handler) {
            super(handler);
        }

        void observe() {
            resolver.registerContentObserver(Settings.System.getUriFor(
                    Settings.System.NAVIGATION_BAR_ALPHA), false, this);
            resolver.registerContentObserver(Settings.System.getUriFor(
                    Settings.System.STATUSBAR_BACKGROUND_ALPHA), false, this);
            resolver.registerContentObserver(Settings.System.getUriFor(
                    Settings.System.STATUSBAR_BACKGROUND_ALPHA_MODE), false, this);
            resolver.registerContentObserver(Settings.System.getUriFor(
                    Settings.System.NAVIGATION_BAR_ALPHA_MODE), false, this);
            resolver.registerContentObserver(Settings.System.getUriFor(
                    Settings.System.LOCKSCREEN_BACKGROUND), false, this);
            resolver.registerContentObserver(Settings.System.getUriFor(
                    Settings.System.LOCKSCREEN_BACKGROUND_ALPHA), false, this);
            updateSettings();
        }

        @Override
        public void onChange(boolean selfChange) {
            updateSettings();
        }
    }

    protected void updateSettings() {
        final float defaultAlpha = new Float(mContext.getResources().getInteger(
                R.integer.status_nav_bar_transparency) / 255);
        mNavbarInfo.alpha = Settings.System.getFloat(resolver,
                Settings.System.NAVIGATION_BAR_ALPHA, defaultAlpha);
        mStatusbarInfo.alpha = Settings.System.getFloat(resolver,
                Settings.System.STATUSBAR_BACKGROUND_ALPHA, defaultAlpha);
        mStatusbarAlphaMode = Settings.System.getInt(resolver,
                Settings.System.STATUSBAR_BACKGROUND_ALPHA_MODE, 2);
        mNavbarAlphaMode = Settings.System.getInt(resolver,
                Settings.System.NAVIGATION_BAR_ALPHA_MODE, 2);
        mLockscreenMode = Settings.System.getInt(resolver,
                Settings.System.LOCKSCREEN_BACKGROUND_MODE, 2);
        mLockscreenAlpha = Settings.System.getFloat(resolver,
                Settings.System.LOCKSCREEN_BACKGROUND_ALPHA, 1f);

        mUnderlayColor = DEFAULT_BACKGROUND_OVERLAY_COLOR;
        if (mLockscreenMode == 0) {
            String lockscreenBackground = Settings.System.getStringForUser(resolver,
                    Settings.System.LOCKSCREEN_BACKGROUND, UserHandle.USER_CURRENT);
            if (lockscreenBackground != null) {
                mUnderlayColor = Integer.parseInt(lockscreenBackground);
                mUnderlayColor = manipulateAlpha(mUnderlayColor, mLockscreenAlpha);
            }
        }

        update();
    }

    private static int manipulateAlpha(int color, float alpha){
        return Color.argb((int) (alpha * 255), Color.red(color), Color.green(color), Color.blue(color));
    }

}
