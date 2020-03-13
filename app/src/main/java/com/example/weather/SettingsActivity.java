package com.example.weather;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class SettingsActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = WeatherListFragment.TAG;
    private Intent data;
    private List<WeatherInfo> weatherInfos;
    private SQLiteDatabase mDatabase;
    private int getFromAPIResult = 0;
    private int COMPLETED = 0;
    private int UNCOMPLETED = 1;
    private String mCityValue;
    private String mPreUnit;
    private String mUnit;
    private SharedPreferences mSharedPreferences;
    private Preference mCityListPreference;
    private Preference mUnitListPreference;
    private Preference mNotificationPreference;

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            String s = mCityListPreference.getKey();
            if(msg.what == COMPLETED)
            {

                mCityListPreference.setSummary(mSharedPreferences.getString(s, "auto_ip"));
                //mCityListPreference.setDefaultValue(mSharedPreferences.getString(s, "auto_ip"));
                mCityValue = mCityListPreference.getSummary().toString();
                mPreUnit = mUnitListPreference.getSummary().toString();
                data.putExtra("city", mCityValue);
            }
            else if(msg.what == UNCOMPLETED)
            {
                Toast.makeText(getApplicationContext(),
                        "网络连接失败，无法更换城市！",
                        Toast.LENGTH_SHORT).show();
                mCityListPreference.setSummary(mCityValue);
                //mCityListPreference.setDefaultValue(mCityValue);
                mSharedPreferences.edit().putString(s, mCityValue).commit();
            }
        }
    };

    //后台线程，从网络或数据库读取数据
    private class Getter extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            try
            {
                weatherInfos = new ArrayList<>();
                getFromAPIResult = Util.getWeatherInfoFromAPI(getApplicationContext(), weatherInfos);
                Log.d(TAG, "settingActivity doInBackground: result:");
                weatherInfos.get(0).printInfo();
            }catch (Exception e)
            {
                e.printStackTrace();
            } finally {
                if(getFromAPIResult != 1)       //从api获取数据失败
                {
                    Message message = new Message();
                    message.what = UNCOMPLETED;
                    handler.sendMessage(message);
                }
                else
                {
                    mDatabase = new WeatherBaseHelper(getApplicationContext()).getWritableDatabase();
                    Util.storeData(getApplicationContext(), weatherInfos, mDatabase);
                    data.putExtra("size", weatherInfos.size());
                    data.putExtra("needChangeUnit", false);
                    for(int i = 0; i < weatherInfos.size(); ++i)
                    {
                        data.putExtra("weatherInfo" + i, weatherInfos.get(i));
                    }
                    Message msg = new Message();
                    msg.what = COMPLETED;
                    handler.sendMessage(msg);
                }
            }
            return null;
        }
    }

    private void initView()
    {
        data = new Intent();
        weatherInfos = new ArrayList<>();
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mSharedPreferences.registerOnSharedPreferenceChangeListener(this);
        mCityListPreference = (ListPreference) findPreference(getString(R.string.cityKey));
        mUnitListPreference = (ListPreference) findPreference(getString(R.string.unitKey));
        mNotificationPreference = (CheckBoxPreference) findPreference(getString(R.string.notificationKey));
        mCityListPreference.setSummary(mSharedPreferences.getString(mCityListPreference.getKey(), "auto_ip"));
        mCityValue = mCityListPreference.getSummary().toString();
        mUnitListPreference.setSummary(mSharedPreferences.getString(mUnitListPreference.getKey(), "Metric"));
        mUnit = mUnitListPreference.getSummary().toString();
        mPreUnit = mUnit;
        mNotificationPreference.setSummary(mSharedPreferences.getBoolean(mNotificationPreference.getKey(), true) ? "Enable" : "Disabled");
        data.putExtra("size", 0);
        data.putExtra("isMetric", mUnit.equals("Metric") ? true : false);
        data.putExtra("needChangeUnit", false);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.fragment_weatherlist);
        addPreferencesFromResource(R.xml.settings_preference);
        initView();

    }

//    //改变单位制
//    private void changeUnit(boolean isMetricToBritish)
//    {
//        for(int i = 0; i < weatherInfos.size(); ++i)
//        {
//            DecimalFormat df = new DecimalFormat("0.0");
//            float low = Float.parseFloat(weatherInfos.get(i).lowTemperature);
//            float high = Float.parseFloat(weatherInfos.get(i).highTemperature);
//            float wind = Float.parseFloat(weatherInfos.get(i).wind);
//            if(isMetricToBritish)       //公制转英制
//            {
//                Log.d(TAG, "changeUnit: 公制转英制");
//                weatherInfos.get(i).lowTemperature = df.format(1.8 * low + 32);
//                weatherInfos.get(i).highTemperature = df.format(1.8 * high + 32);
//                weatherInfos.get(i).wind = df.format(wind / 1.6);
//                Log.d(TAG, "changeUnit: 转换后最低温度：" + weatherInfos.get(i).lowTemperature);
//            }
//            else                        //英制转公制
//            {
//                Log.d(TAG, "changeUnit: 英制转公制");
//                weatherInfos.get(i).lowTemperature = df.format((low - 32) / 1.8);
//                weatherInfos.get(i).highTemperature = df.format((high - 32) / 1.8);
//                weatherInfos.get(i).wind = df.format(wind * 1.6);
//                Log.d(TAG, "changeUnit: 转换后最低温度：" + weatherInfos.get(i).lowTemperature);
//            }
//        }
//    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        if(s.equals(mCityListPreference.getKey()))  //改变了城市
        {
            Log.d(TAG, "onSharedPreferenceChanged: city may be changed");
            new Getter().execute();
        }
        else if(s.equals(mUnitListPreference.getKey()))     //改变了单位制
        {
            Log.d(TAG, "onSharedPreferenceChanged: unit changed");
            mUnitListPreference.setSummary(mSharedPreferences.getString(s, "Metric"));
            mUnitListPreference.setDefaultValue(mSharedPreferences.getString(s, "Metric"));
            //从数据库获取weatherInfos
//            weatherInfos.clear();
//            Util.getWeatherInfoFromSQLite(getApplicationContext(), weatherInfos);
            //转换单位
//            changeUnit(mUnit.equals("Metric"));
//            mUnit = mUnitListPreference.getSummary().toString();
//            Util.storeData(getApplicationContext(), weatherInfos, mDatabase);
            mUnit = mUnitListPreference.getSummary().toString();
            data.putExtra("isMetric", mUnit.equals("Metric") ? true : false);
            data.putExtra("needChangeUnit", mPreUnit.equals(mUnit) ? false : true);
//            data.putExtra("size", weatherInfos.size());
//            Log.d(TAG, "onSharedPreferenceChanged: size" + weatherInfos.size());
//            for(int i = 0; i < weatherInfos.size(); ++i)
//            {
//                data.putExtra("weatherInfo" + i, weatherInfos.get(i));
//            }
            //data.putExtra("unit", mUnitListPreference.getSummary().toString().toLowerCase());
            //data.putExtra("cityChanged", );
        }
        else if(s.equals(mNotificationPreference.getKey()))
        {
            mNotificationPreference.setSummary(mSharedPreferences.getBoolean(mNotificationPreference.getKey(), false) ? "Enable" : "Disabled");
            boolean isOn = mSharedPreferences.getBoolean(s, true);
            WeatherService.setServiceAlarm(getApplicationContext(), isOn);
        }
        setResult(RESULT_OK, data);
    }
}
