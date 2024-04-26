package com.example.applock;

import android.Manifest;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.CancellationSignal;
import android.util.Log;
import android.widget.Toast;


import androidx.core.app.ActivityCompat;

import com.example.applock.Interface.ItemClickListenerFinger;

public class FingerprintHandler extends FingerprintManager.AuthenticationCallback {

    private ItemClickListenerFinger itemClickListenerFinger ;

    private Context context;

    // Constructor
    public FingerprintHandler(Context mContext) {
        context = mContext;
    }

    // Fingerprint authentication starts here..
    public void Authentication(FingerprintManager manager, FingerprintManager.CryptoObject cryptoObject , ItemClickListenerFinger itemClickListenerFinger) {
        this.itemClickListenerFinger = itemClickListenerFinger ;
        CancellationSignal cancellationSignal = new CancellationSignal();
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        manager.authenticate(cryptoObject, cancellationSignal, 0, this, null);
    }

    // On authentication failed
    @Override
    public void onAuthenticationFailed() {
        itemClickListenerFinger.resultConfirmFinger("false");

//        this.update("Authentication Failed!!!", false);
    }

    // On successful authentication
    @Override
    public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
       itemClickListenerFinger.resultConfirmFinger("success");
    }

    // This method is used to update the text message
    // depending on the authentication result
    public void update(String e, Boolean success){

    }


}