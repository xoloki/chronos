package be.dancingdragon.chronos;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = "timer")
public class Timer {
    @PrimaryKey
    public int uid;

    @ColumnInfo(name = "start_time")
    public long startTime;
}
