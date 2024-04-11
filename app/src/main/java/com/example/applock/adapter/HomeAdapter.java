package com.example.applock.adapter;

import android.content.Context;
import android.content.pm.ApplicationInfo;
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

    Context context ;
    ArrayList<ApplicationInfo> applicationInfos;

    ItemClickListenerLock itemClickListenerLock ;


    public HomeAdapter(Context context ,ArrayList<ApplicationInfo> applicationInfos , ItemClickListenerLock itemClickListenerLock ) {
        this.context = context ;
        this.applicationInfos = applicationInfos;
        this.itemClickListenerLock = itemClickListenerLock ;
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

        ApplicationInfo appInfo = applicationInfos.get(position);
        Lock lock = new Lock(0,appInfo.loadLabel(context.getPackageManager()).toString());


            holder.tvNameApp.setText(appInfo.loadLabel(context.getPackageManager()));
            holder.imgIcon.setImageDrawable(appInfo.loadIcon(context.getPackageManager()));

            holder.imgClock.setOnClickListener(v -> {
                 itemClickListenerLock.clickItemLock(lock);
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
}
