package com.example.applock.service;

import static com.example.applock.MyApplication.CHAINNEL_ID;

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
import android.graphics.drawable.AdaptiveIconDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.VectorDrawable;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.room.Room;

import com.example.applock.OverlayActivity;
import com.example.applock.R;
import com.example.applock.ScreenReceiver;
import com.example.applock.db.LockDatabase;
import com.example.applock.fragment.HomeFragment;
import com.example.applock.model.Lock;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Objects;

public class LockService extends Service {

    ArrayList<Lock> locks = new ArrayList<>();

    private BroadcastReceiver screenReceiver = new ScreenReceiver();

    private Handler mHandler;
    LockDatabase database;

    private boolean isServiceRunning = false;

    public BroadcastReceiver receiver;
    private String currentLock = "";

    private String packageTemp = "null";

    @Override
    public void onCreate() {

        // Tạo BroadcastReceiver
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String message = intent.getStringExtra("message");
                currentLock = message;
            }
        };

        registerReceiver(screenReceiver, new IntentFilter(Intent.ACTION_SCREEN_ON));
        registerReceiver(screenReceiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));

        // Đăng ký BroadcastReceiver với IntentFilter để lắng nghe broadcast có action "ACTION_SEND_MESSAGE"
        IntentFilter filter = new IntentFilter("ACTION_LOCK_APP");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            registerReceiver(receiver, filter, RECEIVER_EXPORTED);
        }

        HandlerThread handlerThread = new HandlerThread("MyHandlerThread");
        handlerThread.start();
        Looper looper = handlerThread.getLooper();
        mHandler = new Handler(looper);

        database = Room.databaseBuilder(getApplicationContext(), LockDatabase.class, "locks_database")
                .allowMainThreadQueries()
                .build();

        locks.addAll(database.lockDAO().getListApps());

        super.onCreate();

    }

    @SuppressLint("ForegroundServiceType")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        if (!isServiceRunning) {

            Intent intent1 = new Intent(this, HomeFragment.class);

            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent1, PendingIntent.FLAG_MUTABLE);


            Notification notification1 = new NotificationCompat
                    .Builder(this, CHAINNEL_ID)
                    .setContentTitle("AppLock")
                    .setContentText("Protecting your apps")
                    .setSmallIcon(R.drawable.ic_launcher_background)
                    .setContentIntent(pendingIntent).build();

            startForeground(1, notification1);
        }
        isServiceRunning = true;


        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    overlayPassWord();
                } catch (PackageManager.NameNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        thread.start();


        return START_STICKY;

    }


    private void overlayPassWord() throws PackageManager.NameNotFoundException {
        while (true) {

//            try {
//                Thread.sleep(300);
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }

            long endTime = System.currentTimeMillis();
            long beginTime = endTime - 10000;
            String result = "null";
            boolean isUnlocked = false;


            UsageEvents.Event event = new UsageEvents.Event();
            UsageStatsManager sUsageStatsManager = (UsageStatsManager) this.getSystemService(Context.USAGE_STATS_SERVICE);
            UsageEvents usageEvents = sUsageStatsManager.queryEvents(beginTime, endTime);

            while (usageEvents.hasNextEvent()) {
                usageEvents.getNextEvent(event);
                if (event.getEventType() == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                    result = event.getPackageName();
                }
            }


            if (database.lockDAO().isPackageLocked(result) != 0) {

                PackageManager packageManager = getPackageManager();

                ApplicationInfo applicationInfo = packageManager.getApplicationInfo(result, PackageManager.GET_META_DATA);

                Drawable iconDrawable = applicationInfo.loadIcon(packageManager);



                Bitmap bitmap = null;

                if (iconDrawable instanceof BitmapDrawable) {
                    bitmap = ((BitmapDrawable) iconDrawable).getBitmap();
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    if (iconDrawable instanceof AdaptiveIconDrawable) {
                        int width = iconDrawable.getIntrinsicWidth();
                        int height = iconDrawable.getIntrinsicHeight();
                        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                        Canvas canvas = new Canvas(bitmap);
                        iconDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                        iconDrawable.draw(canvas);
                    } else {
                        // Xử lý trường hợp khác nếu cần
                        bitmap = null;
                    }
                }

                if(bitmap != null)
                {

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                    byte[] b = baos.toByteArray();

                    Intent intent = new Intent(this, OverlayActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                    intent.putExtra("package", result);
                    intent.putExtra("picture", b);
                    startActivity(intent);

                }



            }



            if (currentLock != null && currentLock.length() > 0) {
                if (!result.equals(packageTemp)
                        && !result.equals("null")
                        && !packageTemp.equals("null")
                        && !result.equals("com.example.applock")
                        && !packageTemp.equals("com.example.applock")) {
                    // chỉ khi nào 2 package khác nhau thi goi laij ham nay

                    Lock lock = database.lockDAO().getLockByPackageName(currentLock);
                    lock.setStateLock(true);
                    database.lockDAO().updateLock(lock);
                    // sau khi update thanh cong clear currentLock
                    currentLock = "";

                }
            }

            packageTemp = result;

        }
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private static Bitmap getBitmapFromDrawable(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (drawable instanceof VectorDrawable || drawable instanceof AdaptiveIconDrawable) {
                // Tạo một Bitmap mới và vẽ Drawable lên đó
                Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                drawable.draw(canvas);
                return bitmap;
            } else {

            }
        }
        return null;
    }


    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.d("534fsdfshh", "75756");
        Intent restartServiceIntent = new Intent(getApplicationContext(), this.getClass());
        restartServiceIntent.setPackage(getPackageName());

        PendingIntent restartServicePendingIntent = PendingIntent.getService(getApplicationContext(), 1, restartServiceIntent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE); // Thêm FLAG_IMMUTABLE vào đây

        AlarmManager alarmService = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        alarmService.set(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + 15 * 60 * 1000, // 15phut
                restartServicePendingIntent);
        super.onTaskRemoved(rootIntent);
    }


    private Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }


    @Override
    public void onDestroy() {
//        // Hủy đăng ký BroadcastReceiver khi Service bị hủy
        unregisterReceiver(receiver);
        unregisterReceiver(screenReceiver);
        super.onDestroy();
    }


    public class Broadcast extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

        }

    }





}
