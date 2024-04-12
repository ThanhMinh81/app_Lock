package com.example.applock;


import static com.example.applock.MyApplication.CHAINNEL_ID;

import android.accessibilityservice.AccessibilityService;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.andrognito.patternlockview.PatternLockView;
import com.andrognito.patternlockview.listener.PatternLockViewListener;
import com.andrognito.patternlockview.utils.PatternLockUtils;
import com.example.applock.db.Lock;
import com.example.applock.fragment.HomeFragment;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;

public class AppCheckService extends AccessibilityService implements   View.OnTouchListener  {

    private static final String TAG = "AppCheckService";
    private static final int CHANNEL_DEFAULT_IMPORTANCE_SERVICE = 1;

    public static Notification notification;

    ArrayList<Lock> locks;

    String currentAppsTempt = "";

    private Broadcast mBroadcast;

    WindowManager windowManager;

    View passwordView;

    private Handler mHandler;


    SharedPreferences sharedPreferences;

    @SuppressLint("ForegroundServiceType")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Service started");

        locks = new ArrayList<>();
        mHandler = new Handler(getMainLooper());

        locks = intent.getParcelableArrayListExtra("listData");

        for (Lock lock : locks) {
            Log.d("fsafas", lock.getName() + " ");

        }


        mBroadcast = new Broadcast();
        IntentFilter filter = new IntentFilter("test.Broadcast");

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                registerReceiver(mBroadcast, filter, Context.RECEIVER_NOT_EXPORTED);
            }
        } catch (Exception e) {
            Log.d("309257320947", e.toString());
        }

        Intent intent1 = new Intent(this, HomeFragment.class);


        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent1, PendingIntent.FLAG_MUTABLE);

        Notification notification1 = new NotificationCompat.Builder(this, CHAINNEL_ID).setContentTitle("Title notification service").setContentText("ok").setSmallIcon(R.drawable.ic_launcher_background).setContentIntent(pendingIntent).build();

        startForeground(1, notification1);


        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                checkRunningApps();
            }
        });
        thread.start();


        return START_STICKY;
    }


    // get current apps is openning
    private void checkRunningApps() {
        while (true) {

//            Log.d("fdsaf35938498347918","32847294791239497241");

            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            UsageStatsManager usageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
            long currentTime = System.currentTimeMillis();
            List<UsageStats> stats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, currentTime - 1000 * 10, currentTime);
            boolean tempt = false;

            if (stats != null) {
                SortedMap<Long, UsageStats> runningTasks = new TreeMap<>();
                for (UsageStats usageStats : stats) {
                    runningTasks.put(usageStats.getLastTimeUsed(), usageStats);
                }
                if (!runningTasks.isEmpty()) {

                    String packageName = runningTasks.get(runningTasks.lastKey()).getPackageName();
                    PackageManager packageManager = getPackageManager();
                    try {

                        ApplicationInfo applicationInfo = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA);

                        String appName = (String) packageManager.getApplicationLabel(applicationInfo);

                        for (Lock lock : locks) {
                            tempt = true;
                            if (lock.getName().equals(appName)) {

                                if (currentAppsTempt != appName) {

                                    Log.d("FDSAOIFJOSASDFAF", lock.getName());
                                    mHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            showOverlay();
                                        }
                                    });
                                }
                                currentAppsTempt = appName;
                            }
                        }


                    } catch (PackageManager.NameNotFoundException e) {
                        Log.d("loiroaioa", e.toString());
                    }
                }
            }

        }
    }

    private void showOverlay() {


        int layoutParamsType;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            layoutParamsType = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        }
        else {
            layoutParamsType = WindowManager.LayoutParams.TYPE_PHONE;
        }

        sharedPreferences = getSharedPreferences("PREFS", Context.MODE_PRIVATE);

        String password = sharedPreferences.getString("password", "null");

        WindowManager windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                layoutParamsType,
                0,
                PixelFormat.TRANSLUCENT
        );
        params.gravity = Gravity.CENTER | Gravity.START;
        params.x = 0;
        params.y = 0;



