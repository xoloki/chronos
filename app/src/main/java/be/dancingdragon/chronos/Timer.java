package be.dancingdragon.chronos;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "timer")
public class Timer {
    @PrimaryKey(autoGenerate = true)
    public int uid = 0;

    @ColumnInfo(name = "name")
    public String name = "Timer " + uid;

    @ColumnInfo(name = "started")
    public boolean started = false;

    @ColumnInfo(name = "start_time")
    public long startTime = 0;

    @ColumnInfo(name = "stop_time")
    public long stopTime = 0;
}
