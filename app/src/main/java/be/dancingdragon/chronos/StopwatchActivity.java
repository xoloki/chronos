package be.dancingdragon.chronos;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class StopwatchActivity extends AppCompatActivity
{
    final static String TAG = "StopwatchActivity";
    static final String PREFS_DATA = "CHRONOS_PREFS_DATA";
    final static long TICK = 44;

    Handler mHandler = null;

    Map<Timer, View> mTimers = null;

    LinearLayout mMainLayout = null;

    static StopwatchActivity mInstance = null;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.main);

        //LinearLayout linearLayout = (LinearLayout)findViewById(R.id.timers);

        LayoutInflater inflater = getLayoutInflater();
        mMainLayout = (LinearLayout)inflater.inflate(R.layout.main, null);

        setContentView(mMainLayout);
        
        mHandler = new Handler(Looper.getMainLooper()) {
                public void handleMessage(Message msg) {
                }
            };
        
        Thread dbLoad = new Thread() {
                public void run() {
                    Log.i(TAG, "dbLoad thread");

                    Log.i(TAG, "getting all timers");
                    final List<Timer> timers = timerDAO().getAll();

                    Runnable t = new Runnable() {
                            public void run() {
                                String ids = "";
                                for(Timer timer : timers) ids += "{uid " + timer.uid + " started " + timer.started + " startTime " + timer.startTime + " stopTime " + timer.stopTime + "}";
                                Log.i(TAG, "calling back to main thread onDbLoad with timers " + ids);
                                onDbLoad(timers);
                            }
                        };
                    mHandler.post(t);
                }
            };
        dbLoad.start();
