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

package com.android.systemui.navbar;

import com.android.systemui.statusbar.WidgetView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;

/*
 * Toggle the Torch/Flashlight
 */

public class TorchToggle extends Activity  {
    public TorchToggle() {
        super();
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        boolean bright = Settings.System.getInt(this.getContentResolver(),
                Settings.System.EXPANDED_FLASH_MODE, 0) == 1;
        Intent toggleTorch = new Intent("net.cactii.flash2.TOGGLE_FLASHLIGHT");
        toggleTorch.putExtra("bright", bright);
        sendBroadcast(toggleTorch);
        this.finish();
    }
}
