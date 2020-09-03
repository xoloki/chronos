package be.dancingdragon.chronos;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "timer")
public class Timer {
    @PrimaryKey(autoGenerate = true)
    public int uid;

    @ColumnInfo(name = "started")
    public boolean started;

    @ColumnInfo(name = "start_time")
    public long startTime;

    @ColumnInfo(name = "stop_time")
    public long stopTime;
}
