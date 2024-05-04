package com.example.applock.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TableLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.hardware.fingerprint.FingerprintManagerCompat;
import androidx.fragment.app.Fragment;


import com.andrognito.patternlockview.PatternLockView;
import com.andrognito.patternlockview.listener.PatternLockViewListener;
import com.andrognito.patternlockview.utils.PatternLockUtils;
import com.example.applock.R;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class SettingFragment extends Fragment {
    View view;
    LinearLayout layoutChangePattern;
    String changePattern = "";

    RelativeLayout layoutChangePin;
    String changePin = "";

    TextView tvPin;

    TextView tvTitleDialog, tvCancel, tvCurrentModeLock;
    MaterialButton btnCheck, btnClear;
    ImageView selectFingerUnlock;

    Button btn1, btn2, btn3, btn4, btn5, btn6, btn7, btn8, btn9, btn0;
    View dialogPattern;
    int count = 0;
    ImageView imgChangeSuccess;
    TableLayout tableLayoutChangePin;
    LinearLayout layoutRelockApp;

    // hiển thị thời gian khóa app
    String minuteLock = "";

    // hien thi text mode hien tai
    String lockModeCurrent = "";
    CheckBox cbFingerPrint;
    SharedPreferences modeLockSpf;
    RelativeLayout layoutFingerPrint;
    ImageView imgSettingFinger;
    SharedPreferences sharedPreferencesPassword;
    RelativeLayout layoutLockRecent;
    CheckBox cbLockRecentMenu;

    public SettingFragment() {
    }

    @SuppressLint({"UseCompatLoadingForDrawables", "CutPasteId"})
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_setting, container, false);

        layoutChangePattern = view.findViewById(R.id.layout_change_pattern);

        sharedPreferencesPassword = getContext().getSharedPreferences("PREFS", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferencesPassword.edit();


        initView();


        layoutChangePattern.setOnClickListener(v -> {
            showDialogChangePattern();
        });

        layoutChangePin.setOnClickListener(v -> {
            showDialogChangePin();
        });
        layoutRelockApp.setOnClickListener(view -> {
            showDialogRelockApp();
        });

        cbFingerPrint.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {

                SharedPreferences.Editor editorFinger = modeLockSpf.edit();

                editorFinger.putString("finger_print", "yes");
                // add finger unlock
                editor.apply();
            } else {
                SharedPreferences.Editor editorFinger = modeLockSpf.edit();
                editorFinger.putString("finger_print", "no");
                editorFinger.putString("unlock_only_finger", "no");

                editor.apply();
            }
        });


        handleEventClick();

        return view;
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void handleEventClick() {

        layoutFingerPrint.setOnClickListener(v -> {

            boolean checkFinger = isFingerprintRegistered(getContext());

            if (!checkFinger) {
                // show dialog register finger
                showDialogEnableFinger();

            } else {

                boolean check = cbFingerPrint.isChecked();

                if (check) {
                    cbFingerPrint.setChecked(false);
                } else {
                    cbFingerPrint.setChecked(true);
                }

                if (cbFingerPrint.isChecked()) {
                    selectFingerUnlock.setImageDrawable(getResources().getDrawable(R.drawable.ic_setting_selectfinger, null));
                } else {
                    selectFingerUnlock.setImageDrawable(getResources().getDrawable(R.drawable.ic_setting_finger, null));
                }

            }
        });

        cbFingerPrint.setOnClickListener(v -> {
            if (cbFingerPrint.isChecked()) {
                selectFingerUnlock.setImageDrawable(getResources().getDrawable(R.drawable.ic_setting_selectfinger, null));

            } else {
                selectFingerUnlock.setImageDrawable(getResources().getDrawable(R.drawable.ic_setting_finger, null));

            }
        });

        selectFingerUnlock.setOnClickListener(v -> {
            if (cbFingerPrint.isChecked()) {
                showDialogUnlockOnlyFinger();
            }
        });


        cbLockRecentMenu.setOnCheckedChangeListener((buttonView, isChecked) -> {

            SharedPreferences.Editor editorRecent = modeLockSpf.edit();

            if (isChecked) {
                editorRecent.putString("lock_recent_menu", "yes");
                editorRecent.apply();
            } else {
                editorRecent.putString("lock_recent_menu", "no");
                editorRecent.apply();
            }
        });


        layoutLockRecent.setOnClickListener(v -> {

            SharedPreferences.Editor editorFinger = modeLockSpf.edit();

            boolean check = cbLockRecentMenu.isChecked();

            if (cbLockRecentMenu.isChecked()) {
                editorFinger.putString("lock_recent_menu", "no");
                cbLockRecentMenu.setChecked(false);

            } else {

                editorFinger.putString("lock_recent_menu", "yes");
                cbLockRecentMenu.setChecked(true);

            }


        });

    }

    @SuppressLint("CutPasteId")
    private void initView() {

        layoutChangePin = view.findViewById(R.id.layout_change_pin);
        layoutRelockApp = view.findViewById(R.id.layout_relockApp);
        tvCurrentModeLock = view.findViewById(R.id.tvCurrentModeLock);
        cbFingerPrint = view.findViewById(R.id.cbUnlockFingerPassword);
        layoutFingerPrint = view.findViewById(R.id.layoutUsegingerPrintUnlock);
        selectFingerUnlock = view.findViewById(R.id.selectFingerUnlock);
        imgSettingFinger = view.findViewById(R.id.selectFingerUnlock);
        layoutLockRecent = view.findViewById(R.id.layout_lock_recentmenu);
        cbLockRecentMenu = view.findViewById(R.id.cb_lock_recent);

        modeLockSpf = getContext().getSharedPreferences("LockMode", Context.MODE_PRIVATE);
        lockModeCurrent = modeLockSpf.getString("lock_mode", "immediately");

        if (modeLockSpf.getString("lock_recent_menu", "no").equals("yes")) {
            cbLockRecentMenu.setChecked(true);
        }


        if (lockModeCurrent.equals("immediately")) {
            tvCurrentModeLock.setText("Immediately");
        } else if (lockModeCurrent.equals("screen_off")) {
            tvCurrentModeLock.setText("After screen off");
        } else {
            tvCurrentModeLock.setText(lockModeCurrent + " minute");
            minuteLock = lockModeCurrent;
        }

        if (modeLockSpf.getString("finger_print", "no").equals("yes")) {
            cbFingerPrint.setChecked(true);
            selectFingerUnlock.setImageDrawable(getResources().getDrawable(R.drawable.ic_setting_selectfinger, null));

        } else {
            cbFingerPrint.setChecked(true);
            selectFingerUnlock.setImageDrawable(getResources().getDrawable(R.drawable.ic_setting_selectfinger, null));

        }

    }

    private void showDialogUnlockOnlyFinger() {


        SharedPreferences.Editor editorFinger = modeLockSpf.edit();


        MaterialButton btnCancel, btnOk;
        CheckBox cbFingerUnlock;
        LinearLayout layoutOnlyFinger;

        AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext(), R.style.MyDialogTheme);
        LayoutInflater layoutInflater = LayoutInflater.from(this.getContext());
        dialogPattern = layoutInflater.inflate(R.layout.dialog_fingerprint_unlock, null);

        builder.setView(dialogPattern);
        AlertDialog dialog = builder.create();
        Window window = dialog.getWindow();
        WindowManager.LayoutParams params = window.getAttributes();

        btnCancel = dialogPattern.findViewById(R.id.btnCancelFinger);
        btnOk = dialogPattern.findViewById(R.id.btnOkFinger);
        cbFingerUnlock = dialogPattern.findViewById(R.id.cbFingerUnlock);

        layoutOnlyFinger = dialogPattern.findViewById(R.id.layoutOnlyFinger);

        dialog.setCanceledOnTouchOutside(true);
        dialog.setCancelable(true);


        params.gravity = Gravity.CENTER;

        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);

        if (modeLockSpf.getString("unlock_only_finger", "no").equals("yes")) {
            cbFingerUnlock.setChecked(true);
        }

        dialog.show();

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnOk.setOnClickListener(v -> {
            if (cbFingerUnlock.isChecked()) {

                editorFinger.putString("unlock_only_finger", "yes");
                editorFinger.apply();
                dialog.dismiss();

            } else {

                editorFinger.putString("unlock_only_finger", "no");
                editorFinger.apply();
                dialog.dismiss();

            }
        });


    }

    private void showDialogEnableFinger() {

        MaterialButton btnSetting;

        AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext(), R.style.MyDialogTheme);
        LayoutInflater layoutInflater = LayoutInflater.from(this.getContext());
        dialogPattern = layoutInflater.inflate(R.layout.dialog_register_finger, null);

        builder.setView(dialogPattern);
        AlertDialog dialog = builder.create();
        Window window = dialog.getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
        window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

        btnSetting = dialogPattern.findViewById(R.id.btnSettingFinger);

        params.gravity = Gravity.CENTER;

        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);

        dialog.show();

        btnSetting.setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_SECURITY_SETTINGS);
            getContext().startActivity(intent);
        });


    }

    private void showDialogRelockApp() {
        RadioButton rbImedia, rbScreenOff, rbAfterMinute;
        MaterialButton tvCancel, tvOk;
        SeekBar seekBarMinute;
        TextView tvMinute;


        SharedPreferences modeLock = getContext().getSharedPreferences("LockMode", Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = modeLock.edit();

        // mac dinh la immediately
        lockModeCurrent = modeLock.getString("lock_mode", "immediately");

        if (!lockModeCurrent.equals("immediately") && !lockModeCurrent.equals("screen_off")) {
            minuteLock = lockModeCurrent;
        } else {
            minuteLock = String.valueOf(1);
        }


        AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext(), R.style.MyDialogTheme);
        LayoutInflater layoutInflater = LayoutInflater.from(this.getContext());
        dialogPattern = layoutInflater.inflate(R.layout.dialog_relock_layout, null);
        rbImedia = dialogPattern.findViewById(R.id.rb_immedia);
        rbScreenOff = dialogPattern.findViewById(R.id.rbScreenOff);
        rbAfterMinute = dialogPattern.findViewById(R.id.rbAfterMinute);
        seekBarMinute = dialogPattern.findViewById(R.id.seekBarMinute);
        tvMinute = dialogPattern.findViewById(R.id.idMinute);
        tvCancel = dialogPattern.findViewById(R.id.tvCancel);
        tvOk = dialogPattern.findViewById(R.id.tvOk);

        builder.setView(dialogPattern);
        AlertDialog dialog = builder.create();
        Window window = dialog.getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        params.gravity = Gravity.CENTER;


        window.setAttributes(params);
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);

        dialog.show();

        tvCancel.setOnClickListener(v -> {
            dialog.dismiss();
        });

        if (lockModeCurrent.equals("immediately")) {
            rbImedia.setChecked(true);
            rbScreenOff.setChecked(false);
            rbAfterMinute.setChecked(false);
        } else if (lockModeCurrent.equals("screen_off")) {
            rbImedia.setChecked(false);
            rbScreenOff.setChecked(true);
            rbAfterMinute.setChecked(false);
        } else {
            seekBarMinute.setProgress(Integer.valueOf(lockModeCurrent));
            tvMinute.setText(lockModeCurrent);
            rbImedia.setChecked(false);
            rbScreenOff.setChecked(false);
            rbAfterMinute.setChecked(true);
        }

        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RadioButton clickedRadioButton = (RadioButton) v;

                Boolean checked = clickedRadioButton.isChecked();

                if (checked) {
                    if (clickedRadioButton.getId() == R.id.rb_immedia) {

                        rbScreenOff.setChecked(false);
                        rbAfterMinute.setChecked(false);

                    } else if (clickedRadioButton.getId() == R.id.rbAfterMinute) {

                        rbImedia.setChecked(false);
                        rbScreenOff.setChecked(false);

                    } else if (clickedRadioButton.getId() == R.id.rbScreenOff) {
                        rbImedia.setChecked(false);
                        rbAfterMinute.setChecked(false);
                    }

                }

            }
        };


        // onClickButton

        rbImedia.setOnClickListener(onClickListener);
        rbScreenOff.setOnClickListener(onClickListener);
        rbAfterMinute.setOnClickListener(onClickListener);

        seekBarMinute.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {


                rbImedia.setChecked(false);
                rbScreenOff.setChecked(false);
                rbAfterMinute.setChecked(true);

                tvMinute.setText(String.valueOf(progress));
                minuteLock = String.valueOf(progress);

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        tvOk.setOnClickListener(v -> {

            if (rbImedia.isChecked()) {

                tvCurrentModeLock.setText("Immediately");

                editor.putString("lock_mode", "immediately");

                editor.apply();
            } else if (rbScreenOff.isChecked()) {
                tvCurrentModeLock.setText("After screen off");
                editor.putString("lock_mode", "screen_off");

                editor.apply();
            } else if (rbAfterMinute.isChecked()) {

                editor.putString("lock_mode", minuteLock);
                tvCurrentModeLock.setText(String.valueOf(minuteLock) + " minute");

                editor.apply();
                minuteLock = "";

            }

            dialog.dismiss();

        });


    }


    private void showDialogChangePin() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext(), R.style.MyDialogTheme);
        LayoutInflater layoutInflater = LayoutInflater.from(this.getContext());
        dialogPattern = layoutInflater.inflate(R.layout.dialog_pin_password, null);

        initViewDialog();

        imgChangeSuccess = dialogPattern.findViewById(R.id.state_true_pattern);
        tableLayoutChangePin = dialogPattern.findViewById(R.id.tableLayout);


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

            if (tvPin.getText().toString().length() < 2) {

                tvTitleDialog.setText("Pin Length is less then 2 , Enter Pin Again");

                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        tvTitleDialog.setText("Create Pin");
                        tvPin.setText("");

                    }
                }, 1000);


            } else {
                if (count == 0) {
                    // nếu lớn hơn 2 thì cho bến string tạm đó bằng pass
                    changePin = tvPin.getText().toString();
                    tvPin.setText("");
                    tvTitleDialog.setText("Confirm pin");
                    count++;
                    return;
                }
                if (count == 1) {
                    if (changePin.equals(tvPin.getText().toString())) {
                        // thay dodoir mat khau thanh cong

                        tvTitleDialog.setText("Pin changed");
                        imgChangeSuccess.setVisibility(View.VISIBLE);
                        tableLayoutChangePin.setVisibility(View.GONE);
                        tvCancel.setText("OK");


                        SharedPreferences.Editor editor = sharedPreferencesPassword.edit();

                        editor.putString("password_pin", tvPin.getText().toString());

                        editor.apply();

                        changePin = "";
                        tvPin.setText("");
                        count = 0;

                    } else {
                        // thay doi mat khau that bai

                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                tvTitleDialog.setText("Create pin");
                            }
                        }, 1000);
                        changePin = "";
                        tvPin.setText("");
                        tvTitleDialog.setText("Pin does not match , Enter Pin Again");
                        count = 0;

                    }
                }


            }

        });

        btn1.setOnClickListener(v -> {
            onClickChangePin(v);
        });
        btn2.setOnClickListener(v -> {
            onClickChangePin(v);
        });
        btn3.setOnClickListener(v -> onClickChangePin(v));
        btn4.setOnClickListener(v -> {
            onClickChangePin(v);
        });
        btn5.setOnClickListener(v -> {
            onClickChangePin(v);
        });
        btn6.setOnClickListener(v -> onClickChangePin(v));
        btn7.setOnClickListener(v -> {
            onClickChangePin(v);
        });
        btn8.setOnClickListener(v -> {
            onClickChangePin(v);
        });
        btn9.setOnClickListener(v -> onClickChangePin(v));
        btn0.setOnClickListener(v -> onClickChangePin(v));
        btnClear.setOnClickListener(v -> {
            onClearPinPassword(v);
        });

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


    public void onClickChangePin(View view) {
        Button button = (Button) view;
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
        final View dialogPattern = layoutInflater.inflate(R.layout.dialog_pattern_password, null);


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
                if (changePattern.isEmpty()) {

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

                        SharedPreferences.Editor editor = sharedPreferencesPassword.edit();

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


    // Kiểm tra xem người dùng đã đăng ký vân tay hay chưa
    // nếu người dùng chưa đăng ký vân tay thì intent đăng ký
    public boolean isFingerprintRegistered(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            FingerprintManager fingerprintManager = context.getSystemService(FingerprintManager.class);
            if (fingerprintManager != null && fingerprintManager.hasEnrolledFingerprints()) {
                return true;
            }
        } else {
            FingerprintManagerCompat fingerprintManagerCompat = FingerprintManagerCompat.from(context);
            if (fingerprintManagerCompat != null && fingerprintManagerCompat.hasEnrolledFingerprints()) {
                return true;
            }
        }
        return false;
    }


}
