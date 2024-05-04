package com.example.applock.service;

import static com.example.applock.MyApplicationNotification.CHAINNEL_ID;

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
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
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
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Objects;

public class LockService extends Service {

    ArrayList<Lock> locks = new ArrayList<>();

    BroadcastReceiver screenReceiverOn;
    Handler mHandler;
    LockDatabase database;
    boolean isServiceRunning = false;
    BroadcastReceiver receiverLockCurrent;
    BroadcastReceiver receiverLockRecentMenu;
    String currentPackageLock = "";
    String packageTemp = "null";
    String modeLock = "immediately";
    boolean screenOff = false;
    ArrayList<Lock> lockAppModeOffScreenList;

    ScreenLockRecent screenLockRecent ;

    final String SYSTEM_DIALOG_REASON_KEY = "reason";
//    final String SYSTEM_DIALOG_REASON_RECENT_APPS = "recentapps";

    final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";


    // check file spf da bat lock menu
    String lockRecentMenu = "";

    // check xem thu man hinh overlay da hien thi hay chua
    boolean isOverlayMenu = false;

    boolean modeLockRecent = false;
//    private WindowManager windowManager;
//    private View floatingView;

    public BroadcastReceiver broadcastReceiver;


    UnlockRecentMenu unlockRecentMenu ;



    @Override
    public void onCreate() {

        unlockRecentMenu = new UnlockRecentMenu() {
            @Override
            public void unlockSs(boolean unlock) {
                isOverlayMenu = unlock ;
            }
        };

        lockAppModeOffScreenList = new ArrayList<>();

            screenLockRecent = new ScreenLockRecent(getApplicationContext() , unlockRecentMenu);


        HandlerThread handlerThread = new HandlerThread("MyHandlerThread");
        handlerThread.start();
        Looper looper = handlerThread.getLooper();
        mHandler = new Handler(looper);

        database = Room.databaseBuilder(getApplicationContext(), LockDatabase.class, "locks_database").allowMainThreadQueries().build();

        locks.addAll(database.lockDAO().getListApps());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            initBroadCast();
        }

        super.onCreate();

    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    private void initBroadCast() {


        receiverLockCurrent = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                String message = intent.getStringExtra("message");
                currentPackageLock = message;

            }
        };

        IntentFilter filter = new IntentFilter("ACTION_LOCK_APP");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // ANDROID 8.0 TRO LEN
            registerReceiver(receiverLockCurrent, filter, RECEIVER_EXPORTED);

        }

        //== broadcast screen ==

        // lang nghe on offf screeeen
        screenReceiverOn = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                if (Objects.equals(intent.getAction(), Intent.ACTION_SCREEN_ON)) {
                    screenOff = false;
                } else if (Objects.equals(intent.getAction(), Intent.ACTION_SCREEN_OFF)) {
                    // manf hinfh tat
                    screenOff = true;
                }
            }
        };

        registerReceiver(screenReceiverOn, new IntentFilter(Intent.ACTION_SCREEN_OFF));

//        ============== receiver press recent =====================

        IntentFilter intentFilterACSD = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                if (intent.getAction().equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
                    String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);

                    if (reason != null) {
                        if (reason.equals(SYSTEM_DIALOG_REASON_HOME_KEY)) {

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                              if(screenLockRecent != null)
                              {
                                  screenLockRecent.disableOverlay();
                                  screenLockRecent = null ;
                              }
                            }

                        } else if (reason.trim().equals("recentapps")) {

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                                  if(screenLockRecent == null)
                                  {
                                      screenLockRecent = new ScreenLockRecent(getApplicationContext() , unlockRecentMenu);
                                  }
                                  // hiển thị rồi hiển thị tiếp crash


                                    if (screenLockRecent.floatingView != null  && !isOverlayMenu)
                                    {
                                        if(!screenLockRecent.isViewAttachedToWindow(screenLockRecent.floatingView))
                                        {
                                            Log.d("40935u7235325","fsadfasdfaf");
                                            screenLockRecent.showScreenPassword();
                                        }
                                    }else {
                                         if(!isOverlayMenu){
                                             screenLockRecent.showScreenPassword();
                                         }
                                    }
                            }

                        }
                    }
                }
            }
        };

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            this.registerReceiver(broadcastReceiver, intentFilterACSD, Context.RECEIVER_NOT_EXPORTED);
        }


        receiverLockRecentMenu = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                isOverlayMenu = intent.getBooleanExtra("message", false);
                Log.d("53dsfasf",isOverlayMenu + " ");

