package be.dancingdragon.chronos;

import java.util.ArrayList;
import java.text.DecimalFormat;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

public class NotificationService extends Service {
	final static String TAG = "NotificationService";
    final static long TICK = 250;

    NotificationManager mManager = null;
    Handler mHandler = null;
    
	public void onCreate() {
		Log.d(TAG, "onCreate");

        mManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        mHandler = new Handler(Looper.getMainLooper()) {
    		public void handleMessage(Message msg) {
            }
        };

        setTimer(TICK);
	}
	
	public void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "onDestroy");

	}

	public void onStart(Intent intent, int startId) {
		Log.d(TAG, "onStart");

	}
	
    public IBinder onBind(Intent intent) {
        return null;
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

    void update() {
        for(Timer timer : StopwatchActivity.mInstance.mTimers.keySet()) {
            update(timer);
        }
    }
    
    void update(Timer timer) {
        CharSequence title = "Stopwatch";

        long start = timer.startTime;
        long stop = timer.stopTime;
        boolean started = timer.started;

        DecimalFormat fmt = new DecimalFormat("00");
        
        long now = System.currentTimeMillis();
        if(!started) {
            now = stop;
        }

        long diff = now - start;
        long hourDiff = diff / (1000 * 60 * 60);
        long minDiff = (diff - hourDiff * 60 * 60 * 1000) / (1000 * 60);
        long secDiff = (diff - hourDiff * 60 * 60 * 1000 - minDiff * 60 * 1000) / (1000);
        //long tenthDiff = (diff - hourDiff * 60 * 60 * 1000 - minDiff * 60 * 1000 - secDiff * 1000) / (100);
        
        String hourVal = fmt.format(hourDiff);

        String minVal = fmt.format(minDiff);
        String secVal = fmt.format(secDiff);
        //String tenthVal = Long.valueOf(tenthDiff).toString();

        CharSequence text = hourVal + ":" + minVal + ":" + secVal;// + "." + tenthVal;
        Intent nintent = new Intent(this, StopwatchActivity.class);
        PendingIntent pintent = PendingIntent.getActivity(this, 0, nintent, 0);
        
        Notification notification = new Notification.Builder(this)
            .setContentTitle(title)
            .setContentText(text)
            .setContentIntent(pintent)
            .setSmallIcon(R.drawable.ic_launcher)
            .setVisibility(Notification.VISIBILITY_PUBLIC)
            .build(); 

        mManager.notify(0, notification);

        long nextTick = diff % TICK;
        
        //return nextTick == 0 ? TICK : nextTick;
    }

}
