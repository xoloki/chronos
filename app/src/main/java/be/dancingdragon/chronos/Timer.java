package be.dancingdragon.chronos;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "timer")
public class Timer {
    @PrimaryKey
    public int uid;

    @ColumnInfo(name = "start_time")
    public long startTime;
}
