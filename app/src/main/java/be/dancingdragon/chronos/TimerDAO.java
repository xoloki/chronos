package be.dancingdragon.chronos;

import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import androidx.room.Dao;

import java.util.List;

@Dao
public interface TimerDAO {
    @Query("SELECT * FROM timer")
    List<Timer> getAll();

    @Query("SELECT * FROM timer WHERE uid IN (:timerIds)")
    List<Timer> loadAllByIds(int[] timerIds);

    //@Query("SELECT * FROM timer WHERE first_name LIKE :first AND " + "last_name LIKE :last LIMIT 1")
    //Timer findByName(String first, String last);

    @Insert
    void insertAll(Timer... timers);

    @Delete
    void delete(Timer timer);
}
