
package com.android.systemui.statusbar.policy;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;

import com.android.systemui.R;
import com.android.systemui.navbar.NavbarAction;
import com.android.systemui.statusbar.policy.KeyButtonView;

public class ExtensibleKeyButtonView extends KeyButtonView {

    public String mClickAction, mLongpress;

    public ExtensibleKeyButtonView(Context context, AttributeSet attrs, String clickAction,
            String longPress) {
        super(context, attrs);
        mClickAction = clickAction;
        mLongpress = longPress;
        setActions(clickAction, longPress);
        setLongPress();
    }

    public void setActions(String clickAction, String longPress) {
        if (clickAction != null) {
            if (clickAction.equals(NavbarAction.ACTION_HOME)) {
                setCode(KeyEvent.KEYCODE_HOME);
                setId(R.id.home);
            } else if (clickAction.equals(NavbarAction.ACTION_BACK)) {
                setCode(KeyEvent.KEYCODE_BACK);
                setId(R.id.back);
            } else if (clickAction.equals(NavbarAction.ACTION_MENU)) {
                setCode(KeyEvent.KEYCODE_MENU);
                setId(R.id.menu);
            } else if (clickAction.equals(NavbarAction.ACTION_POWER)) {
                setCode(KeyEvent.KEYCODE_POWER);
            } else if (clickAction.equals(NavbarAction.ACTION_SEARCH)) {
                setCode(KeyEvent.KEYCODE_SEARCH);
            } else {
                setOnClickListener(mClickListener);
            }
        }
    }

    protected void setLongPress() {
        setSupportsLongPress(false);
        if (mLongpress != null) {
            if ((!mLongpress.equals(NavbarAction.ACTION_NULL)) || (getCode() != 0)) {
                // I want to allow long presses for defined actions, or if
                // primary action is a 'key' and long press isn't defined
                // otherwise
                setSupportsLongPress(true);
                setOnLongClickListener(mLongPressListener);
            }
        }
    }

    protected OnClickListener mClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            NavbarAction.getInstance(mContext).launchAction(mClickAction);
        }
    };

    protected OnLongClickListener mLongPressListener = new OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            return NavbarAction.getInstance(mContext).launchAction(mLongpress);
        }
    };
}
