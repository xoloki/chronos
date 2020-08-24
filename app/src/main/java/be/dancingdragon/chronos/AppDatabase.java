package be.dancingdragon.chronos;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {Timer.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract TimerDAO timerDAO();
}
