package com.example.applock;

import android.Manifest;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.room.Room;
import androidx.viewpager.widget.ViewPager;

import com.example.applock.Interface.OnHomePressedListener;
import com.example.applock.adapter.ViewPagerAdapter;
import com.example.applock.db.LockDatabase;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;

import androidx.appcompat.app.ActionBarDrawerToggle;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static final String CHANNEL_DEFAULT_IMPORTANCE = "Service";
    private static final int REQUEST_OVERLAY_PERMISSION = 1111 ;
    ListView listView;
    TextView text;
    private String TAG = "MainActivity";
    private static final int REQUEST_USAGE_STATS_PERMISSION = 1;

    private static final int REQUEST_SYSTEM_ALERT_WINDOW = 1001;
    private static final int NOTIFICATION_ID = 1;
    Button btnStop;
    TabLayout mTabLayout;
    ViewPager mViewPager;
    Spinner spinnerSelected;
    Toolbar materialToolbar;
    SearchView searchView;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;

    SharedPreferences pref;

    LockDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_main);

        pref = MainActivity.this.getSharedPreferences("PREFS", 0);


        if (!Settings.canDrawOverlays(this)) {
            // Quyền chưa được cấp, bạn cần yêu cầu quyền
            requestOverlayPermission();
        } else {
            // Quyền đã được cấp
            // Thực hiện các hành động khác ở đây
        }




        try {
            database = Room.databaseBuilder(this, LockDatabase.class, "locks_database")
                    .allowMainThreadQueries()
                    .build();

        } catch (Exception e) {
            e.printStackTrace();
        }


        // check quyen truy cap vao ung dung he thong
        requestUsageStatsPermission();

        int permissionState = ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS);
        // If the permission is not granted, request it.
        if (permissionState == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
        }

        materialToolbar = findViewById(R.id.toolbar);

        materialToolbar.setTitleTextColor(Color.WHITE);

        setSupportActionBar(materialToolbar);

        materialToolbar.setBackgroundColor(getColor(R.color.bg_toolbar));

        getSupportActionBar().setDisplayShowTitleEnabled(false);

        spinnerSelected = this.<Spinner>findViewById(R.id.spinner_nav);

        mTabLayout = this.<TabLayout>findViewById(R.id.tab_layout_activity);

        mViewPager = this.<ViewPager>findViewById(R.id.viewpagerHome);

        setSupportActionBar(materialToolbar);

        drawerLayout = findViewById(R.id.drawerlayout);

        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.nav_open, R.string.nav_close);

        drawerLayout.addDrawerListener(actionBarDrawerToggle);

        actionBarDrawerToggle.syncState();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getSupportActionBar().setHomeButtonEnabled(true);

        ArrayAdapter<CharSequence> adapterSpinner = ArrayAdapter.createFromResource(this, R.array.spinner_list_item_array, android.R.layout.simple_spinner_item);

        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerSelected.setAdapter(adapterSpinner);

        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        mViewPager.setAdapter(viewPagerAdapter);

        mTabLayout.addTab(mTabLayout.newTab().setText("APP LIST"));

        mTabLayout.addTab(mTabLayout.newTab().setText("SETTINGS"));
        mTabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTabLayout));

        mViewPager.setCurrentItem(0);

        searchView = this.<SearchView>findViewById(R.id.searchNav);

        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mViewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });


        HomeWatcher mHomeWatcher = new HomeWatcher(this);
        mHomeWatcher.setOnHomePressedListener(new OnHomePressedListener() {
            @Override
            public void onHomePressed() {

                Log.e("AHSAN", "onHomePressed: ");
            }


            @Override
            public void onHomeLongPressed() {
                Log.e("AHSAN", "onHomePressed: ");

            }
        });


//        spinnerSelected.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                ((TextView) view).setTextColor(Color.WHITE);
//                ((TextView) view).setTextSize(18);
//
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//
//            }
//        });

        int appBarPadding = getResources().getDimensionPixelSize(R.dimen.app_bar_padding);
        int menuItemSize = getResources().getDimensionPixelSize(R.dimen.app_bar_size_menu_item);
        searchView.setMaxWidth(materialToolbar.getWidth() * appBarPadding - menuItemSize);


        searchView.setIconified(true);

        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchView.setMaxWidth(1999);
