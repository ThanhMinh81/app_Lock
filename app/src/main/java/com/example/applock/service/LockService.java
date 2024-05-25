package com.example.applock.service;

import static com.example.applock.MyApplicationNotification.CHAINNEL_ID;
import static com.example.applock.service.ShowOverlayPassword.hideOverlay;
import static com.example.applock.service.ShowOverlayPassword.showOverlayPassWord;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.AdaptiveIconDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.VectorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.room.Room;

import com.example.applock.Interface.UnlockRecentMenu;
import com.example.applock.OverlayActivity;
import com.example.applock.R;
import com.example.applock.ScreenLockRecent;
import com.example.applock.db.LockDatabase;
import com.example.applock.fragment.HomeFragment;
import com.example.applock.model.Lock;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Objects;

public class LockService extends Service {
    private boolean isRunning = true;
    BroadcastReceiver screenReceiverOn;
    Handler mHandler;
    static LockDatabase database;
    boolean isServiceRunning = false;
    public BroadcastReceiver receiverLockCurrent;
    BroadcastReceiver receiverLockRecentMenu;
    String currentPackageLock = "";
    String packageTemp = "null";
    String modeLock = "immediately";
    ScreenLockRecent screenLockRecent;
    final String SYSTEM_DIALOG_REASON_KEY = "reason";
    final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";
    String lockRecentMenu = "";
    // check xem thu man hinh overlay da hien thi hay chua
    boolean isOverlayComfirmPasswordRecentMenu = false;
    public BroadcastReceiver broadcastReceiver;
    UnlockRecentMenu unlockRecentMenu;
    boolean isCheckShowOverlayRecent = false;
    private String result = "null";
    private ArrayList<String> isPackageUnlock;
    private Thread thread;
    private boolean checkScreenOverlayShow = false;

    private boolean showPassImplement = false;
    private SharedPreferences sharedPreferences;

    WindowManager windowManager;

    View overlayView;


    @Override
    public void onCreate() {

        unlockRecentMenu = new UnlockRecentMenu() {
            @Override
            public void unlockSs(boolean unlock) {
                isOverlayComfirmPasswordRecentMenu = unlock;
            }
        };


        sharedPreferences = getSharedPreferences("LockMode", Context.MODE_PRIVATE);


        isPackageUnlock = new ArrayList<>();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            screenLockRecent = new ScreenLockRecent(getApplicationContext(), unlockRecentMenu);
        }

        initDb();

        initBroadCast();

