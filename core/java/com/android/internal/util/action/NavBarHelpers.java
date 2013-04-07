
package com.android.internal.util.action;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;

import static com.android.internal.util.action.ActionConstants.*;
import java.net.URISyntaxException;

public class NavBarHelpers {

    // These items will be subtracted from NavBar Actions when RC requests list of
    // Available Actions
    private final ActionConstant[] EXCLUDED_FROM_NAVBAR = {
            ActionConstant.ACTION_CLOCKOPTIONS,
            ActionConstant.ACTION_SILENT,
            ActionConstant.ACTION_VIB,
            ActionConstant.ACTION_SILENT_VIB,
            ActionConstant.ACTION_EVENT,
            ActionConstant.ACTION_TODAY,
            ActionConstant.ACTION_ALARM
    };

    private NavBarHelpers() {
    }

    public static Drawable getIconImage(Context mContext, String uri) {
        Drawable actionIcon;
        if (TextUtils.isEmpty(uri)) {
            uri = ActionConstants.ActionConstant.ACTION_NULL.value();
        }
        if (uri.startsWith("**")) {
            return ActionConstants.getActionIcon(mContext, uri);
        } else {  // This must be an app
            try {
                actionIcon = mContext.getPackageManager().getActivityIcon(Intent.parseUri(uri, 0));
            } catch (NameNotFoundException e) {
                e.printStackTrace();
                actionIcon = ActionConstants.getActionIcon(mContext,
                       ActionConstants.ActionConstant.ACTION_NULL.value());
            } catch (URISyntaxException e) {
                e.printStackTrace();
                actionIcon = ActionConstants.getActionIcon(mContext,
                        ActionConstants.ActionConstant.ACTION_NULL.value());
            }
        }
        return actionIcon;
    }

    public static String[] getNavBarActions() {
        // I need to find a good way to subtract the Excluded array from All actions.
        // for now, just return all Actions.
        return ActionConstants.actionActions();
    }

    public static String getProperSummary(Context mContext, String uri) {
        if (TextUtils.isEmpty(uri)) {
            uri = ActionConstants.ActionConstant.ACTION_NULL.value();
        }
        if (uri.startsWith("**")) {
            return ActionConstants.getProperName(mContext, uri);
        } else {  // This must be an app
            try {
                Intent intent = Intent.parseUri(uri, 0);
                if (Intent.ACTION_MAIN.equals(intent.getAction())) {
                    return getFriendlyActivityName(mContext, intent);
                }
                return getFriendlyShortcutName(mContext, intent);
            } catch (URISyntaxException e) {
                return ActionConstants.getProperName(mContext, ActionConstants.ActionConstant.ACTION_NULL.value());
            }
        }
    }

    private static String getFriendlyActivityName(Context mContext, Intent intent) {
        PackageManager pm = mContext.getPackageManager();
        ActivityInfo ai = intent.resolveActivityInfo(pm, PackageManager.GET_ACTIVITIES);
        String friendlyName = null;

        if (ai != null) {
            friendlyName = ai.loadLabel(pm).toString();
            if (friendlyName == null) {
                friendlyName = ai.name;
            }
        }

        return (friendlyName != null) ? friendlyName : intent.toUri(0);
    }

    private static String getFriendlyShortcutName(Context mContext, Intent intent) {
        String activityName = getFriendlyActivityName(mContext, intent);
        String name = intent.getStringExtra(Intent.EXTRA_SHORTCUT_NAME);

        if (activityName != null && name != null) {
            return activityName + ": " + name;
        }
        return name != null ? name : intent.toUri(0);
    }
}
