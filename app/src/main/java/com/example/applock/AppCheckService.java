package com.example.applock;

import static com.example.applock.MyApplication.CHAINNEL_ID;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.room.Room;

import com.andrognito.patternlockview.PatternLockView;
import com.andrognito.patternlockview.listener.PatternLockViewListener;
import com.andrognito.patternlockview.utils.PatternLockUtils;
import com.example.applock.dao.DataUpdateListener;
import com.example.applock.db.Lock;
import com.example.applock.db.LockDatabase;
import com.example.applock.fragment.HomeFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public class AppCheckService extends Service {

    private static final String TAG = "AppCheckService";
    private static final int CHANNEL_DEFAULT_IMPORTANCE_SERVICE = 1;

    public static Notification notification;

    ArrayList<Lock> locks = new ArrayList<>();

    // list app lock
    ArrayList<String> names = new ArrayList<>();

    String currentAppsTemp = "";
    String recentappsApp = "";
    private Broadcast mBroadcast;
    WindowManager windowManager;
    View passwordView;
    private Handler mHandler;
    View myview;
    SharedPreferences sharedPreferences;
    String packageName = "";
    LockDatabase database;


    @Override
    public void onCreate() {

        HandlerThread handlerThread = new HandlerThread("MyHandlerThread");
        handlerThread.start();
        Looper looper = handlerThread.getLooper();
        mHandler = new Handler(looper);

        database = Room.databaseBuilder(getApplicationContext(), LockDatabase.class, "locks_database")
                .allowMainThreadQueries()
                .build();

        locks.addAll(database.lockDAO().getListApps());

        for (Lock lock : locks) {
            names.add(lock.getName());
        }

        super.onCreate();
    }

    @SuppressLint("ForegroundServiceType")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        // receiver list data
        if (intent != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (intent.getParcelableArrayListExtra("listLock", Lock.class) != null) {

                    ArrayList<Lock> lockArrayList = new ArrayList<>();
                    locks.clear();
                    //                 locks.addAll(intent.getParcelableArrayExtra("listLock"));
                    lockArrayList = intent.getParcelableArrayListExtra("listLock");
                    locks.addAll(lockArrayList);
                    names.clear();

                    for (Lock lock : locks) {
                        names.add(lock.getName());
                    }

                }
            }
        }

        for (Lock lock : locks) {
            Log.d("fsafas", lock.getName() + " ");
        }

        Intent intent1 = new Intent(this, HomeFragment.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent1, PendingIntent.FLAG_MUTABLE);

        Notification notification1 = new NotificationCompat
                .Builder(this, CHAINNEL_ID)
                .setContentTitle("Title notification service")
                .setContentText("Applock")
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentIntent(pendingIntent).build();

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

            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            UsageStatsManager usageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
            long currentTime = System.currentTimeMillis();
            List<UsageStats> stats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, currentTime - 1000 * 10, currentTime);

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
                        packageName = applicationInfo.packageName;

                        // lay app hien tai dang mo
                        String appName = (String) packageManager.getApplicationLabel(applicationInfo);

                        if (names.contains(appName)) {
                            if (currentAppsTemp != appName) {

                                Log.d("anhlatdepjtrai", appName);

                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        showOverlayPassWord();
                                    }
                                });
                            }
                        }

                        currentAppsTemp = appName;
                        // cai nay de check khi nguoi dung bam ung dung gan day
                        recentappsApp = appName;
                        Log.d("loiroaioa", appName);

                    } catch (PackageManager.NameNotFoundException e) {
                    }
                }
            }
        }
    }

    private void showOverlayPassWord() {
        int layoutParamsType;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            layoutParamsType = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            layoutParamsType = WindowManager.LayoutParams.TYPE_PHONE;
        }

        LayoutInflater li = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );
        params.gravity = Gravity.CENTER;
        params.x = 0;
        params.y = 0;

        FrameLayout wrapper = new FrameLayout(this) {
            @Override
            public boolean dispatchKeyEvent(KeyEvent event) {
                if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {

                    hideOverlayPassword(myview);

                    return true;
                }
                return super.dispatchKeyEvent(event);
            }

            public void onCloseSystemDialogs(String reason) {

                if (reason.equals("homekey")) {

                    Intent homeIntent = new Intent(Intent.ACTION_MAIN);
                    homeIntent.addCategory(Intent.CATEGORY_HOME);
                    homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(homeIntent);

                    hideOverlayPassword(myview);


                } else if (reason.equals("recentapps")) {

                    hideOverlayPassword(myview);

                }
            }
        };


        myview = li.inflate(R.layout.password_layout, wrapper);
        sharedPreferences = getSharedPreferences("PREFS", Context.MODE_PRIVATE);
        String password = sharedPreferences.getString("password", "null");
        LayoutInflater inflater = LayoutInflater.from(this);
        ImageView imgIcon = myview.findViewById(R.id.img_iconApp);
        TextView tvName = myview.findViewById(R.id.tvName);
        PatternLockView lockView = myview.findViewById(R.id.pattern_lock_view);
        windowManager.addView(myview, params);

        tvName.setOnClickListener(v -> Log.d("2121212", "fsadfafafsa"));

        myview.setFocusable(true);
        myview.setFocusableInTouchMode(true);
        myview.requestFocus();
        lockView.addPatternLockListener(new PatternLockViewListener() {
            @Override
            public void onStarted() {
            }

            @Override
            public void onProgress(List<PatternLockView.Dot> progressPattern) {
            }

            @Override
            public void onComplete(List<PatternLockView.Dot> pattern) {

                if (password.equals(PatternLockUtils.patternToString(lockView, pattern))) {

                    hideOverlayPassword(myview);

                } else {

                }

            }

            @Override
            public void onCleared() {

            }
        });


    }

    private void hideOverlayPassword(View view) {
        windowManager.removeView(view);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void LogFunc() {
        Log.d("34314", "234");
    }

}
