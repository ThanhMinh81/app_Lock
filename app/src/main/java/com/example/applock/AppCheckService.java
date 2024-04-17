package com.example.applock;

import static com.example.applock.MyApplication.CHAINNEL_ID;

import android.accessibilityservice.AccessibilityService;
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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
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
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
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

    boolean checkThread = true;
    private String appName;
    private ApplicationInfo applicationInfo;
    private String colorBackgroundPassword;

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

            UsageStatsManager usageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
            long currentTime = System.currentTimeMillis();
            List<UsageStats> stats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, currentTime - 1000 * 10, currentTime);

            if (checkThread) {
//                try {
//                    Thread.sleep(200);
//                } catch (InterruptedException e) {
//                    throw new RuntimeException(e);
//                }


                if (stats != null) {

                    SortedMap<Long, UsageStats> runningTasks = new TreeMap<>();

                    for (UsageStats usageStats : stats) {
                        runningTasks.put(usageStats.getLastTimeUsed(), usageStats);
                    }

                    if (!runningTasks.isEmpty()) {

                        String packageName = runningTasks.get(runningTasks.lastKey()).getPackageName();

                        PackageManager packageManager = getPackageManager();

                        try {

                              applicationInfo = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA);


                            // Lấy biểu tượng ứng dụng dưới dạng Drawable
                            Drawable iconDrawable = applicationInfo.loadIcon(packageManager);

                            // Chuyển đổi Drawable thành Bitmap
                            Bitmap iconBitmap = getBitmapFromDrawable(iconDrawable);

                            if (iconBitmap != null) {
                                // Tính toán tọa độ dựa trên phần trăm của kích thước biểu tượng
                                int pointX = (int) (0.3 * iconBitmap.getWidth());
                                int pointY = (int) (0.3 * iconBitmap.getHeight());

                                // Lấy màu tại điểm được chỉ định
                                int color = iconBitmap.getPixel(pointX, pointY);

                                // Chuyển đổi màu sang chuỗi hex
                                 colorBackgroundPassword = String.format("#%06X", (0xFFFFFF & color));
//                                Log.d("532455",colorBackgroundPassword.toString());
                            } else {

                            }


















                            packageName = applicationInfo.packageName;

                            // lay app hien tai dang mo
                            appName = (String) packageManager.getApplicationLabel(applicationInfo);





                            // appName = youtube
                            // currentAppsTemp = lockapp
                            // youtube thuoc lock  ==> show


                            // appName = message
                            // currentAppsTemp  = youtube
                            // message thuoc lock ==> show


                            // appName = youtube
                            // currentAppsTemp = lockapp
                            // yoututbe ==> show


                            // appName = home
                            // currentAppsTemp = youtube
                            // ko show

                            // appName = youtube
                            // currentTempt == home
                            /// ytb thuoc ==> show


                            // appName = currentAppsTempt = home
                            // ytb != home
                            // thread stop


                            //  appName =  gần đây


                            if (names.contains(appName)) {
                                if (currentAppsTemp != appName) {

                                    Log.d("anhlatdepjtrai", appName);

                                    mHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            showOverlayPassWord();
//                                        showOverlayPasssWord2();
                                        }
                                    });
                                }
                            }

                            // curentapp tmp = youtube

                            currentAppsTemp = appName;
                            // cai nay de check khi nguoi dung bam ung dung gan day
                            recentappsApp = appName;
//                            Log.d("loiroaioa", appName);

                        } catch (PackageManager.NameNotFoundException e) {
                        }
                    }
                }
            } else {

                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                SortedMap<Long, UsageStats> runningTasks = new TreeMap<>();

                for (UsageStats usageStats : stats) {
                    runningTasks.put(usageStats.getLastTimeUsed(), usageStats);
                }

                if (!runningTasks.isEmpty()) {

                    String packageName = runningTasks.get(runningTasks.lastKey()).getPackageName();

                    PackageManager packageManager = getPackageManager();

                    try {

                        ApplicationInfo applicationInfo = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA);

                        // lay app hien tai dang mo
                        appName = (String) packageManager.getApplicationLabel(applicationInfo);

                        if (currentAppsTemp != appName) {
                            checkThread = true;
                        }

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

                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    hideOverlayPassword(myview);


                } else if (reason.equals("recentapps")) {

                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    currentAppsTemp = appName;
                    Log.d("34234", "sadfaf");
                    Log.d("appps", currentAppsTemp + " = = " + recentappsApp);
                    hideOverlayPassword(myview);

                    checkThread = false;
                    // duwnfg thread
                    currentAppsTemp = appName;

                    // dung thread
                    // cho appName va currentTempt = nhau
                    // nếu tên đó mà có trong mảng thì bật lại

                    Log.d("34234", "sadfaf");
                    Log.d("appps", currentAppsTemp + " = = " + recentappsApp);
                    hideOverlayPassword(myview);

                }
            }
        };



        myview = li.inflate(R.layout.password_layout, wrapper);

        ConstraintLayout layoutPassWord = myview.findViewById(R.id.layoutPassword);
        layoutPassWord.setBackgroundColor(Color.parseColor(colorBackgroundPassword));

        sharedPreferences = getSharedPreferences("PREFS", Context.MODE_PRIVATE);
        String password = sharedPreferences.getString("password", "null");
        LayoutInflater inflater = LayoutInflater.from(this);
        ImageView imgIcon = myview.findViewById(R.id.img_iconApp);
        imgIcon.setImageDrawable(applicationInfo.loadIcon(getApplicationContext().getPackageManager()));
        TextView tvName = myview.findViewById(R.id.tvName);
//        PatternLockView lockView = myview.findViewById(R.id.pattern_lock_view);
        windowManager.addView(myview, params);

        tvName.setOnClickListener(v -> Log.d("2121212", "fsadfafafsa"));

        myview.setFocusable(true);
        myview.setFocusableInTouchMode(true);
        myview.requestFocus();
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
//
//                if (password.equals(PatternLockUtils.patternToString(lockView, pattern))) {
//
//                    hideOverlayPassword(myview);
//
//                } else {
//
//                }
//
//            }
//
//            @Override
//            public void onCleared() {
//
//            }
//        });


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



}
