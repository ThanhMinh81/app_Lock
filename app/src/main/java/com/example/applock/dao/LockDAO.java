package com.example.applock.dao;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import com.example.applock.db.Lock;
import java.util.List;

@Dao
public interface LockDAO {
    @Insert
    public void insert(Lock... locks);
    @Query("SELECT *  FROM lockApps")
    List<Lock> getListApps();


    @Query("DELETE FROM lockApps WHERE idApp = :id")
    void removeAppLock(int id);


}
