package com.example.applock.model;

import android.content.pm.ApplicationInfo;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "lockApps")
public class Lock implements Parcelable {
    @PrimaryKey(autoGenerate = true)
    private int idApp;

    // trường này check xem app đó có đang bị khóa không

    private boolean stateLock;

    // trường này chech xem có được mở hay khóa ở trạng thái tắt/mở màn hình
    private boolean stateLockScreenOff = false;

    // true  là khóa
    // false là mở khóa

    private String packageApp;
    @Ignore
    private ApplicationInfo applicationInfo;

    private String timeOpen = "0";
    private String timeClose = "0";

    private boolean stateLockScreenAfterMinute = false;


    public Lock() {
    }

    public Lock(int idApp, boolean stateLock, String packageApp, ApplicationInfo applicationInfo) {
        this.idApp = idApp;
        this.stateLock = stateLock;
        this.packageApp = packageApp;
        this.applicationInfo = applicationInfo;
    }

    protected Lock(Parcel in) {
        idApp = in.readInt();
        stateLock = in.readByte() != 0;
        packageApp = in.readString();
        applicationInfo = in.readParcelable(ApplicationInfo.class.getClassLoader());
        stateLockScreenOff = in.readByte() != 0;
        timeClose = in.readString();
        timeOpen = in.readString();
        stateLockScreenAfterMinute = in.readByte() != 0;
    }

    public static final Creator<Lock> CREATOR = new Creator<Lock>() {
        @Override
        public Lock createFromParcel(Parcel in) {
            return new Lock(in);
        }

        @Override
        public Lock[] newArray(int size) {
            return new Lock[size];
        }
    };

    public int getIdApp() {
        return idApp;
    }

    public void setIdApp(int idApp) {
        this.idApp = idApp;
    }

    public boolean isStateLock() {
        return stateLock;
    }

    public boolean isStateLockScreenOff() {
        return stateLockScreenOff;
    }

    public void setStateLockScreenOff(boolean stateLockScreenOff) {
        this.stateLockScreenOff = stateLockScreenOff;
    }

    public void setStateLock(boolean stateLock) {
        this.stateLock = stateLock;
    }

    public String getPackageApp() {
        return packageApp;
    }

    public void setPackageApp(String packageApp) {
        this.packageApp = packageApp;
    }

    public ApplicationInfo getApplicationInfo() {
        return applicationInfo;
    }

    public void setApplicationInfo(ApplicationInfo applicationInfo) {
        this.applicationInfo = applicationInfo;
    }

    public boolean isStateLockScreenAfterMinute() {
        return stateLockScreenAfterMinute;
    }

    public void setStateLockScreenAfterMinute(boolean stateLockScreenAfterMinute) {
        this.stateLockScreenAfterMinute = stateLockScreenAfterMinute;
    }

    public String getTimeOpen() {
        return timeOpen;
    }

    public void setTimeOpen(String timeOpen) {
        this.timeOpen = timeOpen;
    }

    public String getTimeClose() {
        return timeClose;
    }

    public void setTimeClose(String timeClose) {
        this.timeClose = timeClose;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeInt(idApp);
        parcel.writeByte((byte) (stateLock ? 1 : 0));
        parcel.writeString(packageApp);
        parcel.writeParcelable(applicationInfo, i);
        parcel.writeByte((byte) (stateLockScreenOff ? 1 : 0));
        parcel.writeString(timeClose);
        parcel.writeString(timeOpen);
        parcel.writeByte((byte) (stateLockScreenAfterMinute ? 1 : 0));
    }
}
