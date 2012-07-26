
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

public class MediaPlayPauseToggle extends Toggle {
    private static final String TAG = "MediaPlayPauseToggle";

    private static final int MEDIA_STATE_UNKNOWN  = -1;
    private static final int MEDIA_STATE_INACTIVE =  0;
    private static final int MEDIA_STATE_ACTIVE   =  1;

    private int mCurrentState = MEDIA_STATE_UNKNOWN;

    private static AudioManager AUDIO_MANAGER = null;

    public MediaPlayPauseToggle(Context context) {
        super(context);
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

    protected static AudioManager getAudioManager(Context context) {
        if(AUDIO_MANAGER == null) {
            AUDIO_MANAGER = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        }

        return AUDIO_MANAGER;
    }

    @Override
    protected boolean updateInternalToggleState() {
        mCurrentState = (isMusicActive() ? MEDIA_STATE_INACTIVE : MEDIA_STATE_ACTIVE);
        boolean isPlaying = (mCurrentState == MEDIA_STATE_ACTIVE);
        boolean notPlaying = (mCurrentState == MEDIA_STATE_INACTIVE);
        if (isMusicActive()) {
            mToggle.setChecked(true);
            mToggle.setEnabled(true);
            setLabel(R.string.toggle_media_pause);
            setIcon(R.drawable.toggle_media_pause);
            return isPlaying;
        } else {
            mToggle.setChecked(true);
            mToggle.setEnabled(true);
            setLabel(R.string.toggle_media_play);
            setIcon(R.drawable.toggle_media_play);
            return notPlaying;
        }
    }

    @Override
    protected void onCheckChanged(boolean isChecked) {
        sendMediaKeyEvent(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE);
        updateState();
    }

    @Override
    protected boolean onLongPress() {
        return false;
    }

    public boolean isMusicActive() {
        if(mCurrentState == MEDIA_STATE_UNKNOWN) {
            mCurrentState = MEDIA_STATE_INACTIVE;
            AudioManager am = getAudioManager(mView.getContext());
            if(am != null) {
                mCurrentState = (am.isMusicActive() ? MEDIA_STATE_ACTIVE : MEDIA_STATE_INACTIVE);
            }

            return (mCurrentState == MEDIA_STATE_ACTIVE);
        } else {
            boolean active = (mCurrentState == MEDIA_STATE_ACTIVE);
            mCurrentState = MEDIA_STATE_UNKNOWN;
            return active;
        }
    }
}
