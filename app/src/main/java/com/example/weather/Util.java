package com.example.weather;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.List;

public class Util {
    public static String TAG = WeatherListFragment.TAG;

    public static String getWeekday(int n)
    {
        switch (n)
        {
            case 0:
                return "Sunday";
            case 1:
                return "Monday";
            case 2:
                return "Tuesday";
            case 3:
                return "Wednesday";
            case 4:
                return "Thursday";
            case 5:
                return "Friday";
            case 6:
                return "Saturday";
            default:
                return "Sunday";
        }
    }

    public static String getWeekday(String year, String month, String day)
    {
        Date date = new Date(Integer.parseInt(year) - 1900, Integer.parseInt(month) - 1, Integer.parseInt(day));
        return getWeekday(date.getDay());
    }

    public static String getShareInfo(Context context, WeatherInfo weatherInfo, boolean isMetric)
    {
        String city = PreferenceManager.getDefaultSharedPreferences(context).getString("CK", "changsha");
        String shareInfo = "城市：" + city
                +"日期：" + weatherInfo.year + "年" + weatherInfo.month + "月" + weatherInfo.day + "日" + "\n"
                + "天气情况：" + weatherInfo.weatherType + "\n"
                + "最高温度：" + weatherInfo.highTemperature + (isMetric ? "℃" : "℉") + "\n"
                + "最低温度：" + weatherInfo.lowTemperature + (isMetric ? "℃" : "℉") + "\n"
                + "湿度：" + weatherInfo.humidity + "%" + "\n"
                + "大气压力:" + weatherInfo.pressure + "hPa" + "\n"
                + "风速：" + weatherInfo.wind + (isMetric ? "km/h" : "mile/h") + "\n"
                + "风向：" + weatherInfo.windDir;
        return shareInfo;
    }

    public static void setWeatherInfo(Context context, List<WeatherInfo> weatherInfos, JSONObject infos) throws JSONException {
        //weatherInfos = new ArrayList<>();
        JSONArray infoArray = infos.getJSONArray("daily_forecast");
        for(int i = 0; i < infoArray.length(); ++i)
        {
            JSONObject dayWeatherInfo = infoArray.getJSONObject(i);
//            Log.d(TAG, "setWeatherInfo: 第" + i + "天：");
//            Log.d(TAG, "setWeatherInfo: dayWeatherInfo:" + dayWeatherInfo.toString());
            weatherInfos.add(new WeatherInfo(dayWeatherInfo, (i == 0), (i == 1)));
//            weatherInfos[i].setInfo(dayWeatherInfo);
            //weatherInfos.get(i).printInfo();
        }
        JSONObject basic = infos.getJSONObject("basic");
        String lat = basic.getString("lat");
        String lon = basic.getString("lon");
        String city = basic.getString("parent_city");
        Log.d(WeatherListFragment.TAG, "更新: city: " + city + " lat: " + lat + " lon: " + lon);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putString("lat", lat).commit();
        sp.edit().putString("lon", lon).commit();
        sp.edit().putString("city", city).commit();
//        sp.edit().commit();
//        Log.d(TAG, "setWeatherInfo result: ");
//        weatherInfos.get(0).printInfo();

    }

    //从api取得数据
    public static int getWeatherInfoFromAPI(Context context, List<WeatherInfo> weatherInfos) throws IOException {

        /*获取参数*/
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String location = sharedPreferences.getString(context.getString(R.string.cityKey), "auto_ip").toLowerCase();
        String unit;
        if(sharedPreferences.getString(context.getString(R.string.unitKey), "Metric").equals("Metric"))
            unit = "m";
        else
            unit = "i";
        final String key = "d481237e716a4c9681a2d27240dc2a78";

        Log.d(TAG, "ready to connect");
        String urlstr="https://free-api.heweather.net/s6/weather/forecast?" +
                "location=" + location +
                "&unit=" + unit +
                "&key=" + key;
        //建立网络连接
        URL url = new URL(urlstr);
        HttpURLConnection http= (HttpURLConnection) url.openConnection();
        http.setRequestMethod("GET");
        http.setConnectTimeout(1000);
        http.setReadTimeout(2000);
        http.setDoOutput(true);
        http.setDoInput(true);

        InputStream in = http.getInputStream();

        if(http.getResponseCode() == HttpURLConnection.HTTP_OK) {
            Log.d(TAG, "connect success");
        }
        else {
            Log.d(TAG, "status:" + http.getResponseCode());
            return -1;
        }
        Log.d(TAG, "endif");

        Log.d(TAG, "get success");

        //读取网页返回的数据
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int bytesRead = 0;
        byte[] buffer = new byte[1024];
        while((bytesRead = in.read(buffer)) > 0)
            out.write(buffer, 0, bytesRead);
        out.close();

        String resultStr = out.toString();
        Log.d(TAG, "getStringFromAPI: " + resultStr);

        try {
            /*获取服务器返回的JSON数据*/
            JSONObject jsonObject = new JSONObject(resultStr);
            JSONObject info = jsonObject.getJSONArray("HeWeather6").getJSONObject(0);
            String status = info.getString("status");
            if(status.equals("ok"))
            {
                Log.d(TAG, "getWeatherInfoFromAPI: status:ok");
                setWeatherInfo(context, weatherInfos, info);
                return 1;
            }
            else
                return 0;

        } catch (Exception e) {
            // TODO: handle exception
            Log.e(TAG, "the Error parsing data "+e.toString());
        }
        return 1;
    }

