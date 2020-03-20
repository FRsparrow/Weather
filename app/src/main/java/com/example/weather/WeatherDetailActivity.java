package com.example.weather;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import java.util.List;

public class WeatherDetailActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private boolean isMetric;
    private StringBuffer mStringBuffer;
    private String shareInfo;

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.detail_activity_menu, menu);
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
//                    //Log.i("aaa","包名有"+pageName+"\r\n");
//                    mStringBuffer.append(pageName+",");
//                }
//                if (mStringBuffer.toString().contains("com.baidu.BaiduMap")||
//                        mStringBuffer.toString().contains("com.autonavi.minimap")||
//                        mStringBuffer.toString().contains("com.sougou.map.anroid.maps")||
//                        mStringBuffer.toString().contains("com.tencent.map")){
//                    Uri mUri = Uri.parse("geo:39.940409,116.355257?q=西直门");
//                    Intent mIntent = new Intent(Intent.ACTION_VIEW,mUri);
//                    startActivity(mIntent);
//                }else {
//                    Toast.makeText(WeatherDetailActivity.this,"请安装地图软件,否则无法使用该软件",Toast.LENGTH_SHORT).show();
//                }
//                break;
//            case R.id.settings:
//                //点击settings
//                Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
//                startActivity(intent);
//                break;
//            case R.id.share:
//                //分享
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

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void initToolbar()
    {
        mToolbar = findViewById(R.id.toolbar);
        mToolbar.inflateMenu(R.menu.detail_activity_menu);
        mToolbar.setLogo(null);
        mToolbar.setNavigationIcon(R.drawable.backs);
        mToolbar.setTitle("Detail");
        mToolbar.setNavigationOnClickListener(view -> finish());
        mToolbar.setOnMenuItemClickListener(item -> {
                switch (item.getItemId())
                {
                    case R.id.map:
                        //点击Map Location
                        mStringBuffer = new StringBuffer();
                        List<PackageInfo> pagestring = getPackageManager().getInstalledPackages(0);
                        for (PackageInfo p:pagestring){
                            String  pageName = p.packageName;
                            //Log.i("aaa","包名有"+pageName+"\r\n");
                            mStringBuffer.append(pageName+",");
                        }
                        if (mStringBuffer.toString().contains("com.baidu.BaiduMap")||
                                mStringBuffer.toString().contains("com.autonavi.minimap")||
                                mStringBuffer.toString().contains("com.sougou.map.anroid.maps")||
                                mStringBuffer.toString().contains("com.tencent.map")){
                            Uri mUri = Uri.parse("geo:39.940409,116.355257?q=西直门");
                            Intent mIntent = new Intent(Intent.ACTION_VIEW,mUri);
                            startActivity(mIntent);
                        }else {
                            Toast.makeText(WeatherDetailActivity.this,"请安装地图软件,否则无法使用该软件",Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case R.id.settings:
                        //点击settings
                        Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
                        startActivity(intent);
                        break;
                    case R.id.share:
                        //分享
                        Intent textIntent = new Intent(Intent.ACTION_SEND);
                        textIntent.setType("text/plain");
                        textIntent.putExtra(Intent.EXTRA_TEXT, shareInfo);
                        startActivity(Intent.createChooser(textIntent, "分享"));
                        break;
                    default:
                        break;
                }
                return true;
            });
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_one);
        initToolbar();
        isMetric = getIntent().getBooleanExtra("isMetric", true);

        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.fragment_container);
        if(fragment == null)
        {
            fragment = new WeatherDetailFragment(true, isMetric);
            fm.beginTransaction().
                    add(R.id.fragment_container, fragment)
                    .commit();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        WeatherInfo weatherInfo = getIntent().getParcelableExtra("weatherInfo");
        ((WeatherDetailFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_container)
        ).set(weatherInfo, isMetric);
        shareInfo = Util.getShareInfo(getApplicationContext(), weatherInfo, isMetric);
    }

}
