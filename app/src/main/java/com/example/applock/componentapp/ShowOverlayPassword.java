package com.example.applock.componentapp;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.AdaptiveIconDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;

import com.example.applock.OverlayActivity;

import java.io.ByteArrayOutputStream;

public class ShowOverlayPassword {

    private Context context ;
    private String modeLock ;

    public ShowOverlayPassword(Context context , String modeLock) {
        this.context = context;
        this.modeLock = modeLock ;
    }

    public void showOverlayPassWord(String result, String lockName) throws PackageManager.NameNotFoundException {

        if (lockName.equals("lockScreenApp")) {
            // send overlay lock screen

            PackageManager packageManager = context.getPackageManager();

            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(result, PackageManager.GET_META_DATA);

            Drawable iconDrawable = applicationInfo.loadIcon(packageManager);

            Bitmap bitmap = null;

            if (iconDrawable instanceof BitmapDrawable) {
                bitmap = ((BitmapDrawable) iconDrawable).getBitmap();
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (iconDrawable instanceof AdaptiveIconDrawable) {
                    int width = iconDrawable.getIntrinsicWidth();
                    int height = iconDrawable.getIntrinsicHeight();
                    bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                    Canvas canvas = new Canvas(bitmap);
                    iconDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                    iconDrawable.draw(canvas);
                } else {
                    // Xử lý trường hợp khác nếu cần
                    bitmap = null;
                }
            }

            if (bitmap != null) {


                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 1, baos);
                byte[] b = baos.toByteArray();

                Intent intent = new Intent(context.getApplicationContext(), OverlayActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                intent.putExtra("package", result);
                intent.putExtra("picture", b);
                intent.putExtra("mode_lock", modeLock);

                intent.putExtra("lock_name", "lockScreenApp");

                context.startActivity(intent);

            }
        } else if (lockName.equals("lockRecentMenu")) {
            // lock recent menu apps


            Intent intent = new Intent(context.getApplicationContext(), OverlayActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);


            intent.putExtra("lock_name", "lockRecentMenu");

            context.startActivity(intent);

        }
    }


}