        super.onCreate();

    }

    private void initDb() {
        HandlerThread handlerThread = new HandlerThread("MyHandlerThread");
        handlerThread.start();
        Looper looper = handlerThread.getLooper();
        mHandler = new Handler(looper);

        database = Room.databaseBuilder(getApplicationContext(), LockDatabase.class, "locks_database").allowMainThreadQueries().build();

    }

    public void getArrayPackageLockString() {
        for (Lock item : database.lockDAO().getListLockApps()) {
            isPackageUnlock.add(item.getPackageApp());
        }
    }


    @SuppressLint("ForegroundServiceType")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        if (!isServiceRunning) {
            Intent intentNotifi = new Intent(this, HomeFragment.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intentNotifi, PendingIntent.FLAG_MUTABLE);
            Notification notification = new NotificationCompat.Builder(this, CHAINNEL_ID).setContentTitle("AppLock").setContentText("Protecting your apps").setSmallIcon(R.drawable.ic_launcher_background).setContentIntent(pendingIntent).build();
            startForeground(1, notification);
        }

        if (intent != null && intent.getExtras() != null) {

            Bundle bundle = intent.getExtras();

            if (bundle != null) {

                ArrayList<String> dataList = bundle.getStringArrayList("listPackage");

                isPackageUnlock.clear();
                isPackageUnlock.addAll(dataList);

            }

        }

        isServiceRunning = true;

        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    runAppLockChecker();
                } catch (PackageManager.NameNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        thread.start();

        return Service.START_STICKY;

    }


    private void runAppLockChecker() throws PackageManager.NameNotFoundException {
        while (isRunning) {

            modeLock = sharedPreferences.getString("lock_mode", "immediately");

            lockRecentMenu = sharedPreferences.getString("lock_recent_menu", "no");

            long endTime = System.currentTimeMillis();
            long beginTime = endTime - 6000;
            result = "null";


            UsageEvents.Event event = new UsageEvents.Event();
            UsageStatsManager sUsageStatsManager = (UsageStatsManager) this.getSystemService(Context.USAGE_STATS_SERVICE);
            UsageEvents usageEvents = sUsageStatsManager.queryEvents(beginTime, endTime);

            while (usageEvents.hasNextEvent()) {
                usageEvents.getNextEvent(event);
                if (event.getEventType() == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                    result = event.getPackageName();
                }
            }

            if (lockRecentMenu.equals("yes")) {
                lockMenuRecent();
            }

            lockScreenApp();

            if (modeLock.equals("immediately")) {

                if (currentPackageLock != null && currentPackageLock.length() > 2) {
                    if (!result.equals(packageTemp) && !result.equals("null") && !packageTemp.equals("null") && !result.equals("com.example.applock") && !packageTemp.equals("com.example.applock")) {

                        currentPackageLock = "";
                        checkScreenOverlayShow = false;
                        showPassImplement = false;

                    }
                } else {

                    showPassImplement = false;

                }

            }

            packageTemp = result;

        }
    }

    private void lockScreenApp() throws PackageManager.NameNotFoundException {

        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Log.d("5023fasfasd", result);

        if (modeLock.equals("immediately")) {

            if (database.lockDAO().isPackageLocked(result) != 0 && !showPassImplement && !result.equals("null")) {

                showPassImplement = true;

                showOverlayPassWord(this, result, "lockScreenApp", modeLock);

            }

        } else if (modeLock.equals("screen_off") && isPackageUnlock.contains(result) && !result.equals("null")) {

            showOverlayPassWord(this, result, "lockScreenApp", modeLock);

        } else if (!modeLock.equals("immediately") && !modeLock.equals("screen_off")) {

            if (database.lockDAO().isPackageLocked(result) != 0) {

                Lock lock = database.lockDAO().getLockByPackageName(result);

                if (lock != null) {

                    if (lock.isStateLockScreenAfterMinute()) {
                        showOverlayPassWord(this, result, "lockScreenApp", modeLock);
                    } else if (!lock.isStateLockScreenAfterMinute()) {
                        Calendar currentTime = Calendar.getInstance();
                        int hour = currentTime.get(Calendar.HOUR_OF_DAY);
                        int minute = currentTime.get(Calendar.MINUTE);
                        int minuteCurrentTimeClose = hour * 60 + minute;
                        int timeLock = minuteCurrentTimeClose - Integer.parseInt(lock.getTimeOpen());
                        if (timeLock >= Integer.parseInt(modeLock)) {
                            lock.setStateLockScreenAfterMinute(true);
                            database.lockDAO().updateLock(lock);
                        }
                    }
                }
            }

        }


    }


    private void lockMenuRecent() {
        if (screenLockRecent != null && isCheckShowOverlayRecent && isOverlayComfirmPasswordRecentMenu) {
            screenLockRecent.disableOverlay();
            screenLockRecent = null;
            isCheckShowOverlayRecent = false;
            isOverlayComfirmPasswordRecentMenu = false;
        }
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    private void initBroadCast() {
        // sự kiện khi mở khóa thành công
        receiverLockCurrent = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                String message = intent.getStringExtra("message");
                currentPackageLock = message;

                checkScreenOverlayShow = false;
                showPassImplement = true;


                if (modeLock.equals("screen_off") && isPackageUnlock.contains(message)) {

                    isPackageUnlock.remove(message);

                    isRunning = false;

                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                        }
                    }, 500);

                    isRunning = true;
                    thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                runAppLockChecker();
                            } catch (PackageManager.NameNotFoundException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    });

                    thread.start();

                }

            }
        };

        IntentFilter filter = new IntentFilter("ACTION_LOCK_APP");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            registerReceiver(receiverLockCurrent, filter, Context.RECEIVER_NOT_EXPORTED);
        }

        screenReceiverOn = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (Objects.equals(intent.getAction(), Intent.ACTION_SCREEN_OFF)) {
                    // tắt đi bật lại là khóa hết tất cả
                    isPackageUnlock.clear();
                    getArrayPackageLockString();
                    Log.d("903582asf", "themmanggg");
                }
            }
        };

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            registerReceiver(screenReceiverOn, new IntentFilter(Intent.ACTION_SCREEN_OFF), Context.RECEIVER_NOT_EXPORTED);
        }

//        ============== receiver press recent =====================


        IntentFilter intentFilterACSD = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                if (intent.getAction().equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
                    String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);

                    if (reason != null) {
                        if (reason.equals(SYSTEM_DIALOG_REASON_HOME_KEY)) {

                            hideOverlay(overlayView, windowManager);

                            if (screenLockRecent != null && lockRecentMenu.equals("yes") && isCheckShowOverlayRecent) {
                                screenLockRecent.disableOverlay();
                                screenLockRecent = null;
                                isCheckShowOverlayRecent = false;
                            }

                        } else if (reason.trim().equals("recentapps")) {

                            if (screenLockRecent == null && lockRecentMenu.equals("yes")) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    screenLockRecent = new ScreenLockRecent(getApplicationContext(), unlockRecentMenu);
                                }
                                screenLockRecent.showScreenPassword();
                                isCheckShowOverlayRecent = true;
                            }

                            if (screenLockRecent != null && lockRecentMenu.equals("yes") && !isCheckShowOverlayRecent) {
                                boolean check = screenLockRecent.isOverlayVisible();

                                screenLockRecent.showScreenPassword();
                                isCheckShowOverlayRecent = true;
                            }

                        }
                    }
                }
            }
        };

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            registerReceiver(broadcastReceiver, intentFilterACSD, Context.RECEIVER_NOT_EXPORTED);
        }

    }

    @Override
    public void onDestroy() {

        unregisterReceiver(receiverLockRecentMenu);
        unregisterReceiver(receiverLockCurrent);
        unregisterReceiver(screenReceiverOn);
        super.onDestroy();
    }


}
