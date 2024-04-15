package com.example.applock.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;


import com.andrognito.patternlockview.PatternLockView;
import com.andrognito.patternlockview.listener.PatternLockViewListener;
import com.andrognito.patternlockview.utils.PatternLockUtils;
import com.example.applock.R;

import java.util.List;

public class SettingFragment extends Fragment {

    View view;

    PatternLockView mPatternLockView;

    public SettingFragment() {
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_setting, container, false);

        mPatternLockView = (PatternLockView) view.findViewById(R.id.pattern_lock_view);

        // cai nay xet cho no khong thay duoc hinh ve nua
//        mPatternLockView.setInStealthMode(true);

        mPatternLockView.addPatternLockListener(new PatternLockViewListener() {
            @Override
            public void onStarted() {
            }

            @Override
            public void onProgress(List<PatternLockView.Dot> progressPattern) {
            }

            @Override
            public void onComplete(List<PatternLockView.Dot> pattern) {

                // Shared Preferences to save state
                try {
                    SharedPreferences sharedPreferences = getContext().getSharedPreferences("PREFS", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("password", PatternLockUtils.patternToString(mPatternLockView, pattern));
                    editor.apply();
                } catch (Exception e) {
                    Log.d("390427479", e.toString());
                }

            }

            @Override
            public void onCleared() {

            }
        });


        return view;
    }


}
