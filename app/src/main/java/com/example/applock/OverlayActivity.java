package com.example.applock;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import com.andrognito.patternlockview.PatternLockView;
import com.andrognito.patternlockview.listener.PatternLockViewListener;
import com.andrognito.patternlockview.utils.PatternLockUtils;
import com.example.applock.db.LockDatabase;
import com.example.applock.model.Lock;
import com.google.android.material.button.MaterialButton;

import java.util.Calendar;
import java.util.List;

public class OverlayActivity extends AppCompatActivity {

    ImageView imgChangeMode;

    PatternLockView patternLockView;
    TableLayout tableLayout;
    TextView tvNameMode;

    ImageView imgIconApp;
    String packageApp;
    LockDatabase database;

    boolean patternMode = true;

    TextView tvPassword;
    Lock lock;
    private String passwordPattern;
    private String passwordPin ;

    MaterialButton btnClear ;

    private String modeLock ;

    private Button btn1, btn2, btn3, btn4, btn5, btn6, btn7, btn8, btn9, btn0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // service gửi qua : icon app

        EdgeToEdge.enable(this);

        Intent intent = getIntent();

        packageApp = intent.getStringExtra("package");

        // Nhận mảng byte từ Intent
        byte[] byteArray = getIntent().getByteArrayExtra("picture");

        modeLock = getIntent().getStringExtra("mode_lock");


        database = Room.databaseBuilder(getApplicationContext(), LockDatabase.class, "locks_database")
                .allowMainThreadQueries()
                .build();

        lock = database.lockDAO().getLockByPackageName(packageApp);

        setContentView(R.layout.password_layout);

        initView();


        if (byteArray != null) {

            Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);

            imgIconApp.setImageBitmap(bitmap);

        }


        // send bitmap

        SharedPreferences sharedPreferences = getSharedPreferences("PREFS", Context.MODE_PRIVATE);
        passwordPattern = sharedPreferences.getString("password_pattern", "null");
        passwordPin = sharedPreferences.getString("password_pin","null");

        if(!passwordPin.equals("null")) {

            InputFilter[] filterArray = new InputFilter[1];
            filterArray[0] = new InputFilter.LengthFilter(passwordPin.length());
            tvPassword.setFilters(filterArray);

        }

        tvPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                Log.d("|0909fsdf",modeLock);

                if(s.length() == passwordPin.length())
                {
                    if(s.toString().equals(passwordPin))
                    {
                        Intent intent = new Intent("ACTION_LOCK_APP");
                        intent.putExtra("message", packageApp);
                        sendBroadcast(intent);

                        if (lock != null) {

                            if(modeLock.equals("immediately")){
                                lock.setStateLock(false);
                                database.lockDAO().updateLock(lock);
                            }else if(modeLock.equals("screen_off"))
                            {
                                lock.setStateLockScreenOff(false);
                                database.lockDAO().updateLock(lock);

                            }else{

                            }

                        }

                        finish();

                    }else {
                        tvNameMode.setText("Error");
                        tvPassword.setText("");
                    }
                }

            }
        });

        LayoutInflater inflater = LayoutInflater.from(this);

        handleClick();


    }

    private void initView() {

        tvPassword = findViewById(R.id.tvShowPassWord);
        imgChangeMode = findViewById(R.id.imgChangeModeComfirm);
        tableLayout = findViewById(R.id.tableLayout);
        patternLockView = findViewById(R.id.pattern_lock_view);
        tvNameMode = findViewById(R.id.tvNameMode);
        imgIconApp = findViewById(R.id.img_iconApp);
        btnClear = findViewById(R.id.btnClear);

    }

    private void handleClick() {

        if (patternMode) {

            tvPassword.setVisibility(View.GONE);
            tvNameMode.setText("Draw pattern");
            tableLayout.setVisibility(View.GONE);
            patternLockView.setVisibility(View.VISIBLE);

        } else {

            // xet them cho no lenght cua pass
            tvPassword.setVisibility(View.VISIBLE);
            tvNameMode.setText("Enter pin");
            patternLockView.setVisibility(View.GONE);
            tableLayout.setVisibility(View.VISIBLE);

        }

        imgChangeMode.setOnClickListener(v -> {

            patternMode = !patternMode;

            if (patternMode) {

                tvPassword.setVisibility(View.GONE);
                tvNameMode.setText("Draw pattern");
                tableLayout.setVisibility(View.GONE);
                patternLockView.setVisibility(View.VISIBLE);
                imgChangeMode.setImageDrawable(getResources().getDrawable(R.drawable.ic_pin_password, null));

            } else {
                tvPassword.setVisibility(View.VISIBLE);
                tvNameMode.setText("Enter pin");
                patternLockView.setVisibility(View.GONE);
                tableLayout.setVisibility(View.VISIBLE);
                imgChangeMode.setImageDrawable(getResources().getDrawable(R.drawable.ic_pattern_password, null));

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
                    // mở mật khẩu pattern thành công

                    // tat che do lock

                    // gửi suwjkienej về service
                    Intent intent = new Intent("ACTION_LOCK_APP");
                    intent.putExtra("message", packageApp);
                    sendBroadcast(intent);

                    if (lock != null) {

                        if(modeLock.equals("immediately")){
                            lock.setStateLock(false);
                            database.lockDAO().updateLock(lock);
                        }else if(modeLock.equals("screen_off"))
                        {
                            lock.setStateLockScreenOff(false);
                            database.lockDAO().updateLock(lock);

                        }else {

                            // trường hợp cuối cùng khóa bằng thời gian
//                                lock.setStateLockAfterMinute(false);


                            Calendar currentTime = Calendar.getInstance();


                            int hour = currentTime.get(Calendar.HOUR_OF_DAY);
                            int minute = currentTime.get(Calendar.MINUTE);
//
                            int minuteOpen = hour * 60 + minute;


                            int minuteClose = hour * 60 + minute + Integer.parseInt(modeLock) ;

                            lock.setTimeClose(String.valueOf(minuteClose));

                            lock.setTimeOpen(String.valueOf(minuteOpen));

                            lock.setStateLockScreenAfterMinute(false);

                            database.lockDAO().updateLock(lock);


                            Log.d("thoigiainnn",minuteOpen + " " + minuteClose);

                        }

                    }


                    finish();

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

        btnClear.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                tvPassword.setText("");

                return false;
            }
        });


    }

    public void onNumberClick(View view) {
        Button button = (Button) view;

        String currentText = tvPassword.getText().toString();
        String buttonText = button.getText().toString();
        String updatedText = currentText + buttonText;
        tvPassword.setText(updatedText);
    }


    public void onClearClick(View view) {
        String currentText = tvPassword.getText().toString();
        if (!currentText.isEmpty()) {
            String newText = currentText.substring(0, currentText.length() - 1);
            tvPassword.setText(newText);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }


    @Override
    protected void onStop() {

        finish();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        finish();
        super.onDestroy();
    }
}
