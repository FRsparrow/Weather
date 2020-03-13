package com.example.weather;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

public class WeatherBaseHelper extends SQLiteOpenHelper {
    private static final int VERSION = 1;
    private static final String DATABASE_NAME = "weatherBase.db";

    public WeatherBaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table weathers(" +
                "_id integer primary key autoincrement,month,day,weekday,tmp_max,tmp_min,humidity,pressure,wind,wind_dir,weathertype,pnglabel)");
        Log.d(WeatherListFragment.TAG, "onCreate: 建表成功");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
