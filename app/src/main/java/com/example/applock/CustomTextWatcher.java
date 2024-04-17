package com.example.applock;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.TextView;

public class CustomTextWatcher implements TextWatcher {

    private TextView textView;


    public CustomTextWatcher(TextView textView) {
        this.textView = textView;
    }


    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {

        int length = s.length();
        if (length > 0) {
            char lastChar = s.charAt(length - 1);

            Animation animation = new ScaleAnimation(0.5f, 1.0f, 0.5f, 1.0f,
                    Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            animation.setDuration(200);
            textView.startAnimation(animation);
        }

    }
}
