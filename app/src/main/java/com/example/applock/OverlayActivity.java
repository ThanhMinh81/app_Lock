package com.example.applock;

import android.annotation.TargetApi;
import android.app.AppOpsManager;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;
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
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.hardware.fingerprint.FingerprintManagerCompat;
import androidx.room.Room;

import com.andrognito.patternlockview.PatternLockView;
import com.andrognito.patternlockview.listener.PatternLockViewListener;
import com.andrognito.patternlockview.utils.PatternLockUtils;
import com.example.applock.Interface.ItemClickListenerFinger;
import com.example.applock.db.LockDatabase;
import com.example.applock.model.Lock;
import com.google.android.material.button.MaterialButton;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.Executor;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

public class OverlayActivity extends AppCompatActivity {


    // nếu không có vân tay thì icon vân tay không được hiển thị
    // check thiết bị có bật vân tay hay không

    private static final String KEY_NAME = "GEEKSFORGEEKS";
    ImageView imgChangeMode;

    PatternLockView patternLockView;
    TableLayout tableLayout;
    TextView tvNameMode;

    ImageView imgIconApp;
    String packageApp;
    LockDatabase database;
    boolean patternMode = true;
    TextView tvPin;

    private Cipher cipher;

    private KeyStore keyStore;
    Lock lock;
    private String passwordPattern;
    private String passwordPin;

    MaterialButton btnClear;

    private String modeLock;
    private Button btn1, btn2, btn3, btn4, btn5, btn6, btn7, btn8, btn9, btn0;
    private KeyguardManager keyguardManager;
    private ItemClickListenerFinger itemClickListenerFinger;

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

        itemClickListenerFinger = new ItemClickListenerFinger() {
            @Override
            public void resultConfirmFinger(String result) {
                if (result.equals("success")) {
                    confirmSuccess();
                }
            }
        };


        if (byteArray != null) {

            Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);

