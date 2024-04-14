package com.example.applock.fragment;



import static android.app.ProgressDialog.show;

import android.app.job.JobInfo;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

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
    private static final int JOB_ID =123 ;
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





//        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
//        startActivity(intent);


//        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
//        startActivity(intent);


        Intent intent = new Intent(getContext(), AppCheckService.class);
//        intent.putExtra("listData", locks);
        getContext().startService(intent);




//       try {
//           JobSchedulerHelper.scheduleJob(getContext());
//       }catch (Exception e)
//       {
//           Log.d("fsafa",e.toString());
//       }

//        checkAccessibilityPermission();

        return view;
    }

    public boolean checkAccessibilityPermission () {
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


}
