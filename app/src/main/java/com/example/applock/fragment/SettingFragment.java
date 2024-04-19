package com.example.applock.fragment;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;


import com.andrognito.patternlockview.PatternLockView;
import com.andrognito.patternlockview.listener.PatternLockViewListener;
import com.andrognito.patternlockview.utils.PatternLockUtils;
import com.example.applock.R;

import java.util.List;

public class SettingFragment extends Fragment {

    View view;
    LinearLayout layoutChangePattern;
    String changePass = "null";

    public SettingFragment() {
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_setting, container, false);

        layoutChangePattern = view.findViewById(R.id.layout_change_pattern);


        layoutChangePattern.setOnClickListener(v -> {
            showDialogChangePattern();
        });


        return view;
    }

    private void showDialogChangePattern() {


        PatternLockView mPatternLockView;
        TextView tvTitleDialog, tvCancel;

        ImageView img_state_pattern;


        AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext(), R.style.WrapContentDialog);
        LayoutInflater layoutInflater = LayoutInflater.from(this.getContext());
        final View dialogPattern = layoutInflater.inflate(R.layout.pattern_dialog_layout, null);


        tvTitleDialog = dialogPattern.findViewById(R.id.tvTitleDialog);
        tvCancel = dialogPattern.findViewById(R.id.tvCancel);
        mPatternLockView = dialogPattern.findViewById(R.id.pattern_lock_create);
        img_state_pattern = dialogPattern.findViewById(R.id.state_true_pattern);
        img_state_pattern.setVisibility(View.GONE);



        mPatternLockView.addPatternLockListener(new PatternLockViewListener() {

            @Override
            public void onStarted() {}

            @Override
            public void onProgress(List<PatternLockView.Dot> progressPattern) {}

            @Override
            public void onComplete(List<PatternLockView.Dot> pattern) {

//                mPatternLockView.clearPattern();
                if (changePass.equals("null")) {
                    tvTitleDialog.setText("Confirm pattern");
                    changePass = PatternLockUtils.patternToString(mPatternLockView, pattern);
                    mPatternLockView.clearPattern();
                } else {


                    if (changePass.equals(PatternLockUtils.patternToString(mPatternLockView, pattern))) {
                        tvTitleDialog.setText("Pattern Changed");
                        tvCancel.setText("OK");
                        changePass = "null";
                        mPatternLockView.setVisibility(View.GONE);
                        img_state_pattern.setVisibility(View.VISIBLE);


                        SharedPreferences createPassword = getContext().getSharedPreferences("PREFS", Context.MODE_PRIVATE);

                        SharedPreferences.Editor editor = createPassword.edit();

                        editor.putString("password", PatternLockUtils.patternToString(mPatternLockView, pattern));

                        editor.apply();

                    }else {

                        Handler handler = new Handler();

                        tvTitleDialog.setText("Pattern does not match");
                        mPatternLockView.setViewMode(PatternLockView.PatternViewMode.WRONG);
                        mPatternLockView.setWrongStateColor(getResources().getColor(R.color.color_wrong_password,null));

                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mPatternLockView.clearPattern();
                                tvTitleDialog.setText("Create pattern");
                                changePass = "null";
                            }
                        }, 400);


                    }
                }

            }

            @Override
            public void onCleared() {
                Log.d("0sdfsafsa","CLAERRRRR" );
            }
        });



        builder.setView(dialogPattern);
        AlertDialog dialog = builder.create();
        Window window = dialog.getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        params.gravity = Gravity.CENTER;
        params.width = WindowManager.LayoutParams.MATCH_PARENT;

        window.setAttributes(params);
        window.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);


        dialog.show();

        tvCancel.setOnClickListener(v -> {
            dialog.dismiss();
        });


    }


    private void showDialogChangePin(){

    }



}
