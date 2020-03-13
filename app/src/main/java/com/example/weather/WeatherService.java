package com.example.weather;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import java.util.concurrent.TimeUnit;

public class WeatherService extends IntentService {
    private static final String TAG = "weatherservice";
    private static final long WEATHER_INTERVAL_MS = TimeUnit.MINUTES.toMillis(1440);

    public static void setServiceAlarm(Context context, boolean isOn)
    {
        Log.d(TAG, "setServiceAlarm: executed,isOn: " + isOn);
        Intent i = new Intent(context, WeatherService.class);
        PendingIntent pi = PendingIntent.getService(context, 0, i, 0);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if(isOn)
        {
            alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime(), WEATHER_INTERVAL_MS, pi);
        }
        else
        {
            alarmManager.cancel(pi);
            pi.cancel();
        }
    }
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
//    public WeatherService(String name) {
//        super(name);
//        Log.d(TAG, "WeatherService: I'm constructed");
//    }
    public WeatherService(){
        super("123");
        Log.d(TAG, "WeatherService: I'm constructed");
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.d(TAG, "onHandleIntent: executed");
        Intent i = MainActivity.newIntent(this);
        PendingIntent pi = PendingIntent.getActivity(this, 0, i, 0);

        NotificationChannel notificationChannel = new NotificationChannel("0", "123", NotificationManager.IMPORTANCE_DEFAULT);
        //新建通知并设定通知样式
        Notification notification = new NotificationCompat.Builder(this, "0")
                .setTicker("ticker")
                .setSmallIcon(R.drawable.sun)
                .setContentTitle("Today Weather")
                .setContentText(WeatherListFragment.TodayInfo)
                .setContentIntent(pi)
                .setAutoCancel(true)
                .setWhen(System.currentTimeMillis())
                .build();

        NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(this);
        notificationManager.createNotificationChannel(notificationChannel);
        //开启通知
        notificationManager.notify(0, notification);
    }
}