//        LayoutInflater inflater = LayoutInflater.from(this);
//        View overlayView = inflater.inflate(R.layout.password_layout, null);
//        ImageView imgIcon = overlayView.findViewById(R.id.img_iconApp);
//        TextView tvName = overlayView.findViewById(R.id.tvName);
//        PatternLockView lockView = overlayView.findViewById(R.id.pattern_lock_view);
//        lockView.addPatternLockListener(new PatternLockViewListener() {
//            @Override
//            public void onStarted() {
//            }
//
//            @Override
//            public void onProgress(List<PatternLockView.Dot> progressPattern) {
//            }
//
//            @Override
//            public void onComplete(List<PatternLockView.Dot> pattern) {
//                // if drawn pattern is equal to created pattern you will navigate to home screen
//                if (password.equals(PatternLockUtils.patternToString(lockView, pattern))) {
//                    windowManager.removeView(overlayView);
//
////                    Intent intent = new Intent(getApplicationContext(), ProgramActivity.class);
////                    startActivity(intent);
////                    finish();
//
//
//                } else {
//                    lockView.clearPattern();
//                    Log.d("90r97e0afsdf", ";lasdfa");
//
//                }
//            }
//
//            @Override
//            public void onCleared() {
//
//            }
//        });
//
//
//        overlayView.setFocusable(true);
//        overlayView.setFocusableInTouchMode(true);
//        overlayView.requestFocus();
//        overlayView.setOnKeyListener(new View.OnKeyListener() {
//            @Override
//            public boolean onKey(View v, int keyCode, KeyEvent event) {
//                Log.d("009212", keyCode + " ");
//                int temp = keyCode;
//                if (keyCode == KeyEvent.KEYCODE_BACK) {
//                    Log.d("3244fs", "Back ");
//
//                } else if (keyCode == KeyEvent.KEYCODE_HOME) {
//                    Log.d("3244fs", "HOME ");
//
//                } else {
//                    Log.d("3244fs", "SWith ");
//
//                }
//
//                return false;
//
//            }
//        });

        FrameLayout overlayView = new FrameLayout(this){

            @Override
            public boolean dispatchKeyEvent(KeyEvent event) {

                Log.d("|504325320","3454235");

                // Only fire on the ACTION_DOWN event, or you'll get two events (one for _DOWN, one for _UP)
                if (event.getAction() == KeyEvent.ACTION_DOWN) {

                    // Check if the HOME button is pressed
                    if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {

                        Log.v(TAG, "BACK Button Pressed");

                        // As we've taken action, we'll return true to prevent other apps from consuming the event as well
                        return true;
                    }
                }

                // Otherwise don't intercept the event
                return super.dispatchKeyEvent(event);
            }
        };


        LayoutInflater inflater = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE));

        if (inflater != null) {
            View   floatyView = inflater.inflate(R.layout.password_layout, overlayView);
            floatyView.setOnTouchListener(this);
            windowManager.addView(floatyView, params);
        }
        else {
            Log.e("SAW-example", "Layout Inflater Service is null; can't inflate and display R.layout.floating_view");
        }

//        windowManager.addView(overlayView, params);

    }


    private void showPasswordScreen() {

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT);

        LayoutInflater inflater = LayoutInflater.from(this);
        passwordView = inflater.inflate(R.layout.password_layout, null);

//        EditText passwordEditText = passwordView.findViewById(R.id.passwordEditText);
//        Button submitButton = passwordView.findViewById(R.id.submitButton);
//        submitButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                String password = passwordEditText.getText().toString();
//
//                if (password.equals("your_password")) {
//
//                    windowManager.removeView(passwordView);
//                } else {
//
//                }
//            }
//        });


        windowManager.addView(passwordView, params);

    }


    private void hidePasswordScreen() {
        if (windowManager != null && passwordView != null) {
            try {
                windowManager.removeView(passwordView);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        unregisterReceiver(mBroadcast);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

    }

    @Override
    public void onInterrupt() {

    }

    @Override
    public boolean onKeyEvent(KeyEvent event) {
        Log.d("Fdsfasfa23","43434");
        int keyCode = event.getKeyCode();
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                // Xử lý khi nút back được bấm
                Log.d("AccessibilityService", "Back button pressed");
                return true;
            case KeyEvent.KEYCODE_HOME:
                // Xử lý khi nút home được bấm
                Log.d("AccessibilityService", "Home button pressed");
                return true;
            case KeyEvent.KEYCODE_APP_SWITCH:
                // Xử lý khi nút recent apps được bấm
                Log.d("AccessibilityService", "Recent apps button pressed");
                return true;
            default:
                // Xử lý các trường hợp khác
                return super.onKeyEvent(event);
        }
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {

        Log.d("2321313","Fsfasfafht");

        return false;
    }
}
