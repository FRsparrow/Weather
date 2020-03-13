package com.example.weather;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.Inflater;

import static android.content.Context.LOCATION_SERVICE;
import static androidx.core.content.ContextCompat.getSystemService;

public class WeatherListFragment extends Fragment {
    private boolean isPhone;
    private boolean isMetric;
    private boolean firstLaunch = true;
    private List<WeatherInfo> weatherInfos;
    private SQLiteDatabase mDatabase;
    public static final int COMPLETED = 0;
    public static final int UNCOMPLETED = 1;
    private int getFromAPIResult = 0;
    public static final String TAG = "WeatherListFragment";
    public static String TodayInfo;

    public void setMetric(boolean isMetric){ this.isMetric = isMetric; }

    public void setWeatherInfos(List<WeatherInfo> weatherInfos){ this.weatherInfos = weatherInfos; }

    //改变单位制
    protected void changeUnit(boolean isMetricToBritish)
    {
        for(int i = 0; i < weatherInfos.size(); ++i)
        {
            DecimalFormat df = new DecimalFormat("0.0");
            float low = Float.parseFloat(weatherInfos.get(i).lowTemperature);
            float high = Float.parseFloat(weatherInfos.get(i).highTemperature);
            float wind = Float.parseFloat(weatherInfos.get(i).wind);
            if(isMetricToBritish)       //公制转英制
            {
                //Log.d(TAG, "changeUnit: 公制转英制");
                weatherInfos.get(i).lowTemperature = df.format(1.8 * low + 32);
                weatherInfos.get(i).highTemperature = df.format(1.8 * high + 32);
                weatherInfos.get(i).wind = df.format(wind / 1.6);
                //Log.d(TAG, "changeUnit: 转换后最低温度：" + weatherInfos.get(i).lowTemperature);
            }
            else                        //英制转公制
            {
                //Log.d(TAG, "changeUnit: 英制转公制");
                weatherInfos.get(i).lowTemperature = df.format((low - 32) / 1.8);
                weatherInfos.get(i).highTemperature = df.format((high - 32) / 1.8);
                weatherInfos.get(i).wind = df.format(wind * 1.6);
                //Log.d(TAG, "changeUnit: 转换后最低温度：" + weatherInfos.get(i).lowTemperature);
            }
        }
    }

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if(msg.what == UNCOMPLETED)
            {
                Log.d(TAG, "handleMessage: 网络连接失败，从数据库读取");
                Toast.makeText(getContext(), "网络连接失败！", Toast.LENGTH_SHORT).show();
            }
            else if(msg.what == COMPLETED)
            {
                Log.d(TAG, "handleMessage: 网络连接成功");
                Toast.makeText(getContext(), "更新成功！", Toast.LENGTH_SHORT).show();
            }
            showData();
        }
    };

    public WeatherListFragment(boolean isPhone, boolean isMetric){
        this.isPhone = isPhone;
        this.isMetric = isMetric;
    }

