/*
 * Copyright (C) 2013 AOKP by Mike Wilson - Zaphod-Beeblebrox && Steve Spear - Stevespear426
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

package com.android.internal.util.action;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.provider.Settings;

public class ActionConstants {

    public static final String ASSIST_ICON_METADATA_NAME = "com.android.systemui.action_assist_icon";

    public final static int SWIPE_LEFT = 0;
    public final static int SWIPE_RIGHT = 1;
    public final static int SWIPE_DOWN = 2;
    public final static int SWIPE_UP = 3;
    public final static int TAP_DOUBLE = 4;
    public final static int PRESS_LONG = 5;
    public final static int SPEN_REMOVE = 6;
    public final static int SPEN_INSERT = 7;

    /* Adding Actions here will automatically add them to NavBar actions in ROMControl.
     * **app** must remain the last action.  Add other actions before that final action.
     */
    public static enum ActionConstant {
        ACTION_HOME          { @Override public String value() { return "**home**";}},
        ACTION_BACK          { @Override public String value() { return "**back**";}},
        ACTION_MENU          { @Override public String value() { return "**menu**";}},
        ACTION_SEARCH        { @Override public String value() { return "**search**";}},
        ACTION_RECENTS       { @Override public String value() { return "**recents**";}},
        ACTION_ASSIST        { @Override public String value() { return "**assist**";}},
        ACTION_POWER         { @Override public String value() { return "**power**";}},
        ACTION_NOTIFICATIONS { @Override public String value() { return "**notifications**";}},
        ACTION_CLOCKOPTIONS  { @Override public String value() { return "**clockoptions**";}},
        ACTION_VOICEASSIST   { @Override public String value() { return "**voiceassist**";}},
        ACTION_LAST_APP      { @Override public String value() { return "**lastapp**";}},
        ACTION_RECENTS_GB    { @Override public String value() { return "**recentsgb**";}},
        ACTION_TORCH         { @Override public String value() { return "**torch**";}},
        ACTION_IME           { @Override public String value() { return "**ime**";}},
        ACTION_KILL          { @Override public String value() { return "**kill**";}},
        ACTION_SILENT        { @Override public String value() { return "**ring_silent**";}},
        ACTION_VIB           { @Override public String value() { return "**ring_vib**";}},
        ACTION_SILENT_VIB    { @Override public String value() { return "**ring_vib_silent**";}},
        ACTION_EVENT         { @Override public String value() { return "**event**";}},
        ACTION_TODAY         { @Override public String value() { return "**today**";}},
        ACTION_ALARM         { @Override public String value() { return "**alarm**";}},
        ACTION_NULL          { @Override public String value() { return "**null**";}},
        ACTION_APP           { @Override public String value() { return "**app**";}};
        public String value() { return this.value(); }
    }

    public static ActionConstant fromString(String string) {
        ActionConstant[] allTargs = ActionConstant.values();
        for (int i=0; i < allTargs.length; i++) {
            if (string.equals(allTargs[i].value())) {
                return allTargs[i];
            }
        }
        // not in ENUM must be custom
        return ActionConstant.ACTION_APP;
    }

    public static String[] actionActions() {
        return fromActionArray(ActionConstant.values());
    }

    public static String[] fromActionArray(ActionConstant[] allTargs) {
        int actions = allTargs.length;
        String[] values = new String [actions];
        for (int i = 0; i < actions; i++) {
            values [i] = allTargs[i].value();
        }
        return values;
    }

    public static Drawable getSystemUIDrawable(Context context, String DrawableID) {
        Resources res = context.getResources();
        PackageManager pm = context.getPackageManager();
        int resId = 0;
        Drawable d = res.getDrawable(com.android.internal.R.drawable.ic_action_empty);
        if (pm != null) {
            Resources mSystemUiResources = null;
            try {
                mSystemUiResources = pm.getResourcesForApplication("com.android.systemui");
            } catch (Exception e) {
            }

            if (mSystemUiResources != null && DrawableID != null) {
                resId = mSystemUiResources.getIdentifier(DrawableID, null, null);
            }
            if (resId > 0) {
                try {
                    d = mSystemUiResources.getDrawable(resId);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return d;
    }

    public static String getProperName(Context context, String actionstring) {
        // Will return a string for the associated action, but will need the caller's context to get resources.
        Resources res = context.getResources();
        String value = "";
        ActionConstant action = fromString(actionstring);
        switch (action) {
            case ACTION_HOME :
                value = res.getString(com.android.internal.R.string.action_home);
                break;
            case ACTION_BACK:
                value = res.getString(com.android.internal.R.string.action_back);
                break;
            case ACTION_RECENTS:
                value = res.getString(com.android.internal.R.string.action_recents);
                break;
            case ACTION_RECENTS_GB:
                value = res.getString(com.android.internal.R.string.action_recents_gb);
                break;
            case ACTION_SEARCH:
                value = res.getString(com.android.internal.R.string.action_search);
                break;
            /*case ACTION_SCREENSHOT:
                value = res.getString(com.android.internal.R.string.action_screenshot);
                break;*/
            case ACTION_MENU:
                value = res.getString(com.android.internal.R.string.action_menu);
                break;
            case ACTION_IME:
                value = res.getString(com.android.internal.R.string.action_ime);
                break;
            case ACTION_KILL:
                value = res.getString(com.android.internal.R.string.action_kill);
                break;
            case ACTION_LAST_APP:
                value = res.getString(com.android.internal.R.string.action_lastapp);
                break;
            case ACTION_POWER:
                value = res.getString(com.android.internal.R.string.action_power);
                break;
            case ACTION_NOTIFICATIONS:
                value = res.getString(com.android.internal.R.string.action_notifications);
                break;
            case ACTION_ASSIST:
                value = res.getString(com.android.internal.R.string.action_assist);
                break;
            case ACTION_CLOCKOPTIONS:
                value = res.getString(com.android.internal.R.string.action_clockoptions);
                break;
            case ACTION_VOICEASSIST:
                value = res.getString(com.android.internal.R.string.action_voiceassist);
                break;
            case ACTION_TORCH:
                value = res.getString(com.android.internal.R.string.action_torch);
                break;
            case ACTION_SILENT:
                value = res.getString(com.android.internal.R.string.action_silent);
                break;
            case ACTION_VIB:
                value = res.getString(com.android.internal.R.string.action_vib);
                break;
            case ACTION_SILENT_VIB:
                value = res.getString(com.android.internal.R.string.action_silent_vib);
                break;
            case ACTION_EVENT:
                value = res.getString(com.android.internal.R.string.action_event);
                break;
            case ACTION_TODAY:
                value = res.getString(com.android.internal.R.string.action_today);
                break;
            case ACTION_ALARM:
                value = res.getString(com.android.internal.R.string.action_alarm);
                break;
            case ACTION_APP:
                value = res.getString(com.android.internal.R.string.action_app);
                break;
            case ACTION_NULL:
            default:
                value = res.getString(com.android.internal.R.string.action_null);
                break;

        }
        return value;
    }

    public static Drawable getActionIcon(Context context,String actionstring) {
        // Will return a Drawable for the associated action,
        // but will need the caller's context to get resources.
        Resources res = context.getResources();
        int iconStyle = Settings.System.getInt(context.getContentResolver(),
                Settings.System.NAVIGATION_BAR_ICON_STYLE, 0);
        Drawable value = null;
        ActionConstant action = fromString(actionstring);
        switch (action) {
            case ACTION_HOME :
                if (iconStyle == 0) {
                    value = getSystemUIDrawable(context,
                            "com.android.systemui:drawable/ic_sysbar_home");
                } else {
                    value = getSystemUIDrawable(context,
                            "com.android.systemui:drawable/ic_stock_sysbar_home");
                }
                break;
            case ACTION_BACK:
                if (iconStyle == 0) {
                    value = getSystemUIDrawable(context,
                            "com.android.systemui:drawable/ic_sysbar_back");
                } else {
                    value = getSystemUIDrawable(context,
                            "com.android.systemui:drawable/ic_stock_sysbar_back");
                }
                break;
            case ACTION_RECENTS:
                if (iconStyle == 0) {
                    value = getSystemUIDrawable(context,
                            "com.android.systemui:drawable/ic_sysbar_recent");
                } else {
                    value = getSystemUIDrawable(context,
                            "com.android.systemui:drawable/ic_stock_sysbar_recent");
                }
                break;
            case ACTION_RECENTS_GB:
                if (iconStyle == 0) {
                    value = getSystemUIDrawable(context,
                            "com.android.systemui:drawable/ic_sysbar_recent_gb");
                } else {
                    value = getSystemUIDrawable(context,
                            "com.android.systemui:drawable/ic_stock_sysbar_recent_gb");
                }
                break;
            case ACTION_SEARCH:
                if (iconStyle == 0) {
                    value = getSystemUIDrawable(context,
                            "com.android.systemui:drawable/ic_sysbar_search");
                } else {
                    value = getSystemUIDrawable(context,
                            "com.android.systemui:drawable/ic_stock_sysbar_search");
                }
                break;
            /*case ACTION_SCREENSHOT:
                value = getSystemUIDrawable(context, "com.android.systemui:drawable/ic_sysbar_screenshot");
                break;*/
            case ACTION_MENU:
                if (iconStyle == 0) {
                    value = getSystemUIDrawable(context,
                            "com.android.systemui:drawable/ic_sysbar_menu_big");
                } else {
                    value = getSystemUIDrawable(context,
                            "com.android.systemui:drawable/ic_stock_sysbar_menu_big");
                }
                break;
            case ACTION_IME:
                if (iconStyle == 0) {
                    value = getSystemUIDrawable(context,
                            "com.android.systemui:drawable/ic_sysbar_ime_switcher");
                } else {
                    value = getSystemUIDrawable(context,
                            "com.android.systemui:drawable/ic_stock_sysbar_ime_switcher");
                }
                break;
            case ACTION_KILL:
                if (iconStyle == 0) {
                    value = getSystemUIDrawable(context,
                            "com.android.systemui:drawable/ic_sysbar_killtask");
                } else {
                    value = getSystemUIDrawable(context,
                            "com.android.systemui:drawable/ic_stock_sysbar_killtask");
                }
                break;
            case ACTION_LAST_APP:
                if (iconStyle == 0) {
                    value = getSystemUIDrawable(context,
                            "com.android.systemui:drawable/ic_sysbar_lastapp");
                } else {
                    value = getSystemUIDrawable(context,
                            "com.android.systemui:drawable/ic_stock_sysbar_lastapp");
                }
                break;
            case ACTION_POWER:
                if (iconStyle == 0) {
                    value = getSystemUIDrawable(context,
                            "com.android.systemui:drawable/ic_sysbar_power");
                } else {
                    value = getSystemUIDrawable(context,
                            "com.android.systemui:drawable/ic_stock_sysbar_power");
                }
                break;
            case ACTION_NOTIFICATIONS:
                if (iconStyle == 0) {
                    value = getSystemUIDrawable(context,
                            "com.android.systemui:drawable/ic_sysbar_notifications");
                } else {
                    value = getSystemUIDrawable(context,
                            "com.android.systemui:drawable/ic_stock_sysbar_notifications");
                }
                break;
            case ACTION_ASSIST:
                if (iconStyle == 0) {
                    value = getSystemUIDrawable(context,
                                "com.android.systemui:drawable/ic_sysbar_assist");
                } else {
                    value = getSystemUIDrawable(context,
                                "com.android.systemui:drawable/ic_stock_sysbar_assist");
                }
                break;
            case ACTION_CLOCKOPTIONS:
                if (iconStyle == 0) {
                    value = getSystemUIDrawable(context,
                            "com.android.systemui:drawable/ic_sysbar_clockoptions");
                } else {
                    value = getSystemUIDrawable(context,
                                "com.android.systemui:drawable/ic_stock_sysbar_clockoptions");
                }
                break;
            case ACTION_VOICEASSIST:
                if (iconStyle == 0) {
                    value = getSystemUIDrawable(context,
                            "com.android.systemui:drawable/ic_sysbar_voiceassist");
                } else {
                    value = getSystemUIDrawable(context,
                                "com.android.systemui:drawable/ic_stock_sysbar_voiceassist");
                }
                break;
            case ACTION_TORCH:
                if (iconStyle == 0) {
                    value = getSystemUIDrawable(context,
                            "com.android.systemui:drawable/ic_sysbar_torch");
                } else {
                    value = getSystemUIDrawable(context,
                                "com.android.systemui:drawable/ic_stock_sysbar_torch");
                }
                break;
            case ACTION_SILENT:
                if (iconStyle == 0) {
                    value = getSystemUIDrawable(context,
                            "com.android.systemui:drawable/ic_sysbar_silent");
                } else {
                    value = getSystemUIDrawable(context,
                                "com.android.systemui:drawable/ic_stock_sysbar_silent");
                }
                break;
            case ACTION_VIB:
                if (iconStyle == 0) {
                    value = getSystemUIDrawable(context,
                            "com.android.systemui:drawable/ic_sysbar_vib");
                } else {
                    value = getSystemUIDrawable(context,
                                "com.android.systemui:drawable/ic_stock_sysbar_vib");
                }
                break;
            case ACTION_SILENT_VIB:
                if (iconStyle == 0) {
                    value = getSystemUIDrawable(context,
                            "com.android.systemui:drawable/ic_sysbar_silent_vib");
                } else {
                    value = getSystemUIDrawable(context,
                                "com.android.systemui:drawable/ic_stock_sysbar_silent_vib");
                }
                break;
            case ACTION_EVENT:
                if (iconStyle == 0) {
                    value = getSystemUIDrawable(context,
                            "com.android.systemui:drawable/ic_sysbar_event");
                } else {
                    value = getSystemUIDrawable(context,
                                "com.android.systemui:drawable/ic_stock_sysbar_event");
                }
                break;
            case ACTION_TODAY:
                if (iconStyle == 0) {
                    value = getSystemUIDrawable(context,
                            "com.android.systemui:drawable/ic_sysbar_today");
                } else {
                    value = getSystemUIDrawable(context,
                                "com.android.systemui:drawable/ic_stock_sysbar_today");
                }
                break;
            case ACTION_ALARM:
                if (iconStyle == 0) {
                    value = getSystemUIDrawable(context,
                            "com.android.systemui:drawable/ic_sysbar_alarm");
                } else {
                    value = getSystemUIDrawable(context,
                                "com.android.systemui:drawable/ic_stock_sysbar_alarm");
                }
                break;
            case ACTION_APP: // APP doesn't really have an icon - it should look up
                        //the package icon - we'll return the 'null' on just in case
            case ACTION_NULL:
            default:
                if (iconStyle == 0) {
                    value = getSystemUIDrawable(context,
                            "com.android.systemui:drawable/ic_sysbar_null");
                } else {
                    value = getSystemUIDrawable(context,
                                "com.android.systemui:drawable/ic_stock_sysbar_null");
                }
                break;

        }
        return value;
    }
}
