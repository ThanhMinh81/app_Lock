package com.example.applock;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.compose.ui.graphics.Canvas;
import androidx.room.Room;

import com.andrognito.patternlockview.PatternLockView;
import com.andrognito.patternlockview.listener.PatternLockViewListener;
import com.andrognito.patternlockview.utils.PatternLockUtils;
import com.example.applock.db.LockDatabase;
import com.example.applock.model.Lock;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class OverlayActivity extends AppCompatActivity {

    ImageView imgChangeMode;

    PatternLockView patternLockView;
    TableLayout tableLayout;
    TextView tvNameMode;

    ImageView imgIconApp;
    String packageApp;

    private SharedPreferences.Editor editor;

    LockDatabase database;

    boolean patternMode = true;

    TextView tvPassword;
    Lock lock;
    private String password;

    MaterialButton btnClear ;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // service gửi qua : icon app

        EdgeToEdge.enable(this);

        Intent intent = getIntent();

        packageApp = intent.getStringExtra("package");

        // Nhận mảng byte từ Intent
        byte[] byteArray = getIntent().getByteArrayExtra("picture");


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
          password = sharedPreferences.getString("password_pattern", "null");
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

                if (password.equals(PatternLockUtils.patternToString(patternLockView, pattern))) {

                    // tat che do lock

                    // Gửi broadcast với nội dung
                    Intent intent = new Intent("ACTION_LOCK_APP");
                    intent.putExtra("message", packageApp);
                    sendBroadcast(intent);

                    if (lock != null) {
                        lock.setStateLock(false);
                        database.lockDAO().updateLock(lock);
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
        Log.d("fsdfas",button.getText().toString());

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


}