//    private void setWeatherInfo(List<WeatherInfo> weatherInfos, JSONObject infos) throws JSONException {
//        weatherInfos = new ArrayList<>();
//        JSONArray infoArray = infos.getJSONArray("daily_forecast");
//        for(int i = 0; i < infoArray.length(); ++i)
//        {
//            JSONObject dayWeatherInfo = infoArray.getJSONObject(i);
////            Log.d(TAG, "setWeatherInfo: 第" + i + "天：");
////            Log.d(TAG, "setWeatherInfo: dayWeatherInfo:" + dayWeatherInfo.toString());
//            weatherInfos.add(new WeatherInfo(dayWeatherInfo, (i == 0), (i == 1)));
////            weatherInfos[i].setInfo(dayWeatherInfo);
//            //weatherInfos.get(i).printInfo();
//        }
//    }
//
//    //从api取得数据
//    private int getWeatherInfoFromAPI(List<WeatherInfo> weatherInfos) throws IOException {
//
//        /*获取参数*/
//        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
//        String location = sharedPreferences.getString(getString(R.string.cityKey), "auto_ip").toLowerCase();
//        String unit;
//        if(sharedPreferences.getString(getString(R.string.unitKey), "Metric").equals("Metric"))
//            unit = "m";
//        else
//            unit = "i";
//        final String key = "d481237e716a4c9681a2d27240dc2a78";
//
//        Log.d(TAG, "ready to connect");
//        String urlstr="https://free-api.heweather.net/s6/weather/forecast?" +
//                "location=" + location +
//                "&unit=" + unit +
//                "&key=" + key;
//        //建立网络连接
//        URL url = new URL(urlstr);
//        HttpURLConnection http= (HttpURLConnection) url.openConnection();
//        http.setRequestMethod("GET");
//        http.setConnectTimeout(5000);
//        http.setReadTimeout(2000);
//        http.setDoOutput(true);
//        http.setDoInput(true);
//
//        InputStream in = http.getInputStream();
//
//        if(http.getResponseCode() == HttpURLConnection.HTTP_OK) {
//            Log.d(TAG, "connect success");
//        }
//        else {
//            Log.d(TAG, "status:" + http.getResponseCode());
//            return -1;
//        }
//        Log.d(TAG, "endif");
//
//        Log.d(TAG, "get success");
//
//        //读取网页返回的数据
//        ByteArrayOutputStream out = new ByteArrayOutputStream();
//        int bytesRead = 0;
//        byte[] buffer = new byte[1024];
//        while((bytesRead = in.read(buffer)) > 0)
//            out.write(buffer, 0, bytesRead);
//        out.close();
//
//        String resultStr = out.toString();
//        Log.d(TAG, "getStringFromAPI: " + resultStr);
//
//        try {
//            /*获取服务器返回的JSON数据*/
//            JSONObject jsonObject = new JSONObject(resultStr);
//            JSONObject info = jsonObject.getJSONArray("HeWeather6").getJSONObject(0);
//            String status = info.getString("status");
//            if(status.equals("ok"))
//            {
//                Log.d(TAG, "getWeatherInfoFromAPI: status:ok");
//                setWeatherInfo(weatherInfos, info);
//                return 1;
//            }
//            else
//                return 0;
//
//        } catch (Exception e) {
//            // TODO: handle exception
//            Log.e(TAG, "the Error parsing data "+e.toString());
//        }
//        return 1;
//    }
//
//    //从SQLite取得数据
//    private void getWeatherInfoFromSQLite(List<WeatherInfo> weatherInfos)
//    {
//        int i = 0;
//        weatherInfos = new ArrayList<>();
//        mDatabase = new WeatherBaseHelper(getContext()).getWritableDatabase();
//        Cursor cursor = mDatabase.query("weathers",null,null,null,null,null,null);
//        while(cursor.moveToNext())
//        {
//            String[] infos = {cursor.getString(cursor.getColumnIndex("month")),
//                    cursor.getString(cursor.getColumnIndex("day")),
//                    cursor.getString(cursor.getColumnIndex("tmp_max")),
//                    cursor.getString(cursor.getColumnIndex("tmp_min")),
//                    cursor.getString(cursor.getColumnIndex("humidity")),
//                    cursor.getString(cursor.getColumnIndex("pressure")),
//                    cursor.getString(cursor.getColumnIndex("wind")),
//                    cursor.getString(cursor.getColumnIndex("wind_dir")),
//                    cursor.getString(cursor.getColumnIndex("weathertype")),
//                    cursor.getString(cursor.getColumnIndex("pnglabel"))};
////            for(String str: infos)
////                Log.d(TAG, "getWeatherInfoFromSQLite: " + str);
//            weatherInfos.add(new WeatherInfo(infos[0], infos[1], infos[2], infos[3], infos[4], infos[5], infos[6], infos[7], infos[8],infos[9], (i == 0), (i == 1)));
//            ++i;
//        }
//        cursor.close();
//    }

    //显示数据在屏幕上
    private void showData()
    {
        if(weatherInfos.isEmpty())
        {
            Toast.makeText(getContext(), "获取数据失败！", Toast.LENGTH_SHORT).show();
            return;
        }
        TodayInfo = "天气：" + weatherInfos.get(0).weatherType
                + " 最高温度：" + weatherInfos.get(0).highTemperature
                + " 最低温度：" + weatherInfos.get(0).lowTemperature;
        //在列表上显示数据
        Log.d(TAG, "showData: executed");
//        ((MainActivity)getActivity()).setFirstDay(weatherInfos.get(0).date, weatherInfos.get(0).highTemperature, weatherInfos.get(0).lowTemperature, weatherInfos.get(0).weatherType);;
        RecyclerView recyclerView = (RecyclerView) getActivity().findViewById(R.id.recycler_view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        Log.d(TAG, "showData: list size:" + weatherInfos.size());
        WeatherAdapter adapter = new WeatherAdapter(weatherInfos, isPhone);
        recyclerView.setAdapter(adapter);

        //若从api获取数据失败则弹出toast
//        if(getFromAPIResult != 1)
//            Toast.makeText(getContext(), "网络连接失败！", Toast.LENGTH_SHORT).show();
        //第一次启动
        if(firstLaunch) {
            firstLaunch = false;
            //平板在右边显示今天天气
            if (!isPhone) {
                Log.d(TAG, "showData: I'm tablet!");
                WeatherDetailFragment fragment = (WeatherDetailFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.fragment_right_container);
                fragment.set(weatherInfos.get(0), isMetric);
                ((MainActivity) getActivity()).shareInfo = Util.getShareInfo(getContext(), weatherInfos.get(0), isMetric);
            }
        }
        else
        {
            if(!isPhone)
            {
                WeatherDetailFragment fragment = (WeatherDetailFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.fragment_right_container);
                fragment.set(weatherInfos.get(0), isMetric);
                ((MainActivity) getActivity()).shareInfo = Util.getShareInfo(getContext(), weatherInfos.get(0), isMetric);
            }
        }
    }

    //后台线程，从网络或数据库读取数据
    private class Getter extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            try
            {
                weatherInfos = new ArrayList<>();
                getFromAPIResult = Util.getWeatherInfoFromAPI(getContext(), weatherInfos);
                Log.d(TAG, "doInBackground: result:");
                weatherInfos.get(0).printInfo();
            }catch (Exception e)
            {
                e.printStackTrace();
            } finally {
                if(getFromAPIResult != 1)       //从api获取数据失败
                {
                    Util.getWeatherInfoFromSQLite(getContext(), weatherInfos);
                    Message message = new Message();
                    message.what = UNCOMPLETED;
                    handler.sendMessage(message);
                }
                else
                {
                    Util.storeData(getContext(), weatherInfos, mDatabase);
                    Message message = new Message();
                    message.what = COMPLETED;
                    handler.sendMessage(message);
                }
            }
//            try
//            {
//                if(getWeatherInfoFromAPI() == 1)        //从api获取数据成功
//                {
//                    //显示数据
//                    Message message = new Message();
//                    message.what = COMPLETED;
//                    handler.sendMessage(message);
//                }
//                else
//                {
//                    //从SQLite读取数据并显示
//                    getWeatherInfoFromSQLite();
//                    Message message = new Message();
//                    message.what = COMPLETED;
//                    handler.sendMessage(message);
//                }
//            }catch (Exception e)
//            {
//                e.printStackTrace();
//            }
            return null;
        }
    }

