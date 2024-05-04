package com.example.applock;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import com.andrognito.patternlockview.PatternLockView;
import com.andrognito.patternlockview.listener.PatternLockViewListener;
import com.andrognito.patternlockview.utils.PatternLockUtils;
import com.example.applock.Interface.UnlockRecentMenu;

import java.util.Calendar;
import java.util.List;

public class ScreenLockRecent {

    public WindowManager windowManager;
    public View floatingView;

    public Context context;

    private WindowManager.LayoutParams params;
    private Button btn1, btn2, btn3, btn4, btn5, btn6, btn7, btn8, btn9, btn0, btnClear;

    private TextView tvPin, tvNameMode;

    private TableLayout tableLayout;
    private PatternLockView patternLockView;

    private ImageView imgChangeMode;

    private boolean patternMode = true;

    private String passwordPattern;

    String passwordPin;

    UnlockRecentMenu unlockRecentMenu;

    boolean isViewAdded = false;


    @RequiresApi(api = Build.VERSION_CODES.O)
    public ScreenLockRecent(Context context, UnlockRecentMenu unlockRecentMenu) {
        this.context = context;
        this.unlockRecentMenu = unlockRecentMenu;
//            Log.d("5309fsfsdaf","fasfa");
        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

        isViewAdded = false;

        LayoutInflater layoutInflater = LayoutInflater.from(context);


        floatingView = layoutInflater.inflate(R.layout.password_layout_recent, null);


        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ?
                        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY :
                        WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                        | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                        | WindowManager.LayoutParams.FLAG_FULLSCREEN
                        |WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                PixelFormat.TRANSLUCENT);


        params.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_FULLSCREEN;



        params.gravity = Gravity.CENTER;




        initView();
        handleEventUnlock();


    }

    private void handleEventUnlock() {

        tvPin.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                if (s.length() == passwordPin.length()) {
                    if (s.toString().equals(passwordPin)) {

                        confirmSuccess();

                    } else {
                        tvNameMode.setText("Error");
                        tvPin.setText("");
                    }
                }

            }
        });


        patternLockView.addPatternLockListener(new PatternLockViewListener() {
            @Override
            public void onStarted() {
            }

            @Override
            public void onProgress(List<PatternLockView.Dot> progressPattern) {
            }

            @Override
            public void onComplete(List<PatternLockView.Dot> pattern) {

                if (passwordPattern.equals(PatternLockUtils.patternToString(patternLockView, pattern))) {

                    confirmSuccess();

                } else {

                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {

                            tvNameMode.setText("Error");
                            patternLockView.setViewMode(PatternLockView.PatternViewMode.WRONG);

                        }
                    }, 200);

                    patternLockView.clearPattern();
                }

            }

            @Override
            public void onCleared() {
            }
        });

    }

    private void confirmSuccess() {

        unlockRecentMenu.unlockSs(false);

    }

    private void initView() {

        SharedPreferences sharedPreferences = context.getSharedPreferences("PREFS", Context.MODE_PRIVATE);
        passwordPattern = sharedPreferences.getString("password_pattern", "null");


        passwordPin = sharedPreferences.getString("password_pin", "null");

        tableLayout = floatingView.findViewById(R.id.tableLayout);
        tableLayout.setVisibility(View.GONE);

        patternLockView = floatingView.findViewById(R.id.pattern_lock_view);
        imgChangeMode = floatingView.findViewById(R.id.imgChangeModeComfirm);

        btnClear = floatingView.findViewById(R.id.btnClear);
        tvNameMode = floatingView.findViewById(R.id.tvNameMode);
        btn1 = floatingView.findViewById(R.id.btn1);
        btn2 = floatingView.findViewById(R.id.btn2);
        btn3 = floatingView.findViewById(R.id.btn3);
        btn4 = floatingView.findViewById(R.id.btn4);
        btn5 = floatingView.findViewById(R.id.btn5);
        btn6 = floatingView.findViewById(R.id.btn6);
        btn7 = floatingView.findViewById(R.id.btn7);
        btn8 = floatingView.findViewById(R.id.btn8);
        btn9 = floatingView.findViewById(R.id.btn9);
        btn0 = floatingView.findViewById(R.id.btn0);
        tvPin = floatingView.findViewById(R.id.tvShowPassWord);

        btn1.setOnClickListener(this::onClickChangePin);
        btn2.setOnClickListener(this::onClickChangePin);
        btn3.setOnClickListener(this::onClickChangePin);
        btn4.setOnClickListener(this::onClickChangePin);
        btn5.setOnClickListener(this::onClickChangePin);
        btn6.setOnClickListener(this::onClickChangePin);
        btn7.setOnClickListener(this::onClickChangePin);
        btn8.setOnClickListener(this::onClickChangePin);
        btn9.setOnClickListener(this::onClickChangePin);
        btn0.setOnClickListener(this::onClickChangePin);
        btnClear.setOnClickListener(this::onClearPinPassword);

        if (patternMode) {

            tvPin.setVisibility(View.GONE);
            tvNameMode.setText("Draw pattern");
            tableLayout.setVisibility(View.GONE);
            patternLockView.setVisibility(View.VISIBLE);

        } else {

            // xet them cho no lenght cua pass
            tvPin.setVisibility(View.VISIBLE);
            tvNameMode.setText("Enter pin");
            patternLockView.setVisibility(View.GONE);
            tableLayout.setVisibility(View.VISIBLE);

        }

        imgChangeMode.setOnClickListener(v -> {

            patternMode = !patternMode;

            if (patternMode) {

                tvPin.setVisibility(View.GONE);
                tvNameMode.setText("Draw pattern");
                tableLayout.setVisibility(View.GONE);
                patternLockView.setVisibility(View.VISIBLE);
                imgChangeMode.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_pin_password, null));

            } else {

                tvPin.setVisibility(View.VISIBLE);
                tvNameMode.setText("Enter pin");
                patternLockView.setVisibility(View.GONE);
                tableLayout.setVisibility(View.VISIBLE);
                imgChangeMode.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_pattern_password, null));

            }

        });


    }

    public void showScreenPassword() {

        Log.d("5023fasfasd", "dsafafa");

        // Thêm floatingView vào WindowManager
        if (!isViewAdded) {
            windowManager.addView(floatingView, params);
            btnClear = floatingView.findViewById(R.id.btnClear);
            isViewAdded = true;
        }


        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("935793845", "|fsopdfs");
            }
        });


    }

    private void onClickChangePin(View view) {
        Button button = (Button) view;

        String currentText = tvPin.getText().toString();
        String buttonText = button.getText().toString();
        String updatedText = currentText + buttonText;
        tvPin.setText(updatedText);

    }


    private void onClearPinPassword(View view) {
        String currentText = tvPin.getText().toString();
        if (!currentText.isEmpty()) {
            String newText = currentText.substring(0, currentText.length() - 1);
            tvPin.setText(newText);
        }
    }


    // hide screen overlay

    public void disableOverlay() {


        windowManager.removeView(floatingView);
        floatingView = null;
        isViewAdded = false;


    }

    // check xem windownmanager overlay con hien thi hay ko
    // tra ve true neu no con

    public boolean isViewAttachedToWindow(View view) {
        if (view.getWindowToken() != null) {
            return true;
        } else {
            return false;
        }
    }


}
