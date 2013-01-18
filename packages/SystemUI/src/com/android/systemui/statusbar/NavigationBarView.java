/*
 * Copyright (C) 2008 The Android Open Source Project
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

import java.io.File;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.LayoutTransition;
import android.app.StatusBarManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.graphics.ColorFilterMaker;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Slog;
import android.util.TypedValue;
import android.view.animation.AccelerateInterpolator;
import android.view.Display;
import android.view.DisplayInfo;
import android.view.IWindowManager;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Surface;
import android.view.WindowManager;
import android.view.WindowManagerGlobal;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.android.internal.statusbar.IStatusBarService;
import com.android.systemui.navbar.NavbarAction;
import com.android.systemui.R;
import com.android.systemui.statusbar.BaseStatusBar;
import com.android.systemui.statusbar.DelegateViewHelper;
import com.android.systemui.statusbar.policy.KeyButtonView;
import com.android.systemui.statusbar.policy.ExtensibleKeyButtonView;
import com.android.systemui.statusbar.policy.RecentsKeyButtonView;

public class NavigationBarView extends LinearLayout {
    final static boolean DEBUG = false;
    final static String TAG = "PhoneStatusBar/NavigationBarView";

    final static boolean NAVBAR_ALWAYS_AT_RIGHT = true;

    // slippery nav bar when everything is disabled, e.g. during setup
    final static boolean SLIPPERY_WHEN_DISABLED= true;

    final static boolean ANIMATE_HIDE_TRANSITION = false; // turned off because it introduces unsightly delay when videos goes to full screen

    protected IStatusBarService mBarService;
    protected IWindowManager mWindowManagerService;
    final Display mDisplay;
    View mCurrentView = null;
    View[] mRotatedViews = new View[4];

    int mBarSize;
    boolean mVertical;
    boolean mScreenOn;

    boolean mHidden, mLowProfile, mShowMenu;
    int mDisabledFlags = 0;
    int mNavigationIconHints = 0;

    private Drawable mBackIcon, mBackLandIcon, mBackAltIcon, mBackAltLandIcon;

    public DelegateViewHelper mDelegateHelper;

    // workaround for LayoutTransitions leaving the nav buttons in a weird state (bug 5549288)
    final static boolean WORKAROUND_INVALID_LAYOUT = true;
    final static int MSG_CHECK_INVALID_LAYOUT = 8686;

    int mNumberOfButtons = 3;

    // Will determine if NavBar goes to the left side in Landscape Mode
    private boolean mLeftyMode;

    // Device types
    private static final int DEVICE_PHONE = 0;
    private static final int DEVICE_PHABLET = 1;
    private static final int DEVICE_TABLET = 2;

    // Device type reference
    private static int mDeviceType = -1;

    public String[] mClickActions = new String[7];
    public String[] mLongpressActions = new String[7];
    public String[] mPortraitIcons = new String[7];

    public final static int StockButtonsQty = 3;
    public final static String[] StockClickActions = {
        NavbarAction.ACTION_BACK,
        NavbarAction.ACTION_HOME,
        NavbarAction.ACTION_RECENTS,
        NavbarAction.ACTION_NULL,
        NavbarAction.ACTION_NULL,
        NavbarAction.ACTION_NULL,
        NavbarAction.ACTION_NULL };
    public final static String[] StockLongpress = {
        NavbarAction.ACTION_NULL,
        NavbarAction.ACTION_NULL,
        NavbarAction.ACTION_NULL,
        NavbarAction.ACTION_NULL,
        NavbarAction.ACTION_NULL,
        NavbarAction.ACTION_NULL,
        NavbarAction.ACTION_NULL };

    FrameLayout rot0;
    FrameLayout rot90;

    //Definitions for NavBar Menu button customization
    public final static int SHOW_LEFT_MENU = 1;
    public final static int SHOW_RIGHT_MENU = 0;
    public final static int SHOW_BOTH_MENU = 2;
    public final static int SHOW_DONT = 4;

    public final static int VISIBILITY_SYSTEM = 0;
    public final static int VISIBILITY_SYSTEM_AND_INVIZ = 3;
    public final static int VISIBILITY_NEVER = 1;
    public final static int VISIBILITY_ALWAYS = 2;

    public static final int KEY_MENU_RIGHT = 2;
    public static final int KEY_MENU_LEFT = 5;

    private int mMenuVisbility;
    private int mMenuLocation;
    private boolean mHasBigMenuButton = false;

    private class H extends Handler {
        public void handleMessage(Message m) {
            switch (m.what) {
                case MSG_CHECK_INVALID_LAYOUT:
                    final String how = "" + m.obj;
                    final int w = getWidth();
                    final int h = getHeight();
                    final int vw = mCurrentView.getWidth();
                    final int vh = mCurrentView.getHeight();

                    if (h != vh || w != vw) {
                        Slog.w(TAG, String.format(
                            "*** Invalid layout in navigation bar (%s this=%dx%d cur=%dx%d)",
                            how, w, h, vw, vh));
                        if (WORKAROUND_INVALID_LAYOUT) {
                            requestLayout();
                        }
                    }
                    break;
            }
        }
    }

    public void setDelegateView(View view) {
        mDelegateHelper.setDelegateView(view);
        //mDelegateHelper.setLefty(mLeftyMode);
    }

    public void setBar(BaseStatusBar phoneStatusBar) {
        mDelegateHelper.setBar(phoneStatusBar);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mDelegateHelper != null) {
            mDelegateHelper.onInterceptTouchEvent(event);
        }
        return true;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        return mDelegateHelper.onInterceptTouchEvent(event);
    }

    private H mHandler = new H();

    public View getLeftMenuButton() {
        return mCurrentView.findViewById(R.id.menu_left);
    }

    public View getRightMenuButton() {
        return mCurrentView.findViewById(R.id.menu);
    }

    public View getRecentsButton() {
        return mCurrentView.findViewById(R.id.recent_apps);
    }

    public View getBackButton() {
        return mCurrentView.findViewById(R.id.back);
    }

    public View getHomeButton() {
        return mCurrentView.findViewById(R.id.home);
    }

    // for when home is disabled, but search isn't
    public View getSearchLight() {
        return mCurrentView.findViewById(R.id.search_light);
    }

    public NavigationBarView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mWindowManagerService = WindowManagerGlobal.getWindowManagerService();

        mHidden = false;

        mDisplay = ((WindowManager)context.getSystemService(
                Context.WINDOW_SERVICE)).getDefaultDisplay();
        mBarService = IStatusBarService.Stub.asInterface(
                ServiceManager.getService(Context.STATUS_BAR_SERVICE));

        final Resources res = mContext.getResources();
        mBarSize = res.getDimensionPixelSize(R.dimen.navigation_bar_size);
        mVertical = false;
        mShowMenu = false;
        mDelegateHelper = new DelegateViewHelper(this);

        mBackIcon = res.getDrawable(R.drawable.ic_sysbar_back);
        mBackLandIcon = res.getDrawable(R.drawable.ic_sysbar_back_land);
        mBackAltIcon = res.getDrawable(R.drawable.ic_sysbar_back_ime);
        mBackAltLandIcon = res.getDrawable(R.drawable.ic_sysbar_back_ime);
    }

    private void makeBar() {

        ((LinearLayout) rot0.findViewById(R.id.nav_buttons)).removeAllViews();
        ((LinearLayout) rot0.findViewById(R.id.lights_out)).removeAllViews();
        ((LinearLayout) rot90.findViewById(R.id.nav_buttons)).removeAllViews();
        ((LinearLayout) rot90.findViewById(R.id.lights_out)).removeAllViews();

        for (int i = 0; i <= 1; i++) {
            boolean landscape = (i == 1);

            LinearLayout navButtonLayout = (LinearLayout) (landscape ? rot90
                    .findViewById(R.id.nav_buttons) : rot0
                    .findViewById(R.id.nav_buttons));

            LinearLayout lightsOut = (LinearLayout) (landscape ? rot90
                    .findViewById(R.id.lights_out) : rot0
                    .findViewById(R.id.lights_out));

            // Add the Main Nav Buttons
            mHasBigMenuButton = false;
            String iconUri = "";
            for (int j = 0; j < mNumberOfButtons; j++) {
                ExtensibleKeyButtonView v = generateKey(landscape, mClickActions[j],
                        mLongpressActions[j],
                        mPortraitIcons[j]);
                v.setTag((landscape ? "key_land_" : "key_") + j);
                // v.setNavbarTarget(mNavbarTarget);
                iconUri = mPortraitIcons[j];
                if (iconUri != null && iconUri.length() > 0) {
                    // custom icon from the URI here
                    File f = new File(Uri.parse(iconUri).getPath());
                    if (f.exists()) {
                        v.setImageDrawable(new BitmapDrawable(getResources(), f.getAbsolutePath()));
                    }
                    v.setTint(false);
                } else {
                    v.setImageDrawable(NavbarAction.getInstance(mContext).getIconImage(mClickActions[j]));
                    v.setTint(mClickActions[j].startsWith("**"));
                }
                addButton(navButtonLayout, v, landscape && !mLeftyMode);
                // if we are in LeftyMode, then we want to add to end, like Portrait
                addLightsOutButton(lightsOut, v, landscape && !mLeftyMode, false);

                if (v.getId() == R.id.back){
                        mBackIcon = mBackLandIcon = v.getDrawable();
                }
                if (v.getId() == R.id.menu){
                    mHasBigMenuButton = true;
                }
                if (mNumberOfButtons == 3 && j != (mNumberOfButtons - 1)) {
                    // add separator view here
                    View separator = new View(mContext);
                    separator.setLayoutParams(getSeparatorLayoutParams(landscape));
                    addButton(navButtonLayout, separator, landscape);
                    addLightsOutButton(lightsOut, separator, landscape, true);
                }
            }
            // check to see if we already have a menu button
            if (!mHasBigMenuButton) {  // don't add menu buttons if we already have one
                // add left menu
                if (mMenuLocation != SHOW_DONT) {
                    View leftMenuKey = generateKey(landscape, KEY_MENU_LEFT);
                    // since we didn't add these at the beginning, we need to insert it now
                    // the behavior is backwards from landscape (ie, insert at beginning
                    // if portrait, add to end if landscape
                    addButton(navButtonLayout, leftMenuKey, !landscape || (landscape && mLeftyMode));
                    addLightsOutButton(lightsOut, leftMenuKey, !landscape || (landscape && mLeftyMode), true);

                    View rightMenuKey = generateKey(landscape, KEY_MENU_RIGHT);
                    addButton(navButtonLayout, rightMenuKey, landscape && !mLeftyMode);
                    addLightsOutButton(lightsOut, rightMenuKey, landscape && !mLeftyMode, true);
                }
            }
        }
    }

    private void addLightsOutButton(LinearLayout root, View v, boolean landscape, boolean empty) {

        ImageView addMe = new ImageView(mContext);
        addMe.setLayoutParams(v.getLayoutParams());
        addMe.setImageResource(empty ? R.drawable.ic_sysbar_lights_out_dot_large
                : R.drawable.ic_sysbar_lights_out_dot_small);
        addMe.setScaleType(ImageView.ScaleType.CENTER);
        addMe.setVisibility(empty ? View.INVISIBLE : View.VISIBLE);

        if (landscape)
            root.addView(addMe, 0);
        else
            root.addView(addMe);
    }

    private void addButton(ViewGroup root, View addMe, boolean landscape) {
        if (landscape)
            root.addView(addMe, 0);
        else
            root.addView(addMe);
    }

    private View generateKey(boolean landscape, int keyId) {
        KeyButtonView v = null;
        Resources r = getResources();

        switch (keyId) {

            case KEY_MENU_RIGHT:
                v = new KeyButtonView(mContext, null);
                v.setLayoutParams(getLayoutParams(landscape, (isTablet(mContext)) ? 80 : 40));

                v.setId(R.id.menu);
                v.setCode(KeyEvent.KEYCODE_MENU);
                if (isTablet(mContext)) {
                    v.setImageResource(R.drawable.ic_sysbar_menu_big);
                    v.setVisibility(View.GONE);
                } else {
                    v.setImageResource(landscape ? R.drawable.ic_sysbar_menu_land
                        : R.drawable.ic_sysbar_menu);
                    v.setVisibility(View.INVISIBLE);
                }
                v.setContentDescription(r.getString(R.string.accessibility_menu));
                v.setGlowBackground(landscape ? R.drawable.ic_sysbar_highlight_land
                        : R.drawable.ic_sysbar_highlight);
                break;
            case KEY_MENU_LEFT:
                v = new KeyButtonView(mContext, null);
                v.setLayoutParams(getLayoutParams(landscape, (isTablet(mContext)) ? 80 : 40));

                v.setId(R.id.menu_left);
                v.setCode(KeyEvent.KEYCODE_MENU);
                if (isTablet(mContext)) {
                    v.setImageResource(R.drawable.ic_sysbar_menu_big);
                    v.setVisibility(View.GONE);
                } else {
                    v.setImageResource(landscape ? R.drawable.ic_sysbar_menu_land
                        : R.drawable.ic_sysbar_menu);
                    v.setVisibility(View.INVISIBLE);
                }
                v.setContentDescription(r.getString(R.string.accessibility_menu));
                v.setGlowBackground(landscape ? R.drawable.ic_sysbar_highlight_land
                        : R.drawable.ic_sysbar_highlight);
                break;

        }

        return v;
    }

    private ExtensibleKeyButtonView generateKey(boolean landscape, String clickAction,
            String longpress,
            String iconUri) {

        final int iconSize = 80;
        ExtensibleKeyButtonView v = null;
        if (NavbarAction.ACTION_RECENTS.equals(clickAction)) {
            v = new RecentsKeyButtonView(mContext, null, clickAction, longpress);
        } else {
            v = new ExtensibleKeyButtonView(mContext, null, clickAction, longpress);
        }
        v.setLayoutParams(getLayoutParams(landscape, iconSize));
        v.setGlowBackground(landscape ? R.drawable.ic_sysbar_highlight_land
                : R.drawable.ic_sysbar_highlight);
        return v;
    }

    private LayoutParams getLayoutParams(boolean landscape, float dp) {
        float px = dp * getResources().getDisplayMetrics().density;
        return landscape ?
                new LayoutParams(LayoutParams.MATCH_PARENT, (int) px, 1f) :
                new LayoutParams((int) px, LayoutParams.MATCH_PARENT, 1f);
    }

    private LayoutParams getSeparatorLayoutParams(boolean landscape) {
        float px = 25 * getResources().getDisplayMetrics().density;
        return landscape ?
                new LayoutParams(LayoutParams.MATCH_PARENT, (int) px) :
                new LayoutParams((int) px, LayoutParams.MATCH_PARENT);
    }

    public void notifyScreenOn(boolean screenOn) {
        mScreenOn = screenOn;
        setDisabledFlags(mDisabledFlags, true);
    }

    View.OnTouchListener mLightsOutListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent ev) {
            if (ev.getAction() == MotionEvent.ACTION_DOWN) {
                // even though setting the systemUI visibility below will turn these views
                // on, we need them to come up faster so that they can catch this motion
                // event
                setLowProfile(false, false, false);

                try {
                    mBarService.setSystemUiVisibility(0, View.SYSTEM_UI_FLAG_LOW_PROFILE);
                } catch (android.os.RemoteException ex) {
                }
            }
            return false;
        }
    };

    public void setNavigationIconHints(int hints) {
        setNavigationIconHints(hints, false);
    }

    public void setNavigationIconHints(int hints, boolean force) {
        if (!force && hints == mNavigationIconHints) return;

        if (DEBUG) {
            android.widget.Toast.makeText(mContext,
                "Navigation icon hints = " + hints,
                500).show();
        }

        mNavigationIconHints = hints;
        // We can't gaurantee users will set these buttons as targets
        if (getBackButton() != null) {
            getBackButton().setAlpha((0 != (hints & StatusBarManager.NAVIGATION_HINT_BACK_NOP)) ? 0.5f : 1.0f);
            ((ImageView)getBackButton()).setImageDrawable(
                    (0 != (hints & StatusBarManager.NAVIGATION_HINT_BACK_ALT))
                    ? (mVertical ? mBackAltLandIcon : mBackAltIcon)
                    : (mVertical ? mBackLandIcon : mBackIcon));
        }
        if (getHomeButton()!=null) {
            getHomeButton().setAlpha((0 != (hints & StatusBarManager.NAVIGATION_HINT_HOME_NOP)) ? 0.5f : 1.0f);
        }
        if (getRecentsButton()!=null) {
            getRecentsButton().setAlpha((0 != (hints & StatusBarManager.NAVIGATION_HINT_RECENT_NOP)) ? 0.5f : 1.0f);
        }
        setDisabledFlags(mDisabledFlags, true);
    }

    public void setDisabledFlags(int disabledFlags) {
        setDisabledFlags(disabledFlags, false);
    }

    public void setDisabledFlags(int disabledFlags, boolean force) {
        if (!force && mDisabledFlags == disabledFlags) return;

        mDisabledFlags = disabledFlags;

        final boolean disableHome = ((disabledFlags & View.STATUS_BAR_DISABLE_HOME) != 0);
        final boolean disableRecent = ((disabledFlags & View.STATUS_BAR_DISABLE_RECENT) != 0);
        final boolean disableBack = ((disabledFlags & View.STATUS_BAR_DISABLE_BACK) != 0)
              && ((mNavigationIconHints & StatusBarManager.NAVIGATION_HINT_BACK_ALT) == 0);
        final boolean disableSearch = ((disabledFlags & View.STATUS_BAR_DISABLE_SEARCH) != 0);

        if (!isTablet(mContext) && SLIPPERY_WHEN_DISABLED) {
            // Tabletmode doesn't deal with slippery
            setSlippery(disableHome && disableRecent && disableBack && disableSearch);
        }
        if (!mScreenOn && mCurrentView != null) {
            ViewGroup navButtons = (ViewGroup) mCurrentView.findViewById(R.id.nav_buttons);
            LayoutTransition lt = navButtons == null ? null : navButtons.getLayoutTransition();
            if (lt != null) {
                lt.disableTransitionType(
                        LayoutTransition.CHANGE_APPEARING | LayoutTransition.CHANGE_DISAPPEARING |
                        LayoutTransition.APPEARING | LayoutTransition.DISAPPEARING);
            }
        }
        for (int j = 0; j < mNumberOfButtons; j++) {
            View v = (View) findViewWithTag((mVertical ? "key_land_" : "key_") + j);
            if (v != null) {
                int vid = v.getId();
                if (vid == R.id.back) {
                    v.setVisibility(disableBack ? View.INVISIBLE : View.VISIBLE);
                } else if (vid == R.id.recent_apps) {
                    v.setVisibility(disableRecent ? View.INVISIBLE : View.VISIBLE);
                } else { // treat all other buttons as same rule as home
                    v.setVisibility(disableHome ? View.INVISIBLE : View.VISIBLE);
                }

            }
        }
        getSearchLight().setVisibility((disableHome && !disableSearch) ? View.VISIBLE : View.GONE);
    }

    public void setSlippery(boolean newSlippery) {
        WindowManager.LayoutParams lp = (WindowManager.LayoutParams) getLayoutParams();
        if (lp != null) {
            boolean oldSlippery = (lp.flags & WindowManager.LayoutParams.FLAG_SLIPPERY) != 0;
            if (!oldSlippery && newSlippery) {
                lp.flags |= WindowManager.LayoutParams.FLAG_SLIPPERY;
            } else if (oldSlippery && !newSlippery) {
                lp.flags &= ~WindowManager.LayoutParams.FLAG_SLIPPERY;
            } else {
                return;
            }
            WindowManager wm = (WindowManager)getContext().getSystemService(Context.WINDOW_SERVICE);
            wm.updateViewLayout(this, lp);
        }
    }

    public boolean getMenuVisibility() {
        return mShowMenu;
    }

    public void setMenuVisibility(final boolean show) {
        setMenuVisibility(show, false);
    }

    public void setMenuVisibility(final boolean show, final boolean force) {

        if (!force && mShowMenu == show)
            return;

        if ((mMenuLocation == SHOW_DONT) || mHasBigMenuButton) {
            return;
        }

        mShowMenu = show;
        boolean localShow = show;

        ImageView leftButton = (ImageView) getLeftMenuButton();
        ImageView rightButton = (ImageView) getRightMenuButton();

        switch (mMenuVisbility) {
            case VISIBILITY_ALWAYS:
                localShow = true;
            case VISIBILITY_SYSTEM:
                if (isTablet(mContext)) {
                    rightButton.setImageResource(R.drawable.ic_sysbar_menu_big);
                    leftButton.setImageResource(R.drawable.ic_sysbar_menu_big);
                } else {
                    rightButton.setImageResource(mVertical ? R.drawable.ic_sysbar_menu_land
                        : R.drawable.ic_sysbar_menu);
                    leftButton.setImageResource(mVertical ? R.drawable.ic_sysbar_menu_land
                            : R.drawable.ic_sysbar_menu);
                }
                break;
            case VISIBILITY_NEVER:
                leftButton
                        .setImageResource(R.drawable.ic_sysbar_menu_inviz);
                rightButton
                        .setImageResource(R.drawable.ic_sysbar_menu_inviz);
                localShow = true;
                break;
            case VISIBILITY_SYSTEM_AND_INVIZ:
                if (localShow) {
                    if (isTablet(mContext)) {
                        rightButton.setImageResource(R.drawable.ic_sysbar_menu_big);
                        leftButton.setImageResource(R.drawable.ic_sysbar_menu_big);
                    } else {
                        rightButton.setImageResource(mVertical ? R.drawable.ic_sysbar_menu_land
                            : R.drawable.ic_sysbar_menu);
                        leftButton.setImageResource(mVertical ? R.drawable.ic_sysbar_menu_land
                                : R.drawable.ic_sysbar_menu);
                    }
                } else {
                    localShow = true;
                    leftButton
                            .setImageResource(R.drawable.ic_sysbar_menu_inviz);
                    rightButton
                            .setImageResource(R.drawable.ic_sysbar_menu_inviz);
                }
                break;
        }

        // do this after just in case show was changed
        // Tablet menu buttons should not take up space when hidden.
        switch (mMenuLocation) {
            case SHOW_BOTH_MENU:
                if (isTablet(mContext)) {
                    leftButton.setVisibility(localShow ? View.VISIBLE : View.GONE);
                    rightButton.setVisibility(localShow ? View.VISIBLE : View.GONE);
                } else {
                    leftButton.setVisibility(localShow ? View.VISIBLE : View.INVISIBLE);
                    rightButton.setVisibility(localShow ? View.VISIBLE : View.INVISIBLE);
                }
                break;
            case SHOW_LEFT_MENU:
                if (isTablet(mContext)) {
                    leftButton.setVisibility(localShow ? View.VISIBLE : View.GONE);
                } else {
                    leftButton.setVisibility(localShow ? View.VISIBLE : View.INVISIBLE);
                }
                rightButton.setVisibility((isTablet(mContext)) ? View.GONE : View.INVISIBLE);
                break;
            default:
            case SHOW_RIGHT_MENU:
                leftButton.setVisibility((isTablet(mContext)) ? View.GONE : View.INVISIBLE);
                if (isTablet(mContext)) {
                    rightButton.setVisibility(localShow ? View.VISIBLE : View.GONE);
                } else {
                    rightButton.setVisibility(localShow ? View.VISIBLE : View.INVISIBLE);
                }
                break;
        }
    }

    public void setLowProfile(final boolean lightsOut) {
        setLowProfile(lightsOut, true, false);
    }

    public void setLowProfile(final boolean lightsOut, final boolean animate, final boolean force) {
        if (!force && lightsOut == mLowProfile) return;

        mLowProfile = lightsOut;

        if (DEBUG) Slog.d(TAG, "setting lights " + (lightsOut?"out":"on"));

        final View navButtons = mCurrentView.findViewById(R.id.nav_buttons);
        final View lowLights = mCurrentView.findViewById(R.id.lights_out);

        // ok, everyone, stop it right there
        navButtons.animate().cancel();
        lowLights.animate().cancel();

        if (!animate) {
            navButtons.setAlpha(lightsOut ? 0f : 1f);

            lowLights.setAlpha(lightsOut ? 1f : 0f);
            lowLights.setVisibility(lightsOut ? View.VISIBLE : View.GONE);
        } else {
            navButtons.animate()
                .alpha(lightsOut ? 0f : 1f)
                .setDuration(lightsOut ? 750 : 250)
                .start();

            lowLights.setOnTouchListener(mLightsOutListener);
            if (lowLights.getVisibility() == View.GONE) {
                lowLights.setAlpha(0f);
                lowLights.setVisibility(View.VISIBLE);
            }
            lowLights.animate()
                .alpha(lightsOut ? 1f : 0f)
                .setDuration(lightsOut ? 750 : 250)
                .setInterpolator(new AccelerateInterpolator(2.0f))
                .setListener(lightsOut ? null : new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator _a) {
                        lowLights.setVisibility(View.GONE);
                    }
                })
                .start();
        }
    }

    public void setHidden(final boolean hide) {
        if (hide == mHidden) return;

        mHidden = hide;
        Slog.d(TAG,
            (hide ? "HIDING" : "SHOWING") + " navigation bar");

        // bring up the lights no matter what
        setLowProfile(false);
    }

    @Override
    public void onFinishInflate() {
        rot0 = (FrameLayout) findViewById(R.id.rot0);
        rot90 = (FrameLayout) findViewById(R.id.rot90);

        mRotatedViews[Surface.ROTATION_0] =
            mRotatedViews[Surface.ROTATION_180] = findViewById(R.id.rot0);

        mRotatedViews[Surface.ROTATION_90] = findViewById(R.id.rot90);

        mRotatedViews[Surface.ROTATION_270] = NAVBAR_ALWAYS_AT_RIGHT
                ? findViewById(R.id.rot90)
                : findViewById(R.id.rot270);

        for (View v : mRotatedViews) {
            // this helps avoid drawing artifacts with glowing navigation keys
            ViewGroup group = (ViewGroup) v.findViewById(R.id.nav_buttons);
            group.setMotionEventSplittingEnabled(false);
        }
        mCurrentView = mRotatedViews[Surface.ROTATION_0];

        updateNavigationBarBackground();

        // this takes care of making the buttons
        SettingsObserver settingsObserver = new SettingsObserver(new Handler());
        settingsObserver.observe();
    }

    public void reorient() {
        final int rot = mDisplay.getRotation();
        for (int i=0; i<4; i++) {
            mRotatedViews[i].setVisibility(View.GONE);
        }
        if (isPhone(mContext)) {
            mCurrentView = mRotatedViews[rot];
        } else { // Tablet or Phablet.  Need to stay at Rot_0
            mCurrentView = mRotatedViews[Surface.ROTATION_0];
        }
        mCurrentView.setVisibility(View.VISIBLE);

        // force the low profile & disabled states into compliance
        setLowProfile(mLowProfile, false, true /* force */);
        setDisabledFlags(mDisabledFlags, true /* force */);
        setMenuVisibility(mShowMenu, true /* force */);
        setNavigationIconHints(mNavigationIconHints, true);
        if (DEBUG) {
            Slog.d(TAG, "reorient(): rot=" + mDisplay.getRotation());
        }
    }

    private static int getScreenType(Context con) {
        if (mDeviceType == -1) {
            WindowManager wm = (WindowManager)con.getSystemService(Context.WINDOW_SERVICE);
            DisplayInfo outDisplayInfo = new DisplayInfo();
            wm.getDefaultDisplay().getDisplayInfo(outDisplayInfo);
            int shortSize = Math.min(outDisplayInfo.logicalHeight, outDisplayInfo.logicalWidth);
            int shortSizeDp = shortSize * DisplayMetrics.DENSITY_DEFAULT / outDisplayInfo.logicalDensityDpi;
            if (shortSizeDp < 600) {
                // 0-599dp: "phone" UI with a separate status & navigation bar
                mDeviceType =  DEVICE_PHONE;
            } else if (shortSizeDp < 720) {
                // 600-719dp: "phone" UI with modifications for larger screens
                mDeviceType = DEVICE_PHABLET;
            } else {
                // 720dp: "tablet" UI with a single combined status & navigation bar
                mDeviceType = DEVICE_TABLET;
            }
        }
        return mDeviceType;
    }

    public static boolean isPhone(Context con) {
        return getScreenType(con) == DEVICE_PHONE;
    }

    public static boolean isPhablet(Context con) {
        return getScreenType(con) == DEVICE_PHABLET;
    }

    public static boolean isTablet(Context con) {
        return getScreenType(con) == DEVICE_TABLET;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        mDelegateHelper.setInitialTouchRegion(this);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (DEBUG) Slog.d(TAG, String.format(
                    "onSizeChanged: (%dx%d) old: (%dx%d)", w, h, oldw, oldh));

        final boolean newVertical = w > 0 && h > w;
        if (newVertical != mVertical) {
            mVertical = newVertical;
            //Slog.v(TAG, String.format("onSizeChanged: h=%d, w=%d, vert=%s", h, w, mVertical?"y":"n"));
            reorient();
        }

        postCheckForInvalidLayout("sizeChanged");
        super.onSizeChanged(w, h, oldw, oldh);
    }

    /*
    @Override
    protected void onLayout (boolean changed, int left, int top, int right, int bottom) {
        if (DEBUG) Slog.d(TAG, String.format(
                    "onLayout: %s (%d,%d,%d,%d)",
                    changed?"changed":"notchanged", left, top, right, bottom));
        super.onLayout(changed, left, top, right, bottom);
    }

    // uncomment this for extra defensiveness in WORKAROUND_INVALID_LAYOUT situations: if all else
    // fails, any touch on the display will fix the layout.
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (DEBUG) Slog.d(TAG, "onInterceptTouchEvent: " + ev.toString());
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            postCheckForInvalidLayout("touch");
        }
        return super.onInterceptTouchEvent(ev);
    }
    */

    class SettingsObserver extends ContentObserver {
        SettingsObserver(Handler handler) {
            super(handler);
        }

        void observe() {
            ContentResolver resolver = mContext.getContentResolver();

            resolver.registerContentObserver(Settings.System.getUriFor(
                    Settings.System.MENU_LOCATION), false, this);
            resolver.registerContentObserver(Settings.System.getUriFor(
                    Settings.System.MENU_VISIBILITY), false, this);
            resolver.registerContentObserver(Settings.System.getUriFor(
                    Settings.System.NAVIGATION_BAR_BUTTONS_QTY), false, this);
            resolver.registerContentObserver(Settings.System.getUriFor(
                    Settings.System.NAVIGATION_BAR_LEFTY_MODE), false, this);
            resolver.registerContentObserver(Settings.System.getUriFor(
                    Settings.System.NAVIGATION_BAR_BACKGROUND_STYLE), false, this);
            resolver.registerContentObserver(Settings.System.getUriFor(
                    Settings.System.NAVIGATION_BAR_BACKGROUND_COLOR), false, this);

            for (int j = 0; j < 7; j++) { // watch all 7 settings for changes.
                resolver.registerContentObserver(Settings.System.getUriFor(
                        Settings.System.NAVIGATION_CUSTOM_ACTIVITIES[j]), false, this);
                resolver.registerContentObserver(Settings.System.getUriFor(
                        Settings.System.NAVIGATION_LONGPRESS_ACTIVITIES[j]), false, this);
                resolver.registerContentObserver(Settings.System.getUriFor(
                        Settings.System.NAVIGATION_CUSTOM_APP_ICONS[j]),false,
                        this);
            }
            updateSettings();
            updateNavigationBarBackground();
        }

        @Override
        public void onChange(boolean selfChange) {
            updateSettings();
            updateNavigationBarBackground();
        }
    }

    protected void updateNavigationBarBackground() {
        try {
            boolean showNav = mWindowManagerService.hasNavigationBar();
            if (showNav) {
                // NavigationBar background color
                int defaultBg = Settings.System.getInt(mContext.getContentResolver(),
                        Settings.System.NAVIGATION_BAR_BACKGROUND_STYLE, 2);
                int navbarBackgroundColor = Settings.System.getInt(mContext.getContentResolver(),
                        Settings.System.NAVIGATION_BAR_BACKGROUND_COLOR, 0xFF000000);

                if (defaultBg == 0) {
                    this.setBackgroundColor(navbarBackgroundColor);
                } else if (defaultBg == 1) {
                    this.setBackgroundResource(R.drawable.nav_bar_bg);
                    this.getBackground().setColorFilter(ColorFilterMaker.
                            changeColorAlpha(navbarBackgroundColor, .32f, 0f));
                } else {
                    this.setBackground(mContext.getResources().getDrawable(
                            R.drawable.nav_bar_bg));
                }
            }
        } catch (RemoteException ex) {
            // no window manager? good luck with that
        }
    }

    protected void updateSettings() {
        ContentResolver resolver = mContext.getContentResolver();

        mMenuLocation = Settings.System.getInt(resolver,
                Settings.System.MENU_LOCATION, SHOW_RIGHT_MENU);
        mMenuVisbility = Settings.System.getInt(resolver,
                Settings.System.MENU_VISIBILITY, VISIBILITY_SYSTEM);
        mLeftyMode = Settings.System.getBoolean(resolver,
                Settings.System.NAVIGATION_BAR_LEFTY_MODE, false);
        mNumberOfButtons = Settings.System.getInt(resolver,
                Settings.System.NAVIGATION_BAR_BUTTONS_QTY, 0);
        if (mNumberOfButtons == 0) {
            mNumberOfButtons = StockButtonsQty;
            Settings.System.putInt(resolver,
                    Settings.System.NAVIGATION_BAR_BUTTONS_QTY, StockButtonsQty);
        }

        for (int j = 0; j < 7; j++) {
            mClickActions[j] = Settings.System.getString(resolver,
                    Settings.System.NAVIGATION_CUSTOM_ACTIVITIES[j]);
            if (mClickActions[j] == null) {
                mClickActions[j] = StockClickActions[j];
                Settings.System.putString(resolver,
                        Settings.System.NAVIGATION_CUSTOM_ACTIVITIES[j], mClickActions[j]);
            }

            mLongpressActions[j] = Settings.System.getString(resolver,
                    Settings.System.NAVIGATION_LONGPRESS_ACTIVITIES[j]);
            if (mLongpressActions[j] == null) {
                mLongpressActions[j] = StockLongpress[j];
                Settings.System.putString(resolver,
                        Settings.System.NAVIGATION_LONGPRESS_ACTIVITIES[j], mLongpressActions[j]);
            }
            mPortraitIcons[j] = Settings.System.getString(resolver,
                    Settings.System.NAVIGATION_CUSTOM_APP_ICONS[j]);
            if (mPortraitIcons[j] == null) {
                mPortraitIcons[j] = "";
                Settings.System.putString(resolver,
                        Settings.System.NAVIGATION_CUSTOM_APP_ICONS[j], "");
            }
        }
        makeBar();
        setMenuVisibility(mShowMenu);
    }

    public void themeHasChanged() {
        updateSettings();
        updateNavigationBarBackground();
    }

    private void postCheckForInvalidLayout(final String how) {
        mHandler.obtainMessage(MSG_CHECK_INVALID_LAYOUT, 0, 0, how).sendToTarget();
    }
}