//    //将数据存在数据库中
//    private void storeData()
//    {
//        if(weatherInfos.isEmpty())
//        {
//            Log.d(TAG, "storeData: 数据信息空");
//            return;
//        }
//        if(mDatabase == null)
//            mDatabase = new WeatherBaseHelper(getContext()).getWritableDatabase();
//
//        Cursor cursor = mDatabase.rawQuery("select * from weathers", null);
//        if(cursor.getCount() > 0) {
//            Log.d(TAG, "storeData: dbsize:" + cursor.getCount());
//            mDatabase.execSQL("delete from weathers");
//        }
//        else
//            Log.d(TAG, "storeData: 数据表为空，不删除");
//        cursor.close();
//        for(WeatherInfo weatherInfo: weatherInfos)
//        {
//            ContentValues contentValues = new ContentValues();
//            contentValues.put("month", weatherInfo.month);
//            contentValues.put("day", weatherInfo.day);
//            contentValues.put("weekday", Util.getWeekday(weatherInfo.year, weatherInfo.month, weatherInfo.day));
//            contentValues.put("tmp_max", weatherInfo.highTemperature);
//            contentValues.put("tmp_min", weatherInfo.lowTemperature);
//            contentValues.put("humidity", weatherInfo.humidity);
//            contentValues.put("pressure", weatherInfo.pressure);
//            contentValues.put("wind", weatherInfo.wind);
//            contentValues.put("wind_dir", weatherInfo.windDir);
//            contentValues.put("weathertype", weatherInfo.weatherType);
//            contentValues.put("pnglabel", weatherInfo.pngLabel);
//            mDatabase.insert("weathers", null, contentValues);
//        }
//        Log.d(TAG, "storeData: 存储数据成功");
//    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        boolean isOn = PreferenceManager.getDefaultSharedPreferences(getContext())
                .getBoolean("NK", true);
        WeatherService.setServiceAlarm(getActivity(), isOn);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_weatherlist, container, false);
        new Getter().execute();


