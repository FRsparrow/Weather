package com.example.weather;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

public class WeatherInfo implements Parcelable{
    protected int ImageId;
    protected String year;
    protected String month;
    protected String day;
    protected String lowTemperature;
    protected String highTemperature;
    protected String humidity;
    protected String pressure;
    protected String wind;
    protected String windDir;
    protected String weatherType;
    protected String pngLabel;
    protected boolean isToday;
    protected boolean isTomorrow;

    public WeatherInfo(JSONObject jsonObject, boolean isToday, boolean isTomorrow) throws JSONException {
        ImageId = R.mipmap.ic_launcher;
        String dateStr = jsonObject.getString("date");
        int firstIndex = dateStr.indexOf("-");
        int secondIndex = dateStr.indexOf("-", firstIndex + 1);
        year = dateStr.substring(0, firstIndex);
        month = dateStr.substring(firstIndex + 1, secondIndex);
        day = dateStr.substring(secondIndex + 1);
        lowTemperature = jsonObject.getString("tmp_min");
        highTemperature = jsonObject.getString("tmp_max");
        humidity = jsonObject.getString("hum");
        pressure = jsonObject.getString("pres");
        windDir = jsonObject.getString("wind_dir");
        wind = jsonObject.getString("wind_spd");
        weatherType = jsonObject.getString("cond_txt_d");
        pngLabel = jsonObject.getString("cond_code_d");
        this.isToday = isToday;
        this.isTomorrow = isTomorrow;
    }

    public WeatherInfo(String month,String day,String tmp_max,String tmp_min,String humidity,String pressure,String wind,String wind_dir,String weathertype,String pngLabel, boolean isToday, boolean isTomorrow)
    {
        ImageId = R.mipmap.ic_launcher;
        year = "2019";
        this.month = month;
        this.day = day;
        lowTemperature = tmp_min;
        highTemperature = tmp_max;
        this.humidity = humidity;
        this.pressure = pressure;
        this.wind = wind;
        this.windDir = wind_dir;
        this.weatherType = weathertype;
        this.pngLabel = pngLabel;
        this.isToday = isToday;
        this.isTomorrow = isTomorrow;
    }

    protected WeatherInfo(Parcel in) {
        ImageId = in.readInt();
        year = in.readString();
        month = in.readString();
        day = in.readString();
        lowTemperature = in.readString();
        highTemperature = in.readString();
        humidity = in.readString();
        pressure = in.readString();
        wind = in.readString();
        windDir = in.readString();
        weatherType = in.readString();
        pngLabel = in.readString();
        isToday = in.readByte() != 0;
        isTomorrow = in.readByte() != 0;
    }

    public static final Creator<WeatherInfo> CREATOR = new Creator<WeatherInfo>() {
        @Override
        public WeatherInfo createFromParcel(Parcel in) {
            return new WeatherInfo(in);
        }

        @Override
        public WeatherInfo[] newArray(int size) {
            return new WeatherInfo[size];
        }
    };

    public static String getMonthString(int month)
    {
        switch (month){
            case 1:
                return "January";
            case 2:
                return "February";
            case 3:
                return "March";
            case 4:
                return "April";
            case 5:
                return "May";
            case 6:
                return "June";
            case 7:
                return "July";
            case 8:
                return "August";
            case 9:
                return "September";
            case 10:
                return "October";
            case 11:
                return "November";
            case 12:
                return "December";
            default:
                return "November";
        }
    }

    public static int getMonthInt(String month)
    {
        switch (month)
        {
            case "January":
                return 1;
            case "February":
                return 2;
            case "March":
                return 3;
            case "April":
                return 4;
            case "May":
                return 5;
            case "June":
                return 6;
            case "July":
                return 7;
            case "August":
                return 8;
            case "September":
                return 9;
            case "October":
                return 10;
            case "November":
                return 11;
            case "December":
                return 12;
            default:
                return 1;
        }
    }

//    public void setInfo(JSONObject jsonObject) throws JSONException {
//        String dateStr = jsonObject.getString("date");
//        int firstIndex = dateStr.indexOf("-");
//        int secondIndex = dateStr.indexOf("-", firstIndex + 1);
//        int year = Integer.parseInt(dateStr.substring(0, firstIndex));
//        int month = Integer.parseInt(dateStr.substring(firstIndex + 1, secondIndex));
//        int day = Integer.parseInt(dateStr.substring(secondIndex + 1));
//        date = new Date(year - 1900, month - 1, day);
//        lowTemperature = jsonObject.getString("tmp_min");
//        highTemperature = jsonObject.getString("tmp_max");
//        humidity = jsonObject.getString("hum");
//        pressure = jsonObject.getString("pres");
//        windDir = jsonObject.getString("wind_dir");
//        wind = jsonObject.getString("wind_spd");
//        weatherType = jsonObject.getString("cond_txt_d");
////        date = jsonObject.getString("date");
//    }

    public void printInfo()
    {
        Log.d(WeatherListFragment.TAG, "date:" + year + "-" + month + "-" + day);
        Log.d(WeatherListFragment.TAG, "lt:" + lowTemperature);
        Log.d(WeatherListFragment.TAG, "up:" + highTemperature);
        Log.d(WeatherListFragment.TAG, "hum:" + humidity);
        Log.d(WeatherListFragment.TAG, "press:" + pressure);
        Log.d(WeatherListFragment.TAG, "wind:" + wind);
        Log.d(WeatherListFragment.TAG, "windDir:" + windDir);
        Log.d(WeatherListFragment.TAG, "type:" + weatherType);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(ImageId);
        dest.writeString(year);
        dest.writeString(month);
        dest.writeString(day);
        dest.writeString(lowTemperature);
        dest.writeString(highTemperature);
        dest.writeString(humidity);
        dest.writeString(pressure);
        dest.writeString(wind);
        dest.writeString(windDir);
        dest.writeString(weatherType);
        dest.writeString(pngLabel);
        dest.writeByte((byte) (isToday ? 1 : 0));
        dest.writeByte((byte) (isTomorrow ? 1 : 0));
    }
}
