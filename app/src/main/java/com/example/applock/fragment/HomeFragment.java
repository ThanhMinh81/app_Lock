package com.example.applock.fragment;


import static android.app.ProgressDialog.show;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.example.applock.service.LockService;
import com.example.applock.Interface.ItemClickListenerLock;
import com.example.applock.R;
import com.example.applock.adapter.HomeAdapter;
import com.example.applock.db.Lock;
import com.example.applock.db.LockDatabase;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {
    private static final int JOB_ID = 123;
    View view;
    HomeAdapter homeAdapter;
    RecyclerView rcvHome;
    ArrayList<ApplicationInfo> infoArrayList;
    ItemClickListenerLock itemClickListenerLock;
    LockDatabase lockDatabase;

    ArrayList<Lock> locks;

    LockDatabase database;

    Button btnOff;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_home, container, false);
        rcvHome = view.findViewById(R.id.rcvHome);
        btnOff = view.findViewById(R.id.btnOFF);

        infoArrayList = new ArrayList<>();
        locks = new ArrayList<>();

        database = Room.databaseBuilder(getActivity(), LockDatabase.class, "locks_database")
                .allowMainThreadQueries()
                .build();

        // them tat ca item lock vao cho locks
        locks.addAll(getListAppLock());

        Intent intent = new Intent(getContext(), LockService.class);

        getContext().startService(intent);


        // send list adappter
        ArrayList<String> strings = new ArrayList<>();



        itemClickListenerLock = new ItemClickListenerLock() {

            @Override
            public void clickItemLock(Lock lock, boolean removeItemLock) {
                if (removeItemLock) {
                    Log.d("42344145", "43");
                    // xoa element app lock
                    database.lockDAO().removeAppLock(lock.getIdApp());
                    // gui lai list duoc updat cho service
                    locks.remove(lock);
                    Intent intent1 = new Intent(getContext(), LockService.class);
                    intent.putParcelableArrayListExtra("listLock", locks);
                    getContext().startService(intent);
                } else {
                    database.lockDAO().insert(lock);
                    locks.add(lock);
                    intent.putParcelableArrayListExtra("listLock", locks);
                    getContext().startService(intent);
                }
            }
        };


        homeAdapter = new HomeAdapter(getContext(), infoArrayList, itemClickListenerLock, locks);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);

        rcvHome.setLayoutManager(layoutManager);
        rcvHome.setAdapter(homeAdapter);

        btnOff.setOnClickListener(v -> {
            Intent intent1 = new Intent(getContext(), LockService.class);
            getContext().stopService(intent1);
        });


        for (Lock s : locks) {
            strings.add(s.getName());
        }

        homeAdapter.setArrayLock(strings);
        homeAdapter.notifyDataSetChanged();

        getListAppSystem();


        return view;
    }

    public boolean checkAccessibilityPermission() {
        int accessEnabled = 0;
        try {
            accessEnabled = Settings.Secure.getInt(getContext().getContentResolver(), Settings.Secure.ACCESSIBILITY_ENABLED);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        if (accessEnabled == 0) {

            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);

            return false;

        } else {
            return true;
        }
    }


    private void getListAppSystem() {
        List<ApplicationInfo> installedApps = getContext().getPackageManager().getInstalledApplications(PackageManager.GET_META_DATA);
        int i = 0;
        ArrayList<ApplicationInfo> applicationInfos = new ArrayList<>();

        for (ApplicationInfo appInfo : installedApps) {
            if (getContext().getPackageManager().getLaunchIntentForPackage(appInfo.packageName) != null) {
                applicationInfos.add(appInfo);
            }
        }
        infoArrayList.addAll(applicationInfos);
        homeAdapter.notifyDataSetChanged();

    }

    private ArrayList<Lock> getListAppLock() {
        ArrayList<Lock> lockArrayList = new ArrayList<>();
        lockArrayList.addAll(database.lockDAO().getListApps());
        return lockArrayList;
    }


}
