package com.example.applock.fragment;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
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

import com.example.applock.MainActivity;
import com.example.applock.service.LockService;
import com.example.applock.Interface.ItemClickListenerLock;
import com.example.applock.R;
import com.example.applock.adapter.HomeAdapter;
import com.example.applock.model.Lock;
import com.example.applock.db.LockDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class HomeFragment extends Fragment {
    private static final int JOB_ID = 123;
    View view;
    HomeAdapter homeAdapter;
    RecyclerView rcvHome;
    ArrayList<ApplicationInfo> infoArrayList;
    ItemClickListenerLock itemClickListenerLock;
    LockDatabase lockDatabase;

    ArrayList<Lock> appSystem;

    LockDatabase database;

    Button btnOff;


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        appSystem = new ArrayList<>();

        if (context instanceof MainActivity) {
            MainActivity mainActivity = (MainActivity) context;
            database = mainActivity.getDatabase();
        }

        // Kiểm tra xem cơ sở dữ liệu đã được khởi tạo hay chưa
        if (database != null) {
            Log.d("095348", "fasidfjsiafa");

            List<Lock> locks = database.lockDAO().getListApps();
            if (locks != null && !locks.isEmpty()) {
                ArrayList<Lock> lockArrayList = new ArrayList<>();
                // nếu db đã có table
                appSystem.clear();
                lockArrayList.addAll(database.lockDAO().getListApps());
                appSystem.addAll(setApplications(lockArrayList));


            } else {

                // ban đầu người dùng lần đầu tiên cài apps
                appSystem.addAll(getListAppSystem());
                database.lockDAO().insertLocks(appSystem);

            }
        }


    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_home, container, false);
        rcvHome = view.findViewById(R.id.rcvHome);
        btnOff = view.findViewById(R.id.btnOFF);

        infoArrayList = new ArrayList<>();

        Intent intent = new Intent(getContext(), LockService.class);
        getContext().startService(intent);


//        itemClickListenerLock = new ItemClickListenerLock() {
//
//            @Override
//            public void clickItemLock(Lock lock, boolean removeItemLock) {
//                if (removeItemLock) {
//                    Log.d("42344145", "43");
//                    // xoa element app lock
//                    database.lockDAO().removeAppLock(lock.getIdApp());
//                    // gui lai list duoc updat cho service
//                    locks.remove(lock);
//                    Intent intent1 = new Intent(getContext(), LockService.class);
//                    intent.putParcelableArrayListExtra("listLock", locks);
//                    getContext().startService(intent);
//                } else {
//                    database.lockDAO().insert(lock);
//                    locks.add(lock);
//                    intent.putParcelableArrayListExtra("listLock", locks);
//                    getContext().startService(intent);
//                }
//            }
//        };


        homeAdapter = new HomeAdapter(getContext(), appSystem, database);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);

        rcvHome.setLayoutManager(layoutManager);
        rcvHome.setAdapter(homeAdapter);
        homeAdapter.notifyDataSetChanged();

//        btnOff.setOnClickListener(v -> {
//            Intent intent1 = new Intent(getContext(), LockService.class);
//            getContext().stopService(intent1);
//        });


        return view;
    }


    // khoi tao list app he thong
    private ArrayList<Lock> getListAppSystem() {
        ArrayList<Lock> lockArrayList = new ArrayList<>();
        List<ApplicationInfo> installedApps = getContext().getPackageManager().getInstalledApplications(PackageManager.GET_META_DATA);
        ArrayList<ApplicationInfo> applicationInfos = new ArrayList<>();

        for (ApplicationInfo appInfo : installedApps) {


            if (getContext().getPackageManager().getLaunchIntentForPackage(appInfo.packageName) != null) {
//                Log.d("523453253",appInfo.packageName);
                Lock lock = new Lock(0, false, appInfo.packageName, appInfo);
                lockArrayList.add(lock);
            }
        }
        return lockArrayList;

    }

    private ArrayList<Lock> setApplications(ArrayList<Lock> locks) {
        HashMap<String, ApplicationInfo> packageInfoMap = new HashMap<>();

        // Lưu thông tin ứng dụng vào HashMap với key là packageName
        for (Lock lock : locks) {
            packageInfoMap.put(lock.getPackageApp(), null);
        }

        // Lấy thông tin của tất cả ứng dụng cài đặt trên thiết bị
        List<ApplicationInfo> installedApps = getContext().getPackageManager().getInstalledApplications(PackageManager.GET_META_DATA);

        // Kiểm tra và cập nhật thông tin ứng dụng
        for (ApplicationInfo appInfo : installedApps) {
            if (getContext().getPackageManager().getLaunchIntentForPackage(appInfo.packageName) != null) {
                // Nếu ứng dụng đã được cài đặt và có thể mở được
                String packageName = appInfo.packageName;
                if (packageInfoMap.containsKey(packageName)) {
                    // Cập nhật thông tin ứng dụng vào HashMap
                    packageInfoMap.put(packageName, appInfo);
                }
            }
        }

        // Cập nhật thông tin ứng dụng vào các đối tượng Lock
        for (Lock lock : locks) {
            String packageName = lock.getPackageApp();
            if (packageInfoMap.containsKey(packageName)) {
                // Lấy thông tin ứng dụng từ HashMap và cập nhật vào đối tượng Lock
                ApplicationInfo applicationInfo = packageInfoMap.get(packageName);
                lock.setApplicationInfo(applicationInfo);
            }
        }

        return locks;
    }


    // kiểm tra lần đầu tiên nếu chưa có database thì thêm vào
    // them tat ca list app vao db

    private ArrayList<Lock> getListAppLock() {
        ArrayList<Lock> lockArrayList = new ArrayList<>();
        lockArrayList.addAll(database.lockDAO().getListApps());
        return lockArrayList;
    }


}
