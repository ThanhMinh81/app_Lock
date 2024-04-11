package com.example.applock.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.example.applock.dao.LockDAO;

@Database(entities = {Lock.class}, version = 1)
public abstract class LockDatabase extends RoomDatabase {
    public abstract LockDAO lockDAO();

}
