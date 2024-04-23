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
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;


import com.andrognito.patternlockview.PatternLockView;
import com.andrognito.patternlockview.listener.PatternLockViewListener;
import com.andrognito.patternlockview.utils.PatternLockUtils;
import com.example.applock.R;
import com.google.android.material.button.MaterialButton;

import org.w3c.dom.Text;

import java.util.List;

public class SettingFragment extends Fragment {

    View view;
    LinearLayout layoutChangePattern;
    String changePattern = "";

    RelativeLayout layoutChangePin;
    private String changePin = "";
    private SharedPreferences createPassword;
    private TextView tvPin;

    private TextView tvTitleDialog, tvCancel ;
    private MaterialButton btnCheck, btnClear;

   private Button btn1 , btn2 , btn3 , btn4 , btn5, btn6 ,btn7 , btn8 ,btn9 ,btn0 ;

     private  View dialogPattern ;


    ImageView imgChangeSuccess;


    public SettingFragment() {
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_setting, container, false);

        layoutChangePattern = view.findViewById(R.id.layout_change_pattern);

        layoutChangePin = view.findViewById(R.id.layout_change_pin);


        layoutChangePattern.setOnClickListener(v -> {
            showDialogChangePattern();
        });

        layoutChangePin.setOnClickListener(v -> {
            showDialogChangePin();
        });


        return view;
    }




    private void showDialogChangePin() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext(), R.style.WrapContentDialog);
        LayoutInflater layoutInflater = LayoutInflater.from(this.getContext());
          dialogPattern = layoutInflater.inflate(R.layout.pin_dialog_layout, null);

         initViewDialog();


        imgChangeSuccess = dialogPattern.findViewById(R.id.state_true_pattern);

        createPassword = getContext().getSharedPreferences("PREFS", Context.MODE_PRIVATE);

        imgChangeSuccess.setVisibility(View.GONE);

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


        eventClickView();








    }

    private void eventClickView() {

        btnCheck.setOnClickListener(v -> {

            changePin = tvPin.getText().toString();

            if(changePin.length() < 2)
            {
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        tvTitleDialog.setText("Pin Length is less then 2 , Enter Pin Again");
                    }
                }, 200);

                tvTitleDialog.setText("Create Pin");


            }else {
                String pinConfirm = tvPin.getText().toString();

                tvTitleDialog.setText("Confirm pin");

                if (pinConfirm.equals(changePin)) {

                    tvTitleDialog.setText("Pin changed");
                    imgChangeSuccess.setVisibility(View.VISIBLE);
                    tvCancel.setText("OK");

                    SharedPreferences.Editor editor = createPassword.edit();

                    editor.putString("password_pin", pinConfirm);

                    editor.apply();

                    changePin = "";

                } else {

                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            tvTitleDialog.setText("Pin does not match , Enter Pin Again");
                        }
                    }, 200);
                    changePin = "";
                    tvTitleDialog.setText("Create pin");

                }


            }


