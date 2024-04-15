package com.example.applock.adapter;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.applock.Interface.ItemClickListenerLock;
import com.example.applock.R;
import com.example.applock.db.Lock;

import java.util.ArrayList;

public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.ViewHolder> {

    Context context;

    // hien thi tat ca app ra home
    ArrayList<ApplicationInfo> applicationInfos;

    // danh sach cac app dang bi khoa
    // chi lay name
    ArrayList<String> lockArrayList = new ArrayList<>();

    // list lock


    // list lock app database
    ArrayList<Lock> locks;

    ItemClickListenerLock itemClickListenerLock;

    public HomeAdapter(Context context, ArrayList<ApplicationInfo> applicationInfos, ItemClickListenerLock itemClickListenerLock, ArrayList<Lock> locks) {
        this.context = context;
        this.applicationInfos = applicationInfos;
        this.itemClickListenerLock = itemClickListenerLock;
        this.locks = locks;
    }

    @NonNull
    @Override
    public HomeAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view;
        view = inflater.inflate(R.layout.item_layout_app, parent, false);
        return new ViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull HomeAdapter.ViewHolder holder, int position) {

        Log.d("530453",lockArrayList.size() + " ");

        ApplicationInfo appInfo = applicationInfos.get(position);

        Lock lock = new Lock(0, appInfo.loadLabel(context.getPackageManager()).toString());

        // neu app thuoc danh sach khoa
        for (Lock lock1 : locks) {
            // id cho Lock
            if (lock1.getName().equals(appInfo.loadLabel(context.getPackageManager()).toString())) {
                lock.setIdApp(lock1.getIdApp());
            }
        }

        holder.tvNameApp.setText(appInfo.loadLabel(context.getPackageManager()));
        holder.imgIcon.setImageDrawable(appInfo.loadIcon(context.getPackageManager()));

        if (lockArrayList.contains(lock.getName())) {
            holder.imgClock.setImageDrawable(context.getResources().getDrawable(R.drawable._669338_lock_ic_icon, null));
        }

        holder.imgClock.setOnClickListener(v -> {

            if (lockArrayList.contains(lock.getName())) {

                Log.d("543252",lock.getName());
                // xoa khoi list lock

                //                for (String s : lockArrayList) {
//                    if (s.equals(lock.getName())) {
//                        lockArrayList.remove(s);
//                    }
//                }


                try{
                    lockArrayList.remove(lock.getName());
                    itemClickListenerLock.clickItemLock(lock, true);
//                    holder.imgClock.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_lock, null));
                    notifyDataSetChanged();
                }catch (Exception  e)
                {
                    Log.d("5342545dsa",e.toString());
                }


            } else {
                // them vao list lock
//                holder.imgClock.setImageDrawable(context.getResources().getDrawable(R.drawable._669338_lock_ic_icon, null));
                lockArrayList.add(lock.getName());
                itemClickListenerLock.clickItemLock(lock, false);
                notifyDataSetChanged();
            }

        });
    }

    @Override
    public int getItemCount() {
        return applicationInfos.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgIcon, imgSetting, imgClock;
        TextView tvNameApp;

        public ViewHolder(@NonNull View itemView) {

            super(itemView);

            imgIcon = itemView.findViewById(R.id.img_iconApp);
            imgSetting = itemView.findViewById(R.id.imgSetting);
            imgClock = itemView.findViewById(R.id.imgClock);
            tvNameApp = itemView.findViewById(R.id.tv_nameApp);
        }

    }

    public void setArrayLock(ArrayList<String> locks) {
        lockArrayList.clear();
        lockArrayList.addAll(locks);
        this.notifyDataSetChanged();

    }


}