//                 searchView.setBackgroundColor(getResources().getColor(R.color.white,null));
                spinnerSelected.setVisibility(View.GONE);
            }
        });

        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                spinnerSelected.setVisibility(View.VISIBLE);
                return false;
            }
        });


    }

    private void requestOverlayPermission() {
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
        startActivityForResult(intent, REQUEST_OVERLAY_PERMISSION);
    }



    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);

                return true;
        }

        return super.onOptionsItemSelected(item);
    }



    private void requestUsageStatsPermission() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {

            requestSystemAlertWindowPermission();
        } else {

            // showOverlayWindow();
        }


        if (!hasUsageStatsPermission()) {
            Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
            startActivityForResult(intent, REQUEST_USAGE_STATS_PERMISSION);
        }
    }

    private void requestSystemAlertWindowPermission() {
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
        startActivity(intent);
    }

    private boolean hasUsageStatsPermission() {
        AppOpsManager appOps = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), getPackageName());
        return mode == AppOpsManager.MODE_ALLOWED;
    }


    private boolean isAccessGranted() {

        try {
            PackageManager packageManager = getPackageManager();
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(getPackageName(), 0);
            AppOpsManager appOpsManager = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
            int mode = 0;
            if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.KITKAT) {
                mode = appOpsManager.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, applicationInfo.uid, applicationInfo.packageName);
            }
            return (mode == AppOpsManager.MODE_ALLOWED);

        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }

    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {


        menuItem.setChecked(true);

        drawerLayout.closeDrawers();


        return false;
    }


    //    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//
//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.menu_search, menu);
//
//        MenuItem searchMenuItem = menu.findItem(R.id.nav_search);
//        if (searchMenuItem == null) {
//            return true;
//        }
//
//        searchView = (SearchView) searchMenuItem.getActionView();
//
//        searchView.setIconifiedByDefault(false);
//        searchView.setIconified(false);
//        searchView.setMaxWidth(Integer.MAX_VALUE);
//        searchView.setIconifiedByDefault(false);
//        int appBarPadding = getResources().getDimensionPixelSize(R.dimen.app_bar_padding);
//        int menuItemSize = getResources().getDimensionPixelSize(R.dimen.app_bar_size_menu_item);
//        searchView.setMaxWidth(materialToolbar.getWidth() * appBarPadding - menuItemSize);
//
//        ImageView searchIconView = (ImageView) searchView.findViewById(androidx.appcompat.R.id.search_button);
//        searchIconView.setVisibility(View.GONE);
//
//        searchMenuItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
//            @Override
//            public boolean onMenuItemActionExpand(MenuItem item) {
//
//                if (getSupportActionBar() != null) {
//                ImageView searchIcon = (ImageView)searchView.findViewById(R.id.sear);
//                    searchIcon.setImageDrawable(null);
//                    getSupportActionBar().setDisplayShowTitleEnabled(false);
//                    getSupportActionBar().setDisplayHomeAsUpEnabled(false);
//                    int searchIconId = getResources().getIdentifier("android:id/search_mag_icon", null, null);
//                    ImageView searchIcon = searchView.findViewById(searchIconId);

//                    searchIcon.setVisibility(View.GONE);
//
//                    searchView.setBackground(getDrawable(R.drawable.bg_searchview));
//
//                }
//                return true;
//            }
//
//            @Override
//            public boolean onMenuItemActionCollapse(MenuItem item) {
//
//                return true;
//            }
//        });
//
//
//        return super.onCreateOptionsMenu(menu);
//    }


//    @Override
//    public void onBackPressed() {
//        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
//            mDrawerLayout.closeDrawer(GravityCompat.START);
//        } else {
//            super.onBackPressed();
//        }
//    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_HOME) {
            Log.d("52fgdgsdgsdgdsgsg","hufuhfduhisfudishfdushi");
            // Xử lý khi nút BACK được nhấn
            return true; // Trả về true để ngăn chặn hành động mặc định của nút BACK
        }
        return super.onKeyDown(keyCode, event);
    }


}