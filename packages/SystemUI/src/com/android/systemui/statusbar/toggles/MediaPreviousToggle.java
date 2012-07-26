
package com.android.systemui.statusbar.toggles;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;

import com.android.systemui.R;

public class MediaPreviousToggle extends Toggle {

    public MediaPreviousToggle(Context context) {
        super(context);
        setLabel(R.string.toggle_media_previous);
        updateState();
    }

    protected void sendMediaKeyEvent(int code) {
        Context context = mView.getContext();
        long eventtime = SystemClock.uptimeMillis();

        Intent downIntent = new Intent(Intent.ACTION_MEDIA_BUTTON, null);
        KeyEvent downEvent = new KeyEvent(eventtime, eventtime, KeyEvent.ACTION_DOWN, code, 0);
        downIntent.putExtra(Intent.EXTRA_KEY_EVENT, downEvent);
        context.sendOrderedBroadcast(downIntent, null);

        Intent upIntent = new Intent(Intent.ACTION_MEDIA_BUTTON, null);
        KeyEvent upEvent = new KeyEvent(eventtime, eventtime, KeyEvent.ACTION_UP, code, 0);
        upIntent.putExtra(Intent.EXTRA_KEY_EVENT, upEvent);
        context.sendOrderedBroadcast(upIntent, null);
    }

    @Override
    protected boolean updateInternalToggleState() {
        mToggle.setChecked(true);
        mToggle.setEnabled(true);
        setIcon(R.drawable.toggle_media_previous);
        return true;
    }

    @Override
    protected void onCheckChanged(boolean isChecked) {
        sendMediaKeyEvent(KeyEvent.KEYCODE_MEDIA_PREVIOUS);
        updateState();
    }

    @Override
    protected boolean onLongPress() {
        return false;
    }
}