//                isOverlayMenu = true;
            }
        };

//        IntentFilter filterRecent = new IntentFilter("ACTION_LOCK_RECENT_MENU");
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            // ANDROID 8.0 TRO LEN
//            registerReceiver(receiverLockCurrent, filterRecent, RECEIVER_EXPORTED);
//        }

    }


//    @RequiresApi(api = Build.VERSION_CODES.O)
//    private void showLockRecentMenu(Context context) {
//        Button btnClear;
//        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
//
//        LayoutInflater layoutInflater = LayoutInflater.from(context);
//
//        floatingView = layoutInflater.inflate(R.layout.password_layout_recent, null);
//
//        btnClear = floatingView.findViewById(R.id.btnClear);
//
//        WindowManager.LayoutParams params = new WindowManager.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT);
//        params.gravity = Gravity.CENTER;
//        // Thêm floatingView vào WindowManager
//        windowManager.addView(floatingView, params);
//
//        btnClear.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Log.d("935793845", "|fsopdfs");
//            }
//        });
//
//    }


    @SuppressLint("ForegroundServiceType")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        if (!isServiceRunning) {

            Intent intentNotifi = new Intent(this, HomeFragment.class);

            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intentNotifi, PendingIntent.FLAG_MUTABLE);

            Notification notification = new NotificationCompat.Builder(this, CHAINNEL_ID).setContentTitle("AppLock").setContentText("Protecting your apps").setSmallIcon(R.drawable.ic_launcher_background).setContentIntent(pendingIntent).build();

            startForeground(1, notification);

        }

        isServiceRunning = true;


        Thread thread = new Thread(new Runnable() {
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

        return START_STICKY;

    }


    private void runAppLockChecker() throws PackageManager.NameNotFoundException {
        while (true) {

            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            SharedPreferences sharedPreferences = getSharedPreferences("LockMode", Context.MODE_PRIVATE);
            modeLock = sharedPreferences.getString("lock_mode", "immediately");

            lockRecentMenu = sharedPreferences.getString("lock_recent_menu", "no");


            if (lockRecentMenu.equals("yes")) {

                // khi nao thi an

                if (isOverlayMenu) {
                    Log.d("5385423fsa", "fsafas333");
                    screenLockRecent.disableOverlay();
                    isOverlayMenu = false ;
//                    showOverlayPassWord("","lockRecentMenu");
//                    isOverlayMenu = true ;
                    // hiển thị duy nhất 1 lần
                }

            }


            long endTime = System.currentTimeMillis();
            long beginTime = endTime - 10000;
            String result = "null";

            UsageEvents.Event event = new UsageEvents.Event();
            UsageStatsManager sUsageStatsManager = (UsageStatsManager) this.getSystemService(Context.USAGE_STATS_SERVICE);
            UsageEvents usageEvents = sUsageStatsManager.queryEvents(beginTime, endTime);

            while (usageEvents.hasNextEvent()) {
                usageEvents.getNextEvent(event);
                if (event.getEventType() == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                    result = event.getPackageName();
                }
            }


            if (modeLock.equals("immediately")) {

                if (database.lockDAO().isPackageLocked(result) != 0) {
                    showOverlayPassWord(result, "lockScreenApp");
                }

            } else if (modeLock.equals("screen_off")) {


                Log.d("wqrfsafa", "ffsafa");

                if (database.lockDAO().isLockedScreen(result) != 0) {
                    showOverlayPassWord(result, "lockScreenApp");
                }

            } else {

                if (database.lockDAO().isPackageLocked(result) != 0) {

                    Lock lock = database.lockDAO().getLockByPackageName(result);

                    if (lock != null) {

                        if (lock.isStateLockScreenAfterMinute()) {

                            // nếu nó đang là true thì show overlay

                            showOverlayPassWord(result, "lockScreenApp");

                        } else if (!lock.isStateLockScreenAfterMinute()) {
                            // false

                            Calendar currentTime = Calendar.getInstance();

                            int hour = currentTime.get(Calendar.HOUR_OF_DAY);
                            int minute = currentTime.get(Calendar.MINUTE);
//
                            int minuteCurrentTimeClose = hour * 60 + minute;

                            // khi mo ra tinh no la false
                            // lấy thời gian hiện tại trừ đi thời gian đã mở

                            // tính toán thời gian hiện tại
                            int timeLock = minuteCurrentTimeClose - Integer.parseInt(lock.getTimeOpen());

                            Log.d("9905923452", modeLock + " === " + timeLock);

                            if (timeLock >= Integer.parseInt(modeLock)) {
                                // nếu đã vượt quá thời gian mở khóa thì khóa nó lại
                                lock.setStateLockScreenAfterMinute(true);
                                database.lockDAO().updateLock(lock);
                            }
                        }

                    } else {

                        Log.d("fsadfsda", " -- " + modeLock);

                    }

                }
            }


            if (modeLock.equals("immediately")) {

                // app đó đang bị khóa và mode immediately

                if (currentPackageLock != null && currentPackageLock.length() > 2) {
                    if (!result.equals(packageTemp) && !result.equals("null") && !packageTemp.equals("null") && !result.equals("com.example.applock") && !packageTemp.equals("com.example.applock")) {

                        Log.d("5353fsdfdsaf", result);


                        Lock lock = database.lockDAO().getLockByPackageName(currentPackageLock);
                        lock.setStateLock(true);
                        database.lockDAO().updateLock(lock);
                        // sau khi update thanh cong clear currentPackageLock
                        currentPackageLock = "";

                    }
                }

            } else if (modeLock.equals("screen_off")) {

                if (currentPackageLock.trim().length() > 4) {

                    getLockByPackage(currentPackageLock);
                    currentPackageLock = "";

                }

                if (screenOff) {

                    if (!lockAppModeOffScreenList.isEmpty()) {
                        lockListAppOffScreen();
                        screenOff = false;
                        currentPackageLock = "";
                    }

                }

            }

            packageTemp = result;

        }
    }

    private void showOverlayPassWord(String result, String lockName) throws PackageManager.NameNotFoundException {


        if (lockName.equals("lockScreenApp")) {
            // send overlay lock screen

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

            if (bitmap != null) {


                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 1, baos);
                byte[] b = baos.toByteArray();

                Intent intent = new Intent(this, OverlayActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                intent.putExtra("package", result);
                intent.putExtra("picture", b);
                intent.putExtra("mode_lock", modeLock);

                intent.putExtra("lock_name", "lockScreenApp");

                startActivity(intent);

            }
        } else if (lockName.equals("lockRecentMenu")) {
            // lock recent menu apps


            Intent intent = new Intent(this, OverlayActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);


            intent.putExtra("lock_name", "lockRecentMenu");

            startActivity(intent);

        }


    }

    private void lockListAppOffScreen() {

        // danh sach cac package dang bi khoa

        Log.d("osdf3rfsadfaf", lockAppModeOffScreenList.size() + " ");

        for (int i = 0; i < lockAppModeOffScreenList.size(); i++) {
            Lock lock = lockAppModeOffScreenList.get(i);
            lock.setStateLockScreenOff(true);
            database.lockDAO().updateLock(lock);
        }

        lockAppModeOffScreenList.clear();

    }




    private void getLockByPackage(String currentLock) {

        Lock lock = database.lockDAO().getLockByPackageName(currentLock);

        if (lock != null) {

            if (!lockAppModeOffScreenList.contains(lock)) {
                // screenOff la mode tat man hinh de khoa
                lockAppModeOffScreenList.add(lock);
            }

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
        alarmService.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + 15 * 60 * 1000, // 15phut
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
        unregisterReceiver(receiverLockCurrent);
        unregisterReceiver(screenReceiverOn);
        super.onDestroy();
    }


//      class InnerReceiver extends BroadcastReceiver {
//        final String SYSTEM_DIALOG_REASON_KEY = "reason";
//        final String SYSTEM_DIALOG_REASON_RECENT_APPS = "recentapps";
//        final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";
//
//        @Override
//        public void onReceive(Context context, Intent intent) {
//
//            Log.d("r590wrsfas","hofsadfameee");
//
//
//            if (intent.getAction().equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
//                String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);
//                if (reason != null) {
//                        if (reason.equals(SYSTEM_DIALOG_REASON_HOME_KEY)) {
//                            Log.d("r590wrsfas","homeee");
//                            // Home Button click
//                        } else if (reason.equals(SYSTEM_DIALOG_REASON_RECENT_APPS)) {
//                            Log.d("r590wrsfas","recent");
//
//                            // RecentApp or Overview Button click
//                        }
//                }
//            }
//        }
//    }

//
//    public void scheduleLock(int minutes) {
//
//        counterTimeAlarm = true ;
//
//        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
//        Intent alarmIntent = new Intent(this, MyAlarmReceiver.class);
//        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE );
//
//        long interval = (long) minutes * 60 * 1000;
//
//        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + interval, interval, pendingIntent);
//
//    }


}