    //从SQLite取得数据
    public static void getWeatherInfoFromSQLite(Context context, List<WeatherInfo> weatherInfos)
    {
        int i = 0;
        //weatherInfos = new ArrayList<>();
        SQLiteDatabase mDatabase = new WeatherBaseHelper(context).getWritableDatabase();
        Cursor cursor = mDatabase.query("weathers",null,null,null,null,null,null);
        while(cursor.moveToNext())
        {
            String[] infos = {cursor.getString(cursor.getColumnIndex("month")),
                    cursor.getString(cursor.getColumnIndex("day")),
                    cursor.getString(cursor.getColumnIndex("tmp_max")),
                    cursor.getString(cursor.getColumnIndex("tmp_min")),
                    cursor.getString(cursor.getColumnIndex("humidity")),
                    cursor.getString(cursor.getColumnIndex("pressure")),
                    cursor.getString(cursor.getColumnIndex("wind")),
                    cursor.getString(cursor.getColumnIndex("wind_dir")),
                    cursor.getString(cursor.getColumnIndex("weathertype")),
                    cursor.getString(cursor.getColumnIndex("pnglabel"))};
//            for(String str: infos)
//                Log.d(TAG, "getWeatherInfoFromSQLite: " + str);
            weatherInfos.add(new WeatherInfo(infos[0], infos[1], infos[2], infos[3], infos[4], infos[5], infos[6], infos[7], infos[8],infos[9], (i == 0), (i == 1)));
            ++i;
        }
        cursor.close();
    }

    //将数据存在数据库中
    public static void storeData(Context context, List<WeatherInfo> weatherInfos, SQLiteDatabase mDatabase)
    {
        if(weatherInfos.isEmpty())
        {
            Log.d(TAG, "storeData: 数据信息空");
            return;
        }
        if(mDatabase == null)
            mDatabase = new WeatherBaseHelper(context).getWritableDatabase();

        Cursor cursor = mDatabase.rawQuery("select * from weathers", null);
        if(cursor.getCount() > 0) {
            Log.d(TAG, "storeData: dbsize:" + cursor.getCount());
            mDatabase.execSQL("delete from weathers");
        }
        else
            Log.d(TAG, "storeData: 数据表为空，不删除");
        cursor.close();
        for(WeatherInfo weatherInfo: weatherInfos)
        {
            ContentValues contentValues = new ContentValues();
            contentValues.put("month", weatherInfo.month);
            contentValues.put("day", weatherInfo.day);
            contentValues.put("weekday", Util.getWeekday(weatherInfo.year, weatherInfo.month, weatherInfo.day));
            contentValues.put("tmp_max", weatherInfo.highTemperature);
            contentValues.put("tmp_min", weatherInfo.lowTemperature);
            contentValues.put("humidity", weatherInfo.humidity);
            contentValues.put("pressure", weatherInfo.pressure);
            contentValues.put("wind", weatherInfo.wind);
            contentValues.put("wind_dir", weatherInfo.windDir);
            contentValues.put("weathertype", weatherInfo.weatherType);
            contentValues.put("pnglabel", weatherInfo.pngLabel);
            mDatabase.insert("weathers", null, contentValues);
        }
        Log.d(TAG, "storeData: 存储数据成功");
    }