//            if (changePin.isEmpty()) {
//                if (tvPin.length() < 2) {
//                    Handler handler = new Handler();
//                    handler.postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//
//                            tvTitleDialog.setText("Pin Length is less then 2 , Enter Pin Again");
//
//                        }
//
//                    }, 200);
//
//                    tvTitleDialog.setText("Create Pin");
//
//                } else {
//                    changePin = tvPin.getText().toString();
//                    tvTitleDialog.setText("Confirm pin");
//                }
//            } else {
//                String pinConfirm = tvPin.getText().toString();
//
//                if (pinConfirm.equals(changePin)) {
//
//                    tvTitleDialog.setText("Pin changed");
//                    imgChangeSuccess.setVisibility(View.VISIBLE);
//                    tvCancel.setText("OK");
//
//                    SharedPreferences.Editor editor = createPassword.edit();
//
//                    editor.putString("password_pin", pinConfirm);
//
//                    editor.apply();
//
//                    changePin = "";
//
//                } else {
//
//                    Handler handler = new Handler();
//                    handler.postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            tvTitleDialog.setText("Pin does not match , Enter Pin Again");
//                        }
//                    }, 200);
//                    changePin = "";
//                    tvTitleDialog.setText("Create pin");
//
//                }
//
//            }

        });

        btn1.setOnClickListener(v -> {onClickChangePin(v);});
        btn2.setOnClickListener(v -> {onClickChangePin(v);});
        btn3.setOnClickListener(v -> onClickChangePin(v));
        btn4.setOnClickListener(v -> {onClickChangePin(v);});
        btn5.setOnClickListener(v -> {onClickChangePin(v);});
        btn6.setOnClickListener(v -> onClickChangePin(v));
        btn7.setOnClickListener(v -> {onClickChangePin(v);});
        btn8.setOnClickListener(v -> {onClickChangePin(v);});
        btn9.setOnClickListener(v -> onClickChangePin(v));
        btn0.setOnClickListener(v -> onClickChangePin(v));
        btnClear.setOnClickListener(v -> {onClearPinPassword(v);});

        btnClear.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                tvPin.setText("");
                return false;
            }
        });

    }

    private void initViewDialog() {

        tvTitleDialog = dialogPattern.findViewById(R.id.tvTitleDialog);
        tvCancel = dialogPattern.findViewById(R.id.tvCancel);
        tvPin = dialogPattern.findViewById(R.id.tvPassword);
        btnCheck = dialogPattern.findViewById(R.id.btnCheck);
        btnClear = dialogPattern.findViewById(R.id.btnClear);
        btn1 = dialogPattern.findViewById(R.id.btn1);
        btn2 = dialogPattern.findViewById(R.id.btn2);
        btn3 = dialogPattern.findViewById(R.id.btn3);
        btn4 = dialogPattern.findViewById(R.id.btn4);
        btn5 = dialogPattern.findViewById(R.id.btn5);
        btn6 = dialogPattern.findViewById(R.id.btn6);
        btn7 = dialogPattern.findViewById(R.id.btn7);
        btn8 = dialogPattern.findViewById(R.id.btn8);
        btn9 = dialogPattern.findViewById(R.id.btn9);
        btn0 = dialogPattern.findViewById(R.id.btn0);
    }


    public  void onClickChangePin(View view) {
        Button button = (Button) view;
//        Log.d("fsdfas",button.getText().toString());
        String currentText = tvPin.getText().toString();
        String buttonText = button.getText().toString();
        String updatedText = currentText + buttonText;
        tvPin.setText(updatedText);

    }

    public void onClearPinPassword(View view) {
        String currentText = tvPin.getText().toString();
        if (!currentText.isEmpty()) {
            String newText = currentText.substring(0, currentText.length() - 1);
            tvPin.setText(newText);
        }
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
            public void onStarted() {
            }

            @Override
            public void onProgress(List<PatternLockView.Dot> progressPattern) {
            }

            @Override
            public void onComplete(List<PatternLockView.Dot> pattern) {

//                mPatternLockView.clearPattern();
                if (!changePattern.isEmpty()) {
                    tvTitleDialog.setText("Confirm pattern");
                    changePattern = PatternLockUtils.patternToString(mPatternLockView, pattern);
                    mPatternLockView.clearPattern();
                } else {


                    if (changePattern.equals(PatternLockUtils.patternToString(mPatternLockView, pattern))) {
                        tvTitleDialog.setText("Pattern Changed");
                        tvCancel.setText("OK");
                        changePattern = "";
                        mPatternLockView.setVisibility(View.GONE);
                        img_state_pattern.setVisibility(View.VISIBLE);


                        SharedPreferences.Editor editor = createPassword.edit();

                        editor.putString("password_pattern", PatternLockUtils.patternToString(mPatternLockView, pattern));

                        editor.apply();

                    } else {

                        Handler handler = new Handler();

                        tvTitleDialog.setText("Pattern does not match");
                        mPatternLockView.setViewMode(PatternLockView.PatternViewMode.WRONG);
                        mPatternLockView.setWrongStateColor(getResources().getColor(R.color.color_wrong_password, null));

                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mPatternLockView.clearPattern();
                                tvTitleDialog.setText("Create pattern");
                                changePattern = "";
                            }
                        }, 400);
                    }
                }

            }

            @Override
            public void onCleared() {
                Log.d("0sdfsafsa", "CLAERRRRR");
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


}