/*
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.i(TAG, "clicked floating action button");
                    Thread dbInsert = new Thread() {
                            public void run() {
                                final Timer timer = new Timer();
                                Log.i(TAG,"before insert, timer.uid = " + timer.uid);
                                timerDAO().insertAll(timer);
                                Log.i(TAG,"after insert, timer.uid = " + timer.uid);

                                Runnable t = new Runnable() {
                                        public void run() {
                                            onDbAdd(timer);
                                        }
                                    };
                                mHandler.post(t);

                            }
                        };

                    dbInsert.start();
                }
            });
  */
        mInstance = this;
    }

    void onDbLoad(List<Timer> timers) {
        mTimers = new HashMap();
        for(final Timer timer : timers) {
            onDbAdd(timer);
        }

        update();
        setTimer(TICK);

        startService(new Intent(this, NotificationService.class));
    }
    
    void onDbAdd(final Timer timer) {
        LayoutInflater inflater = getLayoutInflater();
        View timerView = inflater.inflate(R.layout.timer, null);
        
        //mMainLayout.addView(timerView);
        LinearLayout timersView = (LinearLayout)mMainLayout.findViewById(R.id.timers);
        timersView.addView(timerView);
        
        mTimers.put(timer, timerView);
        
        final Button renameButton = (Button)timerView.findViewById(R.id.timer_rename);
        renameButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    onRename(timer);
                }
            });
        //renameButton.setDrawable();
        final Button deleteButton = (Button)timerView.findViewById(R.id.timer_delete);
        deleteButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    onDelete(timer);
                }
            });

        final Button resetButton = (Button)timerView.findViewById(R.id.timer_reset);
        resetButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    onReset(timer);
                }
            });
        
        final Button startStopButton = (Button)timerView.findViewById(R.id.timer_start_stop);
        startStopButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    onStartStop(timer);
                }
            });
        
        update();
    }

    void onRename(Timer timer) {
        View timerView = mTimers.get(timer);
        TextView nameView = (TextView)timerView.findViewById(R.id.timer_name);
        //nameView.setText(name);
        nameView.setSelected(true);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder
            .setTitle("Rename timer '" + timer.name + "'")
        // Add the buttons
            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked OK button
                    }
                })
            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    void onDelete(Timer timer) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Add the buttons
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User clicked OK button
                }
            });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User cancelled the dialog
                }
            });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    void onReset(Timer timer) {
        long now = System.currentTimeMillis();

        if(timer.started) {
            timer.startTime = now;
        } else {
            timer.startTime = 0;
        }
        timer.stopTime = 0;

        update(timer);
        updateDb(timer);
    }
    
    void onStartStop(Timer timer) {
        long now = System.currentTimeMillis();

        if(timer.started) {
            timer.stopTime = now;
        } else {
            if(timer.startTime == 0) {
                timer.startTime = now;
            } else {
                timer.startTime = now - (timer.stopTime - timer.startTime);
            }
            timer.stopTime = 0;
        }

        timer.started = !timer.started;
        
        update(timer);
        updateDb(timer);
    }

    void updateDb(final Timer timer) {
        Thread dbUpdate = new Thread() {
                public void run() {
                    Log.i(TAG, "dbUpdate thread");

                    Log.i(TAG, "updating timer");
                    timerDAO().update(timer);
                    /*
                    Runnable t = new Runnable() {
                            public void run() {
                                String ids = "";
                                for(Timer timer : timers) ids += "{uid " + timer.uid + " started " + timer.started + " startTime " + timer.startTime + " stopTime " + timer.stopTime + "}";
                                Log.i(TAG, "calling back to main thread onDbLoad with timers " + ids);
                                onDbLoad(timers);
                            }
                        };
                    mHandler.post(t);
                    */
                }
            };
        dbUpdate.start();
    }
    
    TimerDAO timerDAO() {
        AppDatabase db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "chronos").build();
        return db.timerDAO();
    }
    
    /*    
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
    	super.onPrepareOptionsMenu(menu);

    	menu.clear();
        //
        menu.add(Menu.NONE, R.string.menu_reset, Menu.NONE, R.string.menu_reset)
        	.setIcon(android.R.drawable.ic_menu_revert)
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        menu.add(Menu.NONE, R.string.menu_start_stop, Menu.NONE, R.string.menu_start_stop)
        	.setIcon(android.R.drawable.ic_menu_send)
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);


        return true; 
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	return onOptionsItemIdSelected(item.getItemId());
    }

    public boolean onOptionsItemIdSelected(int id) {
        long now = System.currentTimeMillis();

    	switch(id) {
    	case R.string.menu_reset:
            setStart(now);
            setStop(now);
            break;
    	case R.string.menu_start_stop:
            setStarted(!getStarted());
            
            if(!getStarted()) {
                setStop(now);
            }

            if(getStarted() && getStart() == 0) {
                setStart(now);
            } 

            if(getStarted() && getStop() != 0) {
                long diff = getStop() - getStart();
                setStart(now - diff);
            }

            break;
        }

        update();

        return true;
    }
*/
    void update() {
        for(Timer timer : mTimers.keySet()) {
            update(timer);
        }
    }

    void update(Timer timer) {
        View timerView = mTimers.get(timer);
        long start = timer.startTime;
        long stop = timer.stopTime;
        boolean started = timer.started;
        String name = timer.name;

        TextView nameView = (TextView)timerView.findViewById(R.id.timer_name);
        nameView.setText(name);
        
        DecimalFormat fmt = new DecimalFormat("00");
        
        long now = System.currentTimeMillis();
        if(!started) {
            now = stop;
        }

        long diff = now - start;

        Log.i(TAG, "update timer uid " + timer.uid + "started " + timer.started + " start " + timer.startTime + " stop " + timer.stopTime + ", diff " + diff);


        long hourDiff = diff / (1000 * 60 * 60);
        long minDiff = (diff - hourDiff * 60 * 60 * 1000) / (1000 * 60);
        long secDiff = (diff - hourDiff * 60 * 60 * 1000 - minDiff * 60 * 1000) / (1000);
        long tenthDiff = (diff - hourDiff * 60 * 60 * 1000 - minDiff * 60 * 1000 - secDiff * 1000) / (100);
        
        TextView hourView = (TextView)timerView.findViewById(R.id.timer_hours);
        String hourVal = fmt.format(hourDiff);
        
        hourView.setText(hourVal);
        
        TextView minView = (TextView)timerView.findViewById(R.id.timer_minutes);
        String minVal = fmt.format(minDiff);
        
        minView.setText(minVal);
        
        TextView secView = (TextView)timerView.findViewById(R.id.timer_seconds);
        String secVal = fmt.format(secDiff);
        
        secView.setText(secVal);
        
        TextView tenthView = (TextView)timerView.findViewById(R.id.timer_tenths);
        String tenthVal = Long.valueOf(tenthDiff).toString();
        
        tenthView.setText(tenthVal);

        Button startStopView = (Button)timerView.findViewById(R.id.timer_start_stop);
        String startStopVal = started ? "Stop" : "Start";
        
        startStopView.setText(startStopVal);

        long nextTick = diff % TICK;
        
        //return nextTick == 0 ? TICK : nextTick;
    }

    protected void setTimer(final long time) {
        Runnable t = new Runnable() {
                public void run() {
                        update();
                        setTimer(time);
                    }
            };
        mHandler.postDelayed(t, time);
    }


    static void setDataLong(Context context, String key, long val) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_DATA, 0);
        SharedPreferences.Editor editor = settings.edit();

        editor.putLong(key, val);
        editor.apply();
    }

    static void setDataBoolean(Context context, String key, boolean val) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_DATA, 0);
        SharedPreferences.Editor editor = settings.edit();

        editor.putBoolean(key, val);
        editor.apply();
    }

    static long getDataLong(Context context, String key, long def) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_DATA, 0);
        return settings.getLong(key, def);
    }

    static boolean getDataBoolean(Context context, String key, boolean def) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_DATA, 0);
        return settings.getBoolean(key, def);
    }
}
