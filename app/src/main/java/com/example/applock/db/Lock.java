package com.example.applock.db;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "lockApps")
public class Lock  implements Parcelable {
    @PrimaryKey(autoGenerate = true)
    private int idApp;
    // id app , AppInforName
    private  String name ;


    public Lock() {
    }

    public Lock(@NonNull int idApp, String name) {
        this.idApp = idApp;
        this.name = name;
    }




    protected Lock(Parcel in) {
        idApp = in.readInt();
        name = in.readString();
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

    @NonNull
    public int getIdApp() {
        return idApp;
    }

    public void setIdApp(@NonNull int idApp) {
        this.idApp = idApp;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeInt(idApp);
        dest.writeString(name);
    }
}
