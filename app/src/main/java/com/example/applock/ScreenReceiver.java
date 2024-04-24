package com.example.applock;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ScreenReceiver extends BroadcastReceiver {
    private static final String TAG = "ScreenReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("9059035","fspw9ur9u");
        if (Intent.ACTION_SCREEN_ON.equals(intent.getAction())) {
            // Xử lý khi màn hình được bật
            Log.d(TAG, "Screen is on");
        } else if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) {
            // Xử lý khi màn hình được tắt
            Log.d(TAG, "Screen is off");
        }
    }
}
