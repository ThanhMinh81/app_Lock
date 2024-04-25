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
import com.example.applock.db.LockDatabase;
import com.example.applock.model.Lock;

import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;

public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.ViewHolder> {

    private Context context;
    private ArrayList<Lock> locks;

    private LockDatabase lockDatabase;

    public HomeAdapter(Context context, ArrayList<Lock> locks, LockDatabase lockDatabase) {
        this.context = context;
        this.locks = locks;
        this.lockDatabase = lockDatabase;
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

        Lock lock = locks.get(position);
        ApplicationInfo applicationInfo = lock.getApplicationInfo();

        if (applicationInfo != null)
        {
            holder.tvNameApp.setText(applicationInfo.loadLabel(context.getPackageManager()));
            holder.imgIcon.setImageDrawable(applicationInfo.loadIcon(context.getPackageManager()));
        }

        if (lock.isStateLock()) {
            holder.imgClock.setImageDrawable(context.getResources().getDrawable(R.drawable._669338_lock_ic_icon, null));
        }else {
            holder.imgClock.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_lock_circle, null));
        }

        holder.imgClock.setOnClickListener(v -> {
            Log.d("345325fsa",lock.isStateLock() + " ");
            if (lock.isStateLock()) {
                // xóa khỏi mode lock
                lock.setStateLock(false);
                lock.setStateLockScreenOff(false);

                lock.setStateLockScreenAfterMinute(false);

                int updateState = lockDatabase.lockDAO().updateLock(lock);

                if (updateState != 0) {
                    locks.set(position, lock);
                    notifyItemChanged(position);
                }
            } else {
                // them vao lock
                lock.setStateLock(true);
                lock.setStateLockScreenOff(true);
                lock.setStateLockScreenAfterMinute(true);

                int updateState = lockDatabase.lockDAO().updateLock(lock);

                if (updateState != 0) {
                    locks.set(position, lock);
                    notifyItemChanged(position);
                }

                Log.d("532523fasdf",updateState  + " ");

            }
        });
    }

    @Override
    public int getItemCount() {
        return locks.size();
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


}
