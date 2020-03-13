package com.example.weather;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private boolean isPhone = false;
    private boolean isMetric = true;
    protected String shareInfo = "";
    private StringBuffer mStringBuffer;
    private static final int REQUEST_CODE = 0;

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.menu, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId())
//        {
//            case R.id.map:
//                //点击Map Location
//                mStringBuffer = new StringBuffer();
//                List<PackageInfo> pagestring = getPackageManager().getInstalledPackages(0);
//                for (PackageInfo p:pagestring){
//                    String  pageName = p.packageName;
////                    Log.i("aaa","包名有"+pageName+"\r\n");
//                    mStringBuffer.append(pageName+",");
//                }
//                if (mStringBuffer.toString().contains("com.baidu.BaiduMap")||
//                        mStringBuffer.toString().contains("com.autonavi.minimap")||
//                        mStringBuffer.toString().contains("com.sougou.map.anroid.maps")||
//                        mStringBuffer.toString().contains("com.tencent.map")){
////                    Uri mUri = Uri.parse("geo:39.940409,116.355257?q=西直门");
//                    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
//                    String city = sp.getString("city", "长沙");
//                    String lat = sp.getString("lat", "28.19408989");
//                    String lon = sp.getString("lon", "112.98227692");
//                    Log.d(WeatherListFragment.TAG, "onOptionsItemSelected: city: " + city + " lat: " + lat + " lon: " + lon);
//                    Uri mUri = Uri.parse("geo:" + lat + "," + lon + "?q=" + city);
//                    Intent mIntent = new Intent(Intent.ACTION_VIEW,mUri);
//                    startActivityForResult(mIntent, REQUEST_CODE);
//                }else {
//                    Toast.makeText(MainActivity.this,"请安装地图软件,否则无法使用该软件",Toast.LENGTH_SHORT).show();
//                }
//                break;
//            case R.id.settings:
//                //点击settings
//                Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
//                startActivityForResult(intent, REQUEST_CODE);
//                break;
//            case R.id.share:
//                //分享
//                Log.d("MainActivity", "onOptionsItemSelected: shareInfo:" + shareInfo);
//                Intent textIntent = new Intent(Intent.ACTION_SEND);
//                textIntent.setType("text/plain");
//                textIntent.putExtra(Intent.EXTRA_TEXT, shareInfo);
//                startActivity(Intent.createChooser(textIntent, "分享"));
//                break;
//            default:
//                break;
//        }
//        return true;
//    }

    public static Intent newIntent(Context context)
    {
        return  new Intent(context, MainActivity.class);
    }

    private void initToolbar()
    {
        mToolbar = findViewById(R.id.toolbar);
        mToolbar.inflateMenu(R.menu.menu);
        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId())
                {
                    case R.id.map:
                        //点击Map Location
                        mStringBuffer = new StringBuffer();
                        List<PackageInfo> pagestring = getPackageManager().getInstalledPackages(0);
                        for (PackageInfo p:pagestring){
                            String  pageName = p.packageName;
                            mStringBuffer.append(pageName+",");
                        }
                        if (mStringBuffer.toString().contains("com.baidu.BaiduMap")||
                                mStringBuffer.toString().contains("com.autonavi.minimap")||
                                mStringBuffer.toString().contains("com.sougou.map.anroid.maps")||
                                mStringBuffer.toString().contains("com.tencent.map")){
        //                    Uri mUri = Uri.parse("geo:39.940409,116.355257?q=西直门");
                            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                            String city = sp.getString("city", "长沙");
                            String lat = sp.getString("lat", "28.19408989");
                            String lon = sp.getString("lon", "112.98227692");
                            Log.d(WeatherListFragment.TAG, "onOptionsItemSelected: city: " + city + " lat: " + lat + " lon: " + lon);
                            Uri mUri = Uri.parse("geo:" + lat + "," + lon + "?q=" + city);
                            Intent mIntent = new Intent(Intent.ACTION_VIEW,mUri);
                            startActivityForResult(mIntent, REQUEST_CODE);
                        }else {
                            Toast.makeText(MainActivity.this,"请安装地图软件,否则无法使用该软件",Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case R.id.settings:
                        //点击settings
                        Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
                        startActivityForResult(intent, REQUEST_CODE);
                        break;
                    case R.id.share:
                        //分享
                        Log.d("MainActivity", "onOptionsItemSelected: shareInfo:" + shareInfo);
                        Intent textIntent = new Intent(Intent.ACTION_SEND);
                        textIntent.setType("text/plain");
                        textIntent.putExtra(Intent.EXTRA_TEXT, shareInfo);
                        startActivity(Intent.createChooser(textIntent, "分享"));
                        break;
                    default:
                        break;
                }
                return true;
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_masterdetail);
        initToolbar();
        if(findViewById(R.id.fragment_right_container) == null)
            isPhone = true;
        boolean isMetric = true;

        String unit = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("UK", "metric").toLowerCase();
        if(!unit.equals("metric"))
            isMetric = false;

        //System.Net.ServicePointManager.ServerCertificateValidationCallback = delegate { return true; };
        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.fragment_container);
        if(fragment == null)
        {
            fragment = new WeatherListFragment(isPhone, isMetric);
            fm.beginTransaction()
                    .add(R.id.fragment_container, fragment)
                    .commit();
        }
        if(!isPhone)
        {
            Fragment fragment_detail = fm.findFragmentById(R.id.fragment_right_container);
            if (fragment_detail == null) {
                fragment_detail = new WeatherDetailFragment(isPhone, isMetric);
                fm.beginTransaction()
                        .add(R.id.fragment_right_container, fragment_detail)
                        .commit();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(resultCode != Activity.RESULT_OK)
            return;
        if(requestCode == REQUEST_CODE)
        {
            if(data == null)
                return;
            Log.d(WeatherListFragment.TAG, "onActivityResult: data:");
            int size = data.getIntExtra("size", 0);
            boolean needChangeUnit = data.getBooleanExtra("needChangeUnit", false);
            boolean isMetric = PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                    .getString("UK", "Metric")
                    .equals("Metric");
            String city = data.getStringExtra("city");
            Log.d(WeatherListFragment.TAG, "size:" + size);
            Log.d(WeatherListFragment.TAG, "needChangeUnit: " + needChangeUnit);
            Log.d(WeatherListFragment.TAG, "city: " + city);
            if(size != 0 || needChangeUnit)
            {
                WeatherListFragment weatherListFragment = (WeatherListFragment) (getSupportFragmentManager()
                        .findFragmentById(R.id.fragment_container));
                weatherListFragment.setMetric(isMetric);
                if(size != 0)
                {
                    List<WeatherInfo> weatherInfos = new ArrayList<>();
                    for(int i = 0; i < size; ++i)
                    {
                        weatherInfos.add(data.getParcelableExtra("weatherInfo" + i));
                    }
                    weatherListFragment.setWeatherInfos(weatherInfos);
                }
                Log.d(WeatherListFragment.TAG, "onActivityResult: city: " + city);
                if(city != null)
                    Toast.makeText(getApplicationContext(), "当前城市：" + city, Toast.LENGTH_SHORT).show();

                if(needChangeUnit)
                {
                    weatherListFragment.changeUnit(!isMetric);
                }

            }
        }
    }

    protected void setFirstDay(Date date, String tmp_max, String tmp_min, String weatherType)
    {
        TextView DateTextView = (TextView) findViewById(R.id.first_day_date);
        TextView tmpMaxTextView = (TextView) findViewById(R.id.first_day_tmp_max);
        TextView tmpMinTextView = (TextView) findViewById(R.id.first_day_tmp_min);
        TextView weatherTypeTextView = (TextView) findViewById(R.id.first_day_weathertype);
        ImageView imageView = (ImageView) findViewById(R.id.first_day_image);
        DateTextView.setText("Today," + WeatherInfo.getMonthString(date.getMonth() + 1) + " " + date.getDate());
        tmpMaxTextView.setText(tmp_max + "°");
        tmpMinTextView.setText(tmp_min + "°");
        weatherTypeTextView.setText(weatherType);

    }

}
