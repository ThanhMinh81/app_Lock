package com.example.applock.dao;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.applock.model.Lock;
import java.util.List;

@Dao
public interface LockDAO {
    @Insert
    public   void insertLocks(List<Lock> locks);
    @Query("SELECT *  FROM lockApps")
    List<Lock> getListApps();

    @Query("DELETE FROM lockApps WHERE idApp = :id")
    void removeAppLock(int id);


    @Update
    int updateLock(Lock lock);

    @Query("SELECT COUNT(*) FROM lockApps WHERE packageApp = :packageName AND stateLock = 1")
    int isPackageLocked(String packageName);


}
