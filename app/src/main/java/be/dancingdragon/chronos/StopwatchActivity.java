package be.dancingdragon.chronos;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
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

import androidx.room.Room;

import java.text.DecimalFormat;
import java.util.List;

public class StopwatchActivity extends Activity
{
    final static String TAG = "StopwatchActivity";
    static final String PREFS_DATA = "CHRONOS_PREFS_DATA";
    final static long TICK = 44;

    Handler mHandler = null;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.main);

        //LinearLayout linearLayout = (LinearLayout)findViewById(R.id.timers);

        LayoutInflater inflater = getLayoutInflater();
        LinearLayout linearLayout = (LinearLayout)inflater.inflate(R.layout.main, null);

        AppDatabase db = Room.databaseBuilder(getApplicationContext(),
        AppDatabase.class, "database-name").build();        
        List<Timer> timers = db.timerDAO().getAll();

        for(Timer timer : timers) {
            View timerView = inflater.inflate(R.layout.timer, null);

            linearLayout.addView(timerView);
            
            final Button resetButton = (Button)timerView.findViewById(R.id.timer_reset);
            resetButton.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        onOptionsItemIdSelected(R.string.menu_reset);
                    }
                });
            
            final Button startStopButton = (Button)timerView.findViewById(R.id.timer_start_stop);
            startStopButton.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        onOptionsItemIdSelected(R.string.menu_start_stop);
                    }
                });
        }
        
        setContentView(linearLayout);
        
        mHandler = new Handler(Looper.getMainLooper()) {
    		public void handleMessage(Message msg) {
            }
        };

        update();
        setTimer(TICK);

        startService(new Intent(this, NotificationService.class));
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
    	super.onPrepareOptionsMenu(menu);

    	menu.clear();
        /*
        menu.add(Menu.NONE, R.string.menu_reset, Menu.NONE, R.string.menu_reset)
        	.setIcon(android.R.drawable.ic_menu_revert)
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        menu.add(Menu.NONE, R.string.menu_start_stop, Menu.NONE, R.string.menu_start_stop)
        	.setIcon(android.R.drawable.ic_menu_send)
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        */

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

    long update() {
        long start = getStart();
        long stop = getStop();
        boolean started = getStarted();

        DecimalFormat fmt = new DecimalFormat("00");
        
        long now = System.currentTimeMillis();
        if(!started) {
            now = stop;
        }

        long diff = now - start;
        long hourDiff = diff / (1000 * 60 * 60);
        long minDiff = (diff - hourDiff * 60 * 60 * 1000) / (1000 * 60);
        long secDiff = (diff - hourDiff * 60 * 60 * 1000 - minDiff * 60 * 1000) / (1000);
        long tenthDiff = (diff - hourDiff * 60 * 60 * 1000 - minDiff * 60 * 1000 - secDiff * 1000) / (100);
        
        TextView hourView = (TextView)findViewById(R.id.timer_hours);
        String hourVal = fmt.format(hourDiff);
        
        hourView.setText(hourVal);
        
        TextView minView = (TextView)findViewById(R.id.timer_minutes);
        String minVal = fmt.format(minDiff);
        
        minView.setText(minVal);
        
        TextView secView = (TextView)findViewById(R.id.timer_seconds);
        String secVal = fmt.format(secDiff);
        
        secView.setText(secVal);
        
        TextView tenthView = (TextView)findViewById(R.id.timer_tenths);
        String tenthVal = Long.valueOf(tenthDiff).toString();
        
        tenthView.setText(tenthVal);

        Button startStopView = (Button)findViewById(R.id.timer_start_stop);
        String startStopVal = started ? "Stop" : "Start";
        
        startStopView.setText(startStopVal);

        long nextTick = diff % TICK;
        
        return nextTick == 0 ? TICK : nextTick;
    }

    protected void setTimer(final long time) {
        Runnable t = new Runnable() {
                public void run() {
                    long nextTick = TICK;

                    if(getStarted()) {
                        nextTick = update();
                    }
                    
                    setTimer(time);
                }
            };
        mHandler.postDelayed(t, time);
    }

    void setStart(long start) {
        setDataLong(getApplicationContext(), "start", start);
    }     
    
    void setStop(long stop) {
        setDataLong(getApplicationContext(), "stop", stop);
    }     
    
    void setStarted(boolean started) {
        setDataBoolean(getApplicationContext(), "started", started);
    }     
    
    long getStart() {
        return getDataLong(getApplicationContext(), "start", 0);
    }     
    
    long getStop() {
        return getDataLong(getApplicationContext(), "stop", 0);
    }     
    
    boolean getStarted() {
        return getDataBoolean(getApplicationContext(), "started", false);
    }     

    static void setStart(Context context, long start) {
        setDataLong(context, "start", start);
    }     

    static void setStop(Context context, long stop) {
        setDataLong(context, "stop", stop);
    }     

    static void setStarted(Context context, boolean started) {
        setDataBoolean(context, "started", started);
    }     

    static long getStart(Context context) {
        return getDataLong(context, "start", 0);
    }     

    static long getStop(Context context) {
        return getDataLong(context, "stop", 0);
    }     

    static boolean getStarted(Context context) {
        return getDataBoolean(context, "started", false);
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
