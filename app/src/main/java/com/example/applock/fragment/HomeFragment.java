package com.example.applock.fragment;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.example.applock.AppCheckService;
import com.example.applock.Interface.ItemClickListenerLock;
import com.example.applock.MainActivity;
import com.example.applock.R;
import com.example.applock.adapter.HomeAdapter;
import com.example.applock.db.Lock;
import com.example.applock.db.LockDatabase;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {
    View view;
    HomeAdapter homeAdapter;
    RecyclerView rcvHome;
    ArrayList<ApplicationInfo> infoArrayList;

    ItemClickListenerLock itemClickListenerLock;

    LockDatabase lockDatabase;

    ArrayList<Lock> locks;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_home, container, false);
        rcvHome = view.findViewById(R.id.rcvHome);

        infoArrayList = new ArrayList<>();
        locks = new ArrayList<>();

        LockDatabase database = Room.databaseBuilder(getActivity(), LockDatabase.class, "locks_database")
                .allowMainThreadQueries()
                .build();

        locks.addAll(database.lockDAO().getListApps());

        itemClickListenerLock = new ItemClickListenerLock() {
            @Override
            public void clickItemLock(Lock lock) {
                database.lockDAO().insert(lock);
            }
        };


        homeAdapter = new HomeAdapter(getContext(), infoArrayList, itemClickListenerLock);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);

        rcvHome.setLayoutManager(layoutManager);
        rcvHome.setAdapter(homeAdapter);

        getListAppSystem();

        Intent intent = new Intent(getContext(), AppCheckService.class);
        intent.putExtra("listData", locks);
        getContext().startService(intent);


        return view;
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


}