    //后台线程，从网络或数据库读取数据
//    public static class Getter extends AsyncTask<Void, Void, Void> {
//        Handler handler;
//
//        public Getter(Handler handler){ this.handler = handler; }
//
//        @Override
//        protected Void doInBackground(Void... voids) {
//            int getFromAPIResult = 0;
//            try
//            {
//                getFromAPIResult = getWeatherInfoFromAPI(weatherInfos);
//            }catch (Exception e)
//            {
//                e.printStackTrace();
//            } finally {
//                if(getFromAPIResult != 1)       //从api获取数据失败
//                {
//                    getWeatherInfoFromSQLite(weatherInfos);
//                }
//                else
//                {
//                    storeData();
//                }
//                Message message = new Message();
//                message.what = WeatherListFragment.COMPLETED;
//                handler.sendMessage(message);
//            }
////            try
////            {
////                if(getWeatherInfoFromAPI() == 1)        //从api获取数据成功
////                {
////                    //显示数据
////                    Message message = new Message();
////                    message.what = COMPLETED;
////                    handler.sendMessage(message);
////                }
////                else
////                {
////                    //从SQLite读取数据并显示
////                    getWeatherInfoFromSQLite();
////                    Message message = new Message();
////                    message.what = COMPLETED;
////                    handler.sendMessage(message);
////                }
////            }catch (Exception e)
////            {
////                e.printStackTrace();
////            }
//            return null;
//        }
//    }

//        private static final String TAG = "LocationUtils";

        /**
         * http://ip-api.com/json/58.192.32.1?fields=520191&lang=en
         * 根据ip获取位置信息
         *
         * @param ip
         * @return {"accuracy":50,"as":"AS4538 China Education and Research Network Center",
         * "city":"Nanjing","country":"China","countryCode":"CN","isp":
         * "China Education and Research Network Center","lat":32.0617,"lon":118.7778,"mobile":false,
         * "org":"China Education and Research Network Center","proxy":false,"query":"58.192.32.1",
         * "region":"JS","regionName":"Jiangsu","status":"success","timezone":"Asia/Shanghai","zip":""}
         */
        public static JSONObject Ip2Location(String ip) {
            JSONObject jsonObject = null;

            String urlStr = "http://ip-api.com/json/" + ip + "?fields=520191&lang=en";
            try {
                URL url = new URL(urlStr);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setReadTimeout(5000);//读取超时
                urlConnection.setConnectTimeout(5000); // 连接超时
                urlConnection.setDoInput(true);
                urlConnection.setUseCaches(false);
                int responseCode = urlConnection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    //找到服务器的情况下,可能还会找到别的网站返回html格式的数据
                    InputStream is = urlConnection.getInputStream();

                    BufferedReader buff = new BufferedReader(new InputStreamReader(is, "UTF-8"));//注意编码，会出现乱码
                    StringBuilder builder = new StringBuilder();
                    String line = null;
                    while ((line = buff.readLine()) != null) {
                        builder.append(line);
                    }
                    buff.close();//内部会关闭InputStream
                    urlConnection.disconnect();

                    String res = builder.toString();

                    Log.i(TAG, "Ip2Location: res -- "+res);
                    jsonObject = new JSONObject(res);
                }


            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Log.i(TAG, "获取得到的json：" + jsonObject.toString());
            return jsonObject;
        }


        /**
         * 根据ip通过百度api去获取城市
         * @param ip
         * @return
         */
        public static String Ip2LocationByBaiduApi(String ip){
            try {
                URL url = new URL("http://int.dpool.sina.com.cn/iplookup/iplookup.php?format=json&ip=" + ip);
                URLConnection connection = url.openConnection();
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(),"utf-8"));
                String line = null;
                StringBuffer res = new StringBuffer();
                while ((line = reader.readLine())!=null){
                    res.append(line);
                }
                reader.close();
                String ipAddr = res.toString();
                JSONObject jsonObject = new JSONObject(ipAddr);
                if ("1".equals(jsonObject.get("ret").toString())){
                    return jsonObject.get("city").toString();
                }else {
                    return "读取失败";
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
                return "读取失败 e -- "+e.getMessage();
            } catch (IOException e) {
                e.printStackTrace();
                return "读取失败 e -- "+e.getMessage();
            } catch (JSONException e) {
                e.printStackTrace();
                return "读取失败 e -- "+e.getMessage();
            }
        }
    }
//    public static void putWeatherInfo(Intent intent, WeatherInfo weatherInfo)
//    {
//        intent.putExtra("weekday", getWeekday())
//    }
//
//    public static void getWeatherInfo(Intent intent)
