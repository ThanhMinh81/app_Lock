package com.example.applock.service;

import static com.example.applock.MyApplication.CHAINNEL_ID;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
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
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.NotificationCompat;
import androidx.room.Room;

import com.andrognito.patternlockview.PatternLockView;
import com.andrognito.patternlockview.listener.PatternLockViewListener;
import com.andrognito.patternlockview.utils.PatternLockUtils;
import com.example.applock.Broadcast;
import com.example.applock.R;
import com.example.applock.db.Lock;
import com.example.applock.db.LockDatabase;
import com.example.applock.fragment.HomeFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public class LockService extends Service {

    private static final String TAG = "LockService";
    private static final int CHANNEL_DEFAULT_IMPORTANCE_SERVICE = 1;

    public static Notification notification;

    ArrayList<Lock> locks = new ArrayList<>();

    // list app lock
    ArrayList<String> names = new ArrayList<>();

    boolean patternMode = true;

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

    private String appName;
    private ApplicationInfo applicationInfo;
    private String colorBackgroundPassword;

    String passwordStr = "";

    boolean serviceRunning = true;


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
    //
    private void checkRunningApps() {
        while (true) {

            if (serviceRunning) {

                UsageStatsManager usageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
                long currentTime = System.currentTimeMillis();
                List<UsageStats> stats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, currentTime - 10000, currentTime);

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

                            Drawable iconDrawable = applicationInfo.loadIcon(packageManager);


                            Bitmap iconBitmap = getBitmapFromDrawable(iconDrawable);

                            if (iconBitmap != null) {
                                int pointX = (int) (0.3 * iconBitmap.getWidth());
                                int pointY = (int) (0.3 * iconBitmap.getHeight());

                                int color = iconBitmap.getPixel(pointX, pointY);

                                colorBackgroundPassword = String.format("#%06X", (0xFFFFFF & color));
                            } else {

                            }

                            packageName = applicationInfo.packageName;

                            Log.d("f3943434",packageName.toString());

                            appName = (String) packageManager.getApplicationLabel(applicationInfo);

                            boolean check = isAppActive(1000);

                            Log.d("532455", check + " ");


                            if (names.contains(appName)) {
                                if (currentAppsTemp != appName) {
                                    mHandler.post(new Runnable() {
                                        @Override
                                        public void run() {

                                            showOverlayPassWord();

                                        }
                                    });
                                }
                            }

                            currentAppsTemp = appName;

                            Log.d("43043244fsdoifs", currentAppsTemp + " == " + appName);

//                            recentappsApp = appName;

                        } catch (PackageManager.NameNotFoundException e) {
                        }
                    }
                }
            }


        }
    }

    private void showOverlayPassWord() {

        int layoutParamsType;

        ImageView imgChangeMode;

        PatternLockView patternLockView;

        TableLayout tableLayout;

        TextView tvNameMode;

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
                    if (myview.isAttachedToWindow()) {
                        hideOverlayPassword(myview);
                    }
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

//                    try {
//                        Thread.sleep(100);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }

//                    if (myview.isAttachedToWindow()) {
                        hideOverlayPassword(myview);
//                    }


                } else if (reason.equals("recentapps")) {

//                    serviceRunning = false ;
//                    currentAppsTemp = appName;
//                    Log.d("34234", "sadfaf");
                    Log.d("appps", currentAppsTemp + " = = " + recentappsApp);

                    if (myview.isAttachedToWindow()) {
                        hideOverlayPassword(myview);
                    }


                    // dung thread
                    // cho appName va currentTempt = nhau
                    // nếu tên đó mà có trong mảng thì bật lại

//                    Log.d("34234", "sadfaf");
//                    Log.d("appps", currentAppsTemp + " = = " + recentappsApp);
//                    hideOverlayPassword(myview);

                }
            }
        };

        myview = li.inflate(R.layout.password_layout, wrapper);

        TextView tvPassword = myview.findViewById(R.id.tvShowPassWord);
        imgChangeMode = myview.findViewById(R.id.imgChangeModeComfirm);
        tableLayout = myview.findViewById(R.id.tableLayout);
        patternLockView = myview.findViewById(R.id.pattern_lock_view);
        tvNameMode = myview.findViewById(R.id.tvNameMode);

        ImageView imgIconApp = myview.findViewById(R.id.img_iconApp);

        imgIconApp.setImageDrawable(applicationInfo.loadIcon(getApplicationContext().getPackageManager()));


        Button btn1 = myview.findViewById(R.id.btn1);
        Button btn2 = myview.findViewById(R.id.btn2);
        Button btn3 = myview.findViewById(R.id.btn3);
        Button btn4 = myview.findViewById(R.id.btn4);
        Button btn5 = myview.findViewById(R.id.btn5);
        Button btn6 = myview.findViewById(R.id.btn6);
        Button btn7 = myview.findViewById(R.id.btn7);
        Button btn8 = myview.findViewById(R.id.btn8);
        Button btn9 = myview.findViewById(R.id.btn9);


        if (patternMode) {

            tvPassword.setVisibility(View.GONE);
            tvNameMode.setText("Draw pattern");
            tableLayout.setVisibility(View.GONE);
            patternLockView.setVisibility(View.VISIBLE);

        } else {

            // xet them cho no lenght cua pass
            tvPassword.setVisibility(View.VISIBLE);
            tvNameMode.setText("Enter pin");
            patternLockView.setVisibility(View.GONE);
            tableLayout.setVisibility(View.VISIBLE);

        }

        imgChangeMode.setOnClickListener(v -> {

            patternMode = !patternMode;

            if (patternMode) {
                tvPassword.setVisibility(View.GONE);
                tvNameMode.setText("Draw pattern");
                tableLayout.setVisibility(View.GONE);
                patternLockView.setVisibility(View.VISIBLE);

            } else {
                tvPassword.setVisibility(View.VISIBLE);
                tvNameMode.setText("Enter pin");
                patternLockView.setVisibility(View.GONE);
                tableLayout.setVisibility(View.VISIBLE);
            }

        });


        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                passwordStr += "1";
                tvPassword.setText(passwordStr);
            }
        });
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                passwordStr += "2";
                tvPassword.setText(passwordStr);
            }
        });
        btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                passwordStr += "3";
                tvPassword.setText(passwordStr);
            }
        });
        btn4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                passwordStr += "4";
                tvPassword.setText(passwordStr);
            }
        });
        btn5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                passwordStr += "5";
                tvPassword.setText(passwordStr);
            }
        });
        btn6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                passwordStr += "6";
                tvPassword.setText(passwordStr);
            }
        });
        btn7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                passwordStr += "7";
                tvPassword.setText(passwordStr);
            }
        });
        btn8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                passwordStr += "8";
                tvPassword.setText(passwordStr);
            }
        });
        btn9.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                passwordStr += "9";
                tvPassword.setText(passwordStr);
            }
        });


        ConstraintLayout layoutPassWord = myview.findViewById(R.id.layoutPassword);
        layoutPassWord.setBackgroundColor(Color.parseColor(colorBackgroundPassword));

        sharedPreferences = getSharedPreferences("PREFS", Context.MODE_PRIVATE);
        String password = sharedPreferences.getString("password", "null");
        LayoutInflater inflater = LayoutInflater.from(this);

        windowManager.addView(myview, params);
        patternLockView.addPatternLockListener(new PatternLockViewListener() {
            @Override
            public void onStarted() {}

            @Override
            public void onProgress(List<PatternLockView.Dot> progressPattern) {}

            @Override
            public void onComplete(List<PatternLockView.Dot> pattern) {

                if (password.equals(PatternLockUtils.patternToString(patternLockView, pattern))) {

                    if (myview.isAttachedToWindow()) {
                        hideOverlayPassword(myview);
                    }

                } else {

                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            tvNameMode.setText("Error");
                            patternLockView.setViewMode(PatternLockView.PatternViewMode.WRONG);
                        }
                    }, 200);

                    patternLockView.clearPattern();
                }

            }

            @Override
            public void onCleared() {}
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


    public boolean isAppActive(long durationInSeconds) {
        UsageStatsManager usageStatsManager = (UsageStatsManager) getApplicationContext().getSystemService(Context.USAGE_STATS_SERVICE);
        long currentTime = System.currentTimeMillis();
        long startTime = currentTime - (durationInSeconds * 1000);

        List<UsageStats> usageStatsList = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, currentTime);
        if (usageStatsList != null) {
            for (UsageStats usageStats : usageStatsList) {
                if (usageStats.getPackageName().equals(packageName)) {
                    // Check xem thời gian hoạt động của ứng dụng vượt quá thời gian được chỉ định hay không
                    if (usageStats.getLastTimeUsed() >= startTime) {
                        return true;
                    }
                }
            }
        }
        return false;
    }


}