//        mTextView = (TextView) v.findViewById(R.id.testTextView);
//        mTextView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if(isPhone) //设备为手机
//                {
//                    Intent intent = new Intent(getContext(), WeatherDetailActivity.class);
//                    intent.putExtra("weatherInfoObject", weatherInfos.toArray());
//                    startActivity(intent);
//                }
//                else    //设备为平板
//                {
//                    FragmentManager fm = getActivity().getSupportFragmentManager();
//                    Fragment fragment = fm.findFragmentById(R.id.fragment_right_container);
//                    if(fragment == null)
//                    {
//                        fragment = new WeatherDetailFragment();
//                        fm.beginTransaction()
//                                .add(R.id.fragment_right_container, fragment)
//                                .commit();
//                    }
//                }
//            }
//        });
        return v;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Util.storeData(getContext(), weatherInfos, mDatabase);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: resumed firstLaunch" + firstLaunch);
        if(!firstLaunch)
            showData();
    }

    private class WeatherAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
        private List<WeatherInfo> mWeatherInfoList;
        private int currentPosition = 0;
        private boolean isPhone;
        private final int TODAYVIEW = 0;
        private final int NOTTODAYVIEW = 1;

        public WeatherAdapter(List<WeatherInfo> weatherInfoList, boolean isPhone){
            mWeatherInfoList = weatherInfoList;
            this.isPhone = isPhone;
        }

        @Override
        public int getItemViewType(int position) {
            currentPosition = position;
//            Log.d(TAG, "getItemViewType: currentPosition:" + currentPosition);
            if (mWeatherInfoList.get(position).isToday)
                return TODAYVIEW;
            else
                return NOTTODAYVIEW;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            //当前条目不是今天或者当前设备为平板
            if(viewType == NOTTODAYVIEW || !isPhone) {
                int position = currentPosition;
//                Log.d(TAG, "onCreateViewHolder: curr:" + position);
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.weather_item, parent, false);
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!isPhone) {
                            ((WeatherDetailFragment) getActivity()
                                    .getSupportFragmentManager()
                                    .findFragmentById(R.id.fragment_right_container))
                                    .set(weatherInfos.get(position), isMetric);
                            ((MainActivity)getActivity()).shareInfo = Util.getShareInfo(getContext(), weatherInfos.get(position), isMetric);
                        }
                        else{
                            Intent intent = new Intent(getContext(), WeatherDetailActivity.class);
                            intent.putExtra("weatherInfo", weatherInfos.get(position));
                            intent.putExtra("isMetric", isMetric);
                            startActivity(intent);
                        }
                    }
                });
                ViewHolder holder = new ViewHolder(view);
                return holder;
            }
            else{
                int position = currentPosition;
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.weather_today_item, parent, false);
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getContext(), WeatherDetailActivity.class);
                        intent.putExtra("weatherInfo", weatherInfos.get(position));
                        intent.putExtra("isMetric", isMetric);
                        startActivity(intent);
                    }
                });
                TodayViewHolder todayViewHolder = new TodayViewHolder(view);
                return todayViewHolder;
            }
        }


        @SuppressLint("SetTextI18n")
        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            WeatherInfo weatherInfo = mWeatherInfoList.get(position);
            AssetManager assetManager = getContext().getAssets();
            Bitmap bmp = null;
            try {
                InputStream in = assetManager.open("pngs/"+ weatherInfo.pngLabel +".png");
                bmp = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                // TODO: handle exception
                e.printStackTrace();
            }
            if(holder instanceof ViewHolder)
            {
                ((ViewHolder)holder).weatherImage.setImageBitmap(bmp);
                //平板上的第一天信息
                if(!isPhone && weatherInfo.isToday)
                    ((ViewHolder)holder).dateTextView.setText("Today," + WeatherInfo.getMonthString(Integer.parseInt(weatherInfo.month)) + " " + weatherInfo.day);
                else {
                    if(weatherInfo.isTomorrow)
                        ((ViewHolder) holder).dateTextView.setText("Tomorrow");
                    else
                        ((ViewHolder) holder).dateTextView.setText(Util.getWeekday(weatherInfo.year, weatherInfo.month, weatherInfo.day));
                }
                ((ViewHolder)holder).weatherTextView.setText(weatherInfo.weatherType);
                ((ViewHolder)holder).highTextView.setText(weatherInfo.highTemperature + "°");
                ((ViewHolder)holder).lowTextView.setText(weatherInfo.lowTemperature + "°");
            }
            else if(holder instanceof TodayViewHolder)
            {
                Log.d(TAG, "onBindViewHolder: month:" + weatherInfo.month);
                ((TodayViewHolder)holder).dateTextView.setText("Today," + WeatherInfo.getMonthString(Integer.parseInt(weatherInfo.month)) + " " + weatherInfo.day);
                ((TodayViewHolder)holder).highTextView.setText(weatherInfo.highTemperature + "°");
                ((TodayViewHolder)holder).lowTextView.setText(weatherInfo.lowTemperature + "°");
                ((TodayViewHolder)holder).weatherImage.setImageBitmap(bmp);
                ((TodayViewHolder)holder).weatherTextview.setText(weatherInfo.weatherType);
            }

        }

        @Override
        public int getItemCount() {
            return mWeatherInfoList.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder{
            ImageView weatherImage;
            TextView dateTextView;
            TextView weatherTextView;
            TextView highTextView;
            TextView lowTextView;

            public ViewHolder(View view){
                super(view);
                weatherImage = (ImageView) view.findViewById(R.id.weather_image);
                dateTextView = (TextView) view.findViewById(R.id.date);
                weatherTextView = (TextView) view.findViewById(R.id.weather);
                highTextView = (TextView) view.findViewById(R.id.tmp_max);
                lowTextView = (TextView) view.findViewById(R.id.tmp_min);
            }
        }

        public class TodayViewHolder extends RecyclerView.ViewHolder{
            TextView dateTextView;
            TextView highTextView;
            TextView lowTextView;
            ImageView weatherImage;
            TextView weatherTextview;

            public TodayViewHolder(@NonNull View itemView) {
                super(itemView);
                dateTextView = (TextView) itemView.findViewById(R.id.first_day_date);
                highTextView = (TextView) itemView.findViewById(R.id.first_day_tmp_max);
                lowTextView = (TextView) itemView.findViewById(R.id.first_day_tmp_min);
                weatherImage = (ImageView) itemView.findViewById(R.id.first_day_image);
                weatherTextview = (TextView) itemView.findViewById(R.id.first_day_weathertype);
            }
        }
    }

}
