package com.example.applock;

import static com.example.applock.MyApplication.CHAINNEL_ID;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlarmManager;
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
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.SystemClock;
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
import androidx.room.Room;

import com.andrognito.patternlockview.PatternLockView;
import com.andrognito.patternlockview.listener.PatternLockViewListener;
import com.andrognito.patternlockview.utils.PatternLockUtils;
import com.example.applock.db.Lock;
import com.example.applock.db.LockDatabase;
import com.example.applock.fragment.HomeFragment;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;

public class AppCheckService extends AccessibilityService {

    private static final String TAG = "AppCheckService";
    private static final int CHANNEL_DEFAULT_IMPORTANCE_SERVICE = 1;

    public static Notification notification;

    ArrayList<Lock> locks = new ArrayList<>();

    ArrayList<String> names = new ArrayList<>();

    String currentAppsTempt = "";

    String recentappsApp = "";

    private Broadcast mBroadcast;

    WindowManager windowManager;

    View passwordView;

    private Handler mHandler;
    View myview;
    SharedPreferences sharedPreferences;

    String packageName ="";

    @Override
    public void onCreate() {

        HandlerThread handlerThread = new HandlerThread("MyHandlerThread");
        handlerThread.start();
        Looper looper = handlerThread.getLooper();
        mHandler = new Handler(looper);


        LockDatabase database = Room.databaseBuilder(getApplicationContext(), LockDatabase.class, "locks_database")
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


        Log.d(TAG, "Service started");
//
//        locks = new ArrayList<>();
//        mHandler = new Handler(getMainLooper());


//      if(intent.getParcelableArrayListExtra("listData") != null)
//      {
//          locks = intent.getParcelableArrayListExtra("listData");
        for (Lock lock : locks) {
            Log.d("fsafas", lock.getName() + " ");
        }
//      }


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

            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

//            Log.d("fdsfafa", currentAppsTempt);

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
                        packageName = applicationInfo.packageName ;

                        String appName = (String) packageManager.getApplicationLabel(applicationInfo);
//                        String nameTempt = appName ;

                        if (names.contains(appName)) {
                            if (currentAppsTempt != appName) {
                                Log.d("trueeeeaa", appName);
                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        showOverlayPassWord();
                                    }
                                });
                            }

                        }

                        currentAppsTempt = appName;
                        // cai nay de check khi nguoi dung bam ung dung gan day
                        recentappsApp = appName ;


                        Log.d("loiroaioa", appName);
                    } catch (PackageManager.NameNotFoundException e) {
                    }
                }
            }

        }
    }


    private void showPassword() {
        try {
            // Tạo một Intent để chuyển từ Service sang Activity
            Intent activityIntent = new Intent(AppCheckService.this, OverlayActivity.class);
            // Đưa các dữ liệu cần thiết vào Intent nếu cần
            activityIntent.putExtra("key", "value");
            // Bắt đầu Activity mới từ Service
            activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(activityIntent);
        } catch (Exception e) {
            Log.d("Fsfsaf", e.toString());
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
                0,
                PixelFormat.TRANSLUCENT
        );
        params.gravity = Gravity.CENTER;
        params.x = 0;
        params.y = 0;

        FrameLayout wrapper = new FrameLayout(this) {
            @Override
            public boolean dispatchKeyEvent(KeyEvent event) {
                if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
                    removeView22(myview);
                    // handle the back button code;
                    return true;
                }
                return super.dispatchKeyEvent(event);
            }

            public void onCloseSystemDialogs(String reason) {
                System.out.println("System dialog " + reason);

                if (reason.equals("homekey")) {

                    Intent homeIntent = new Intent(Intent.ACTION_MAIN);
                    homeIntent.addCategory(Intent.CATEGORY_HOME);
                    homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(homeIntent);

                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            removeView22(myview);
                        }
                    }, 200);

                } else if (reason.equals("recentapps")) {
                    if(recentappsApp.equals(currentAppsTempt))
                    {
                        removeView22(myview);




                    }else {
                        showOverlayPassWord();

                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {

                                PackageManager packageManager = getPackageManager();
                                Intent launchIntent = packageManager.getLaunchIntentForPackage(packageName);

                                if (launchIntent != null) {
                                    startActivity(launchIntent);
                                } else {
                                }

                            }
                        }, 1000);
                    }
                }
            }


        };

        myview = li.inflate(R.layout.password_layout, wrapper);   // here set into your own layout
        windowManager.addView(myview, params);


//        sharedPreferences = getSharedPreferences("PREFS", Context.MODE_PRIVATE);
//
//        String password = sharedPreferences.getString("password", "null");
//
//
//        LayoutInflater inflater = LayoutInflater.from(this);
//        View overlayView = inflater.inflate(R.layout.password_layout, null);
//        ImageView imgIcon = overlayView.findViewById(R.id.img_iconApp);
//        TextView tvName = overlayView.findViewById(R.id.tvName);
//        PatternLockView lockView = overlayView.findViewById(R.id.pattern_lock_view);
//
//        overlayView.setFocusable(true);
//        overlayView.setFocusableInTouchMode(true);
//        overlayView.requestFocus();
//
//        // Thêm sự kiện KeyEvent cho overlayView
//      overlayView = new FrameLayout(this)
//      {
//          @Override
//          public boolean dispatchKeyEvent(KeyEvent event) {
//              if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
//                  Log.v(TAG, "BACK Button Pressed");
//                  return true;
//              }
//              return super.dispatchKeyEvent(event);
//          }
//
//          public void onCloseSystemDialogs(String reason) {
//              Log.d("fsdfsadfa",)
//              if (reason.equals("homekey")) {
//                  Intent homeIntent = new Intent(Intent.ACTION_MAIN);
//                  homeIntent.addCategory(Intent.CATEGORY_HOME);
//                  homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                  getApplication().startActivity(homeIntent);
//
//              }
//          }
//
//      };
//
//
//        windowManager.addView(overlayView, params);
    }

    private void removeView22(View view) {
        windowManager.removeView(view);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBroadcast);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        int eventType = accessibilityEvent.getEventType();
        switch (eventType) {
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                // Xử lý sự kiện thay đổi trạng thái của cửa sổ
                String packageName = accessibilityEvent.getPackageName().toString();
                String className = accessibilityEvent.getClassName().toString();
                System.out.println("Opened package: " + packageName + ", class: " + className);
                // Xử lý logic của bạn khi một ứng dụng được mở
                break;
            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:
                // Xử lý sự kiện thay đổi nội dung cửa sổ
                break;
            // Các loại sự kiện khác có thể được xử lý tùy ý
        }
    }

    @Override
    public void onInterrupt() {

    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        // Cấu hình Accessibility Service để theo dõi sự kiện cụ thể
        setServiceInfo();
    }

    private void setServiceInfo() {
        // Thiết lập các thuộc tính cho Accessibility Service
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        // Chỉ định loại sự kiện mà service sẽ theo dõi
        info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED | AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED;
        // Không giới hạn gói ứng dụng nào được theo dõi (null)
        info.packageNames = null;
        // Chỉ định các tính năng mà service sẽ cung cấp
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        // Thiết lập thông tin service
        setServiceInfo(info);
    }


}
