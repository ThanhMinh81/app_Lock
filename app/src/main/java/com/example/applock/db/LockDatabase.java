package com.example.applock.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.example.applock.dao.LockDAO;
import com.example.applock.model.Lock;

@Database(entities = {Lock.class}, version = 5)
public abstract class LockDatabase extends RoomDatabase {
    public abstract LockDAO lockDAO();

}
