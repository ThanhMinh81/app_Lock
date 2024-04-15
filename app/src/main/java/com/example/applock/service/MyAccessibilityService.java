package com.example.applock.service;

import android.accessibilityservice.AccessibilityService;
import android.util.Log;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

public class MyAccessibilityService extends AccessibilityService {


    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        int eventType = event.getEventType();
        if (eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {

            if (event.getPackageName() != null && event.getClassName() != null &&  event.getPackageName().equals("com.android.launcher") && event.getClassName().equals("com.android.launcher.Launcher")) {
                    Log.d("fsaofsf","fsafdsa");

            }
        }
    }

    @Override
    public void onInterrupt() {
        // Do nothing
    }


}
