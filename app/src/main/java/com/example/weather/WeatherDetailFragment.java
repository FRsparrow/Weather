package com.example.weather;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.io.InputStream;

public class WeatherDetailFragment extends Fragment {
    private boolean isPhone;
    private boolean isMetric;
    protected TextView mWeekday;
    protected TextView mDate;
    protected TextView mTmpMax;
    protected TextView mTmpMin;
    protected ImageView mWeatherImage;
    protected TextView mWeatherType;
    protected TextView mHumidity;
    protected TextView mPressure;
    protected TextView mWind;

    public WeatherDetailFragment(boolean isPhone, boolean isMetric){
        this.isPhone = isPhone;
        this.isMetric = isMetric;
    }

    public void set(WeatherInfo weatherInfo, boolean isMetric)
    {
        this.isMetric = isMetric;
        String tmpUnit = isMetric ? "℃" : "℉";
        String windUnit = isMetric ? "km/h" : "mile/h";
        AssetManager assetManager = getContext().getAssets();
        Bitmap bmp = null;
        try {
            InputStream in = assetManager.open("pngs/"+ weatherInfo.pngLabel +".png");
            bmp = BitmapFactory.decodeStream(in);
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        if (weatherInfo.isToday)
            mWeekday.setText("Today");
        else if (weatherInfo.isTomorrow)
            mWeekday.setText("Tomorrow");
        else
            mWeekday.setText(Util.getWeekday(weatherInfo.year, weatherInfo.month, weatherInfo.day));
        mDate.setText(WeatherInfo.getMonthString(Integer.parseInt(weatherInfo.month)) + " " + weatherInfo.day);
        mTmpMax.setText(weatherInfo.highTemperature + tmpUnit);
        mTmpMin.setText(weatherInfo.lowTemperature + tmpUnit);
//        mWeatherImage.setImageResource(weatherInfo.ImageId);
        mWeatherImage.setImageBitmap(bmp);
        mWeatherType.setText(weatherInfo.weatherType);
        mHumidity.setText("Humidity:" + weatherInfo.humidity + " %");
        mPressure.setText("Pressure:" + weatherInfo.pressure + " hPa");
        mWind.setText("Wind:" + weatherInfo.wind + " " + windUnit + " " + weatherInfo.windDir);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_detail, container, false);
        mWeekday = (TextView) v.findViewById(R.id.detail_weekday);
        mDate = (TextView) v.findViewById(R.id.detail_date);
        mTmpMax = (TextView) v.findViewById(R.id.detail_tmp_max);
        mTmpMin = (TextView) v.findViewById(R.id.detail_tmp_min);
        mWeatherImage = (ImageView) v.findViewById(R.id.detail_image);
        mWeatherType = (TextView) v.findViewById(R.id.detail_weathertype);
        mHumidity = (TextView) v.findViewById(R.id.detail_humidity);
        mPressure = (TextView) v.findViewById(R.id.detail_pressure);
        mWind = (TextView) v.findViewById(R.id.detail_wind);
        return v;
    }
}