            imgIconApp.setImageBitmap(bitmap);

        }
        // send bitmap

        SharedPreferences sharedPreferences = getSharedPreferences("PREFS", Context.MODE_PRIVATE);
        passwordPattern = sharedPreferences.getString("password_pattern", "null");
        passwordPin = sharedPreferences.getString("password_pin", "null");

        if (!passwordPin.equals("null")) {

            InputFilter[] filterArray = new InputFilter[1];
            filterArray[0] = new InputFilter.LengthFilter(passwordPin.length());
            tvPin.setFilters(filterArray);

        }


        LayoutInflater inflater = LayoutInflater.from(this);

        handleClick();


        boolean checkFingerPrint =  isFingerprintAuthAvailable(OverlayActivity.this);


        // thiet bi ho tro van tay
        if(checkFingerPrint)
        {

            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {

                // be hon hoac bang api 28
                fingerBiometric();

            } else {

                // cao hon api 28
                handleEventFingerPrint();

            }
        }else {
        }


    }

    private void fingerBiometric() {

        Executor executor = ContextCompat.getMainExecutor(this);

        BiometricPrompt biometricPrompt = new BiometricPrompt(this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                // Xác thực vân tay thành công
//                Toast.makeText(OverlayActivity.this, "Xác thực vân tay thành công", Toast.LENGTH_SHORT).show();
                confirmSuccess();

            }

            @Override
            public void onAuthenticationError(int errorCode, CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                // Xác thực vân tay thất bại
                Toast.makeText(OverlayActivity.this, "Xác thực vân tay thất bại", Toast.LENGTH_SHORT).show();
            }
        });

        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Xác thực vân tay")
                .setNegativeButtonText("Hủy")
                .setConfirmationRequired(false)
                .build();

        biometricPrompt.authenticate(promptInfo);


    }

    private void handleEventFingerPrint() {

        // Initializing KeyguardManager and FingerprintManager
        keyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
        FingerprintManager fingerprintManager = (FingerprintManager) getSystemService(FINGERPRINT_SERVICE);

        // Here, we are using various security checks
        // Checking device is inbuilt with fingerprint sensor or not
        if (!fingerprintManager.isHardwareDetected()) {

            Log.d("FinggerrrPRINTTTT", "Device does not support fingerprint sensor");

            // Setting error message if device
            // doesn't have fingerprint sensor
//            errorText.setText("Device does not support fingerprint sensor");
        } else {
            // Checking fingerprint permission
//            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
//                errorText.setText("Fingerprint authentication is not enabled");
//            }else{
            // Check for at least one registered finger
            if (!fingerprintManager.hasEnrolledFingerprints()) {
//                    errorText.setText("Register at least one finger");
            } else {
                // Checking for screen lock security
                if (!keyguardManager.isKeyguardSecure()) {
//                        errorText.setText("Screen lock security not enabled");
                } else {

                    // if everything is enabled and correct then we will generate
                    // the encryption key which will be stored on the device
                    generateKey();
                    if (cipherInit()) {
                        FingerprintManager.CryptoObject cryptoObject = new FingerprintManager.CryptoObject(cipher);
                        FingerprintHandler helper = new FingerprintHandler(this);
                        helper.Authentication(fingerprintManager, cryptoObject, itemClickListenerFinger);
                    }
                }
            }
//            }
        }
    }


    protected void generateKey() {
        try {
            keyStore = KeyStore.getInstance("AndroidKeyStore");
        } catch (Exception e) {
            e.printStackTrace();
        }


        KeyGenerator keyGenerator;
        try {
            keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            throw new RuntimeException("KeyGenerator instance failed", e);
        }

        try {
            keyStore.load(null);
            keyGenerator.init(new
                    KeyGenParameterSpec.Builder(KEY_NAME,
                    KeyProperties.PURPOSE_ENCRYPT |
                            KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setUserAuthenticationRequired(true)
                    .setEncryptionPaddings(
                            KeyProperties.ENCRYPTION_PADDING_PKCS7)
                    .build());
            keyGenerator.generateKey();
        } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException | IOException |
                 CertificateException e) {
            throw new RuntimeException(e);
        }
    }


    public boolean cipherInit() {
        try {
            cipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/" + KeyProperties.BLOCK_MODE_CBC + "/" + KeyProperties.ENCRYPTION_PADDING_PKCS7);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new RuntimeException("Cipher failed", e);
        }

        try {
            keyStore.load(null);
            SecretKey key = (SecretKey) keyStore.getKey(KEY_NAME,
                    null);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return true;
        } catch (KeyPermanentlyInvalidatedException e) {
            return false;
        } catch (KeyStoreException | UnrecoverableKeyException | IOException |
                 NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Cipher initialization failed", e);
        } catch (java.security.cert.CertificateException e) {
            throw new RuntimeException(e);
        }
    }


    private void initView() {

        tvPin = findViewById(R.id.tvShowPassWord);
        imgChangeMode = findViewById(R.id.imgChangeModeComfirm);
        tableLayout = findViewById(R.id.tableLayout);
        patternLockView = findViewById(R.id.pattern_lock_view);
        tvNameMode = findViewById(R.id.tvNameMode);
        imgIconApp = findViewById(R.id.img_iconApp);
        btnClear = findViewById(R.id.btnClear);
        btn1 = findViewById(R.id.btn1);
        btn2 = findViewById(R.id.btn2);
        btn3 = findViewById(R.id.btn3);
        btn4 = findViewById(R.id.btn4);
        btn5 = findViewById(R.id.btn5);
        btn6 = findViewById(R.id.btn6);
        btn7 = findViewById(R.id.btn7);
        btn8 = findViewById(R.id.btn8);
        btn9 = findViewById(R.id.btn9);
        btn0 = findViewById(R.id.btn0);


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

    public void onClickChangePin(View view) {
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

    private void handleClick() {

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
                imgChangeMode.setImageDrawable(getResources().getDrawable(R.drawable.ic_pin_password, null));

            } else {
                tvPin.setVisibility(View.VISIBLE);
                tvNameMode.setText("Enter pin");
                patternLockView.setVisibility(View.GONE);
                tableLayout.setVisibility(View.VISIBLE);
                imgChangeMode.setImageDrawable(getResources().getDrawable(R.drawable.ic_pattern_password, null));

            }

        });
        tvPin.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                Log.d("|0909fsdf", modeLock);

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

        btnClear.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                tvPin.setText("");

                return false;
            }
        });

    }

    private void confirmSuccess() {
        // gui package ve service
        Intent intent = new Intent("ACTION_LOCK_APP");
        intent.putExtra("message", packageApp);
        sendBroadcast(intent);

        if (lock != null) {

            if (modeLock.equals("immediately")) {
                lock.setStateLock(false);
                database.lockDAO().updateLock(lock);
            } else if (modeLock.equals("screen_off")) {
                lock.setStateLockScreenOff(false);
                database.lockDAO().updateLock(lock);

            } else {

                // trường hợp cuối cùng khóa bằng thời gian
                //

                Calendar currentTime = Calendar.getInstance();

                int hour = currentTime.get(Calendar.HOUR_OF_DAY);

                int minute = currentTime.get(Calendar.MINUTE);

                int minuteOpen = hour * 60 + minute;

                int minuteClose = hour * 60 + minute + Integer.parseInt(modeLock);

                lock.setTimeClose(String.valueOf(minuteClose));

                lock.setTimeOpen(String.valueOf(minuteOpen));

                lock.setStateLockScreenAfterMinute(false);

                database.lockDAO().updateLock(lock);

            }
        }

        finish();

    }

    public void onNumberClick(View view) {
        Button button = (Button) view;

        String currentText = tvPin.getText().toString();
        String buttonText = button.getText().toString();
        String updatedText = currentText + buttonText;
        tvPin.setText(updatedText);
    }


    public void onClearClick(View view) {
        String currentText = tvPin.getText().toString();
        if (!currentText.isEmpty()) {
            String newText = currentText.substring(0, currentText.length() - 1);
            tvPin.setText(newText);
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


    // kiem tra van tay cua thiet bi co ho tro hay khong
    public static boolean isFingerprintAuthAvailable(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            FingerprintManager fingerprintManager = context.getSystemService(FingerprintManager.class);
            if (fingerprintManager != null && fingerprintManager.isHardwareDetected()) {
                return true;
            }
        } else {
            FingerprintManagerCompat fingerprintManagerCompat = FingerprintManagerCompat.from(context);
            if (fingerprintManagerCompat != null && fingerprintManagerCompat.isHardwareDetected()) {
                return true;
            }
        }
        return false;
    }



    // kiem tra thiết bị có vân tay hay không
//    private boolean isAccessGranted() {
//
//        try {
//            PackageManager packageManager = getPackageManager();
//            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(getPackageName(), 0);
//            AppOpsManager appOpsManager = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
//            int mode = 0;
//            if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.KITKAT) {
//                mode = appOpsManager.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, applicationInfo.uid, applicationInfo.packageName);
//            }
//            return (mode == AppOpsManager.MODE_ALLOWED);
//
//        } catch (PackageManager.NameNotFoundException e) {
//            return false;
//        }
//
//    }




}
