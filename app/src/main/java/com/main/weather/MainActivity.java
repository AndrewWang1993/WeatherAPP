package com.main.weather;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.mGifLibrary.GifImageView;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.main.Json.Json_Operation;
import com.main.apapter.WeatherPagerAdapter;
import com.main.app.Application;
import com.main.bean.AirBall;
import com.main.bean.City;
import com.main.bean.Pm2d5;
import com.main.bean.SimpleWeather;
import com.main.bean.SimpleWeatherinfo;
import com.main.bean.Weather;
import com.main.bean.Weatherinfo;
import com.main.db.CityDB;
import com.main.fragment.FirstWeatherFragment;
import com.main.fragment.SecondWeatherFragment;
import com.main.indicator.CirclePageIndicator;
import com.main.util.ConfigCache;
import com.main.util.IphoneDialog;
import com.main.util.L;
import com.main.util.NetUtil;
import com.main.util.SharePreferenceUtil;
import com.main.util.T;
import com.main.util.TimeUtil;

import com.myChart.*;


public class MainActivity extends FragmentActivity implements
        Application.EventHandler, OnClickListener {
    public static final String UPDATE_WIDGET_WEATHER_ACTION = "com.way.action.update_weather";
    public static final String WEATHER_SIMPLE_URL = BuildConfig.HOST + BuildConfig.PORT + BuildConfig.SIMPLE_WEATHER;   // 简要天气信息
    public static final String WEATHER_BASE_URL = BuildConfig.HOST + BuildConfig.PORT + BuildConfig.WEATHER;            // 详细天气
    public static final String PM2D5_BASE_URL = BuildConfig.HOST + BuildConfig.PORT + BuildConfig.AIRCONDITION;         //空气信息
    public static final String AIRBALL_BASE_URL = BuildConfig.HOST + BuildConfig.PORT + BuildConfig.AIRBALL;            //空气果信息

    private static final String WEATHER_INFO_FILENAME = "_weather.json";
    private static final String SIMPLE_WEATHER_INFO_FILENAME = "_simple_weather.json";
    private static final String PM2D5_INFO_FILENAME = "_pm2d5.json";
    private static final int LOACTION_OK = 0;
    private static final int ON_NEW_INTENT = 1;
    private static final int UPDATE_EXISTS_CITY = 2;
    private static final int GET_WEATHER_RESULT = 3;
    private static final int MAkINGTOAST = 4;
    private LocationClient mLocationClient;
    private CityDB mCityDB;
    private SharePreferenceUtil mSpUtil;
    private Application mApplication;
    private Context context;
    private City mCurCity;
    private Weatherinfo mCurWeatherinfo;
    private SimpleWeatherinfo mCurSimpleWeatherinfo;
    private Pm2d5 mCurPm2d5;
    private AirBall mCurAirBall;
    private Gson mGson;
    private ImageView mCityManagerBtn, mUpdateBtn, mSetting, mShareBtn;
    private ProgressBar mUpdateProgressBar;
    private TextView mTitleTextView;
    private RelativeLayout rl;
    private LinearLayout r2;
    private City mNewIntentCity;
    private WeatherPagerAdapter mWeatherPagerAdapter;
    private Thread weatherUpdate, autoControl, settingTime, thUpdPm25, thUpdAirball;

    private GifImageView gifImageView;

    private TextView cityTv, timeTv, humidityTv, weekTv, pmDataTv, pmQualityTv,
            temperatureTv, climateTv, windTv, tv11, tv22, alert_tv1, alert_tv2;
    private ImageView weatherImg, pmImg;
    ;
    private ViewPager mViewPager;
    private List<Fragment> fragments;
    static int alert_textview_index = -1;
    private HashMap hm = new HashMap();
    private static int mfactory = 1;
    private static boolean autoCheckAir = true;


    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case LOACTION_OK:
                    String cityName = (String) msg.obj;
                    L.i("cityName = " + cityName);
                    mCurCity = mCityDB.getCity(cityName);
                    L.i(mCurCity.toString());
                    mSpUtil.setCity(mCurCity.getCity());
                    cityTv.setText(mCurCity.getCity());
                    updateWeather(true);
                    break;
                case ON_NEW_INTENT:
                    mCurCity = mNewIntentCity;
                    mSpUtil.setCity(mCurCity.getCity());
                    cityTv.setText(mCurCity.getCity());
                    updateWeather(true);
                    break;
                case UPDATE_EXISTS_CITY:
                    String sPCityName = mSpUtil.getCity();
                    mCurCity = mCityDB.getCity(sPCityName);
                    updateWeather(false);
                    break;
                case GET_WEATHER_RESULT:
                    updateWeatherInfo();
                    updatePm2d5Info();
                    updateWidgetWeather();
                    mUpdateBtn.setVisibility(View.VISIBLE);
                    mUpdateProgressBar.setVisibility(View.GONE);
                    break;
                case MAkINGTOAST:
                    if (msg.arg1 == 0) {
                        if (msg.arg2 == 0) {
                            T.show(getApplicationContext(), "除霾机关闭未成功", Toast.LENGTH_SHORT);

                        } else {
                            T.show(getApplicationContext(), "除霾机已关闭", Toast.LENGTH_SHORT);

                        }
                    } else if (msg.arg1 == 1) {
                        if (msg.arg2 == 0) {
                            T.show(getApplicationContext(), "除霾机开启未成功", Toast.LENGTH_SHORT);

                        } else {
                            T.show(getApplicationContext(), "除霾机已开启", Toast.LENGTH_SHORT);
                        }
                    } else {
                        T.show(getApplicationContext(), "过热保护开启，除霾机已关闭", Toast.LENGTH_SHORT);
                    }
                    break;
                default:
                    break;
            }
        }

    };

    private void updateWidgetWeather() {
        sendBroadcast(new Intent(UPDATE_WIDGET_WEATHER_ACTION));
    }


    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = getApplicationContext();
        initData();
        initView();
        weatherUpdate.start();  //开启自动更新天气线程
        try {
            weatherUpdate.join(800); //等待数据获取
        } catch (Exception e) {
            Log.v("Thread error", "Join failure");
        }
        autoControl.start();    //开启检测室内空气并自动开关除霾设备线程
        settingTime.start();    //开启定时线程

    }

    private void startActivityForResult() {
        Intent i = new Intent(this, SelectCtiyActivity.class);
        startActivityForResult(i, 0);
    }

    private void initView() {
        mCityManagerBtn = (ImageView) findViewById(R.id.title_city_manager);
        mUpdateBtn = (ImageView) findViewById(R.id.title_update_btn);
        mShareBtn = (ImageView) findViewById(R.id.title_share);
        mSetting = (ImageView) findViewById(R.id.title_setting);
        rl = (RelativeLayout) findViewById(R.id.weather_content);
        r2 = (LinearLayout) findViewById(R.id.pm2_5_content);
        mCityManagerBtn.setOnClickListener(this);
        mUpdateBtn.setOnClickListener(this);
        mShareBtn.setOnClickListener(this);
        mSetting.setOnClickListener(this);
        rl.setOnClickListener(this);
        r2.setOnClickListener(this);
        mShareBtn.setVisibility(View.GONE);
        mUpdateProgressBar = (ProgressBar) findViewById(R.id.title_update_progress);
        mTitleTextView = (TextView) findViewById(R.id.title_city_name);

        cityTv = (TextView) findViewById(R.id.city);
        timeTv = (TextView) findViewById(R.id.time);
        tv11 = (TextView) findViewById(R.id.tv11);
        tv22 = (TextView) findViewById(R.id.tv22);
        alert_tv1 = (TextView) findViewById(R.id.alert_tv1);
        alert_tv2 = (TextView) findViewById(R.id.alert_tv2);
        alert_tv1.setOnClickListener(this);
        alert_tv2.setOnClickListener(this);
        timeTv.setText(TimeUtil.getDay(mSpUtil.getTimeSamp())
                + mSpUtil.getTime() + "发布");
        humidityTv = (TextView) findViewById(R.id.humidity);
        weekTv = (TextView) findViewById(R.id.week_today);
        weekTv.setText("今天 " + TimeUtil.getWeek(0, TimeUtil.XING_QI));
        pmDataTv = (TextView) findViewById(R.id.pm_data);
        pmQualityTv = (TextView) findViewById(R.id.pm2_5_quality);
        pmImg = (ImageView) findViewById(R.id.pm2_5_img);
        temperatureTv = (TextView) findViewById(R.id.temperature);
        climateTv = (TextView) findViewById(R.id.climate);
        windTv = (TextView) findViewById(R.id.wind);
        weatherImg = (ImageView) findViewById(R.id.weather_img);
        fragments = new ArrayList<Fragment>();
        fragments.add(new FirstWeatherFragment());
        fragments.add(new SecondWeatherFragment());
        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        mWeatherPagerAdapter = new WeatherPagerAdapter(
                getSupportFragmentManager(), fragments);
        mViewPager.setAdapter(mWeatherPagerAdapter);
        ((CirclePageIndicator) findViewById(R.id.indicator))
                .setViewPager(mViewPager);
        if (TextUtils.isEmpty(mSpUtil.getCity())) {
            if (NetUtil.getNetworkState(this) != NetUtil.NETWORN_NONE) {
                mLocationClient.start();
                mLocationClient.requestLocation();
                T.showShort(this, "正在定位...");
                mUpdateBtn.setVisibility(View.GONE);
                mUpdateProgressBar.setVisibility(View.VISIBLE);
            } else {
                T.showShort(this, R.string.net_err);
            }
        } else {
            mHandler.sendEmptyMessage(UPDATE_EXISTS_CITY);
        }

//        /**
//         * GIF动态天气背景
//         */
//        gifImageView = (GifImageView) findViewById(R.id.gifImageView);
//        InputStream inputStream = null;
//        try {
//            inputStream = this.getAssets().open("shenzhengif.gif");
//            gifImageView.setBytes(IOUtils.toByteArray(inputStream));
//            gifImageView.startAnimation();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

    }

    private void initData() {
        Application.mListeners.add(this);
        mApplication = Application.getInstance();
        mSpUtil = mApplication.getSharePreferenceUtil();
        mLocationClient = mApplication.getLocationClient();

        mLocationClient.registerLocationListener(mLocationListener);
        mCityDB = mApplication.getCityDB();
        // 不转换没有 @Expose 注解的字段
        mGson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation()
                .create();
        /**
         * 天气更新服务线程
         */
        weatherUpdate = new Thread() {
            @Override
            public void run() {
                super.run();
                while (true) {
                    try {
                        int dely = 300 - mfactory;  //随着时间波动变小,避免同时访问服务器
                        if (mfactory < 300) {
                            mfactory += 5;
                        }
                        while (dely < 1) {
                            dely++;
                        }
                        int intervalTime = 30 * 60 + new Random().nextInt(dely);
                        getWeatherInfo(true);
                        getSimpleWeatherInfo(true);
                        getPm2d5Info(true);
                        getAirBallInfo(true);
                        mHandler.sendEmptyMessage(GET_WEATHER_RESULT);
                        Thread.sleep(intervalTime * 1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };


        /**
         * 自动检测空气并开启除霾设备线程
         */
        autoControl = new Thread() {
            @Override
            public void run() {
                super.run();
                while (true) {
                    try {
                        final SharedPreferences sharedPreferences = getSharedPreferences("controlSetting", Context.MODE_WORLD_WRITEABLE);
                        autoCheckAir = sharedPreferences.getBoolean("autoControl", true);
                        int timeInterval = Integer.valueOf(sharedPreferences.getString("time", 2 + ""));

                        try {
                            mCurPm2d5.getPm2_5_24h();
                            mCurAirBall.getPm25();
                        } catch (NullPointerException e) {
                            getPm2d5Info(true);
                            getAirBallInfo(true);
                            Log.v("NullPointException", "retrieve air data");
                        }

                        int outDoorPm25 = Integer.parseInt(mCurPm2d5.getPm2_5_24h());   //获取室外PM2.5数据
                        int pm25 = mCurAirBall.getPm25();                               //获取室内PM2.5数据
                        int result = -1;                                                //除霾机是否开关成功
                        if (autoCheckAir) {
                            Log.v("kk", "dddd");
                            if (mCurAirBall.getPm25() != 0) {                                  //有室内空气果
                                int status = new Json_Operation().getAirFilterStatus(); //获取除霾机当前开关状态
                                Message meg = new Message();
                                meg.what = MAkINGTOAST;
                                if (pm25 > 50 && status == 0) {
                                    result = new Json_Operation().controlAirFliter("1"); //开启除霾设备
                                    meg.arg1 = 1;
                                } else {
                                    result = new Json_Operation().controlAirFliter("0"); //关闭除霾设备
                                    meg.arg1 = 0;
                                }
                                meg.arg2 = result;
                                mHandler.sendMessage(meg);
                            } else {
                                Log.v("autoControl", "no indoors air data");
                            }
                        }
                        if (outDoorPm25 < pm25 * 2) {                              //当室内pm2.5浓度大于室外浓度的1/2时，提示用户可能没关窗户
                            Toast.makeText(context, "根据室内外PM2.5浓度对比，您可能没关窗", Toast.LENGTH_SHORT).show();
                        }
                        sleep(timeInterval * 60 * 60 * 1000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        /**
         * 定时除霾机关闭的时间
         */
        settingTime = new Thread() {
            @Override
            public void run() {
                super.run();
                final SharedPreferences sharedPreferences = getSharedPreferences("controlSetting", Context.MODE_WORLD_WRITEABLE);
                while (true) {                               //此处应该加上判断Dialog中开关有没有打开的变量
                    autoCheckAir = sharedPreferences.getBoolean("autoControl", true);
                    int timeInterval = Integer.valueOf(sharedPreferences.getString("time", 2 + ""));
                    boolean autoShutDown = sharedPreferences.getBoolean("heartProtected", true);
                    if (autoShutDown && timeInterval > 12) {  //过热保护
                        timeInterval = 12;
                    }
                    if (!autoCheckAir) {
                        try {
                            sleep(timeInterval * 60 * 60 * 1000);
                            Message meg = new Message();
                            int result = new Json_Operation().controlAirFliter("0"); //关闭除霾设备
                            if (timeInterval != 12) {
                                meg.arg1 = 0;
                                meg.arg2 = result;
                            } else {
                                meg.arg1 = 3;
                            }
                            mHandler.sendMessage(meg);  //发TOAST
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    try {
                        sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            }
        };

    }

    private void updateWeather(final boolean isRefresh) {
        if (NetUtil.getNetworkState(this) == NetUtil.NETWORN_NONE && isRefresh) {
            T.showLong(this, R.string.net_err);
            return;
        }
        if (mCurCity == null) {
            T.showLong(mApplication, "未找到此城市,请重新定位或选择...");
            return;
        }
        // T.showShort(this, "正在刷新天气...");
        timeTv.setText("同步中...");
        mTitleTextView.setText(mCurCity.getCity() + "天气");
        mUpdateBtn.setVisibility(View.GONE);
        mUpdateProgressBar.setVisibility(View.VISIBLE);
        // 启动线程获取天气信息
        new Thread() {
            @Override
            public void run() {
                super.run();
                getWeatherInfo(isRefresh);
                getSimpleWeatherInfo(isRefresh);
                getPm2d5Info(isRefresh);
                getAirBallInfo(isRefresh);

                if (mCurSimpleWeatherinfo != null)
                    L.i(mCurSimpleWeatherinfo.toString());
                if (mCurWeatherinfo != null)
                    L.i(mCurWeatherinfo.toString());
                if (mCurPm2d5 != null)
                    L.i(mCurPm2d5.toString());
                if (mCurAirBall != null)
                    L.i(mCurAirBall.toString());
                mHandler.sendEmptyMessage(GET_WEATHER_RESULT);
            }

        }.start();
    }


    private void getWeatherInfo(boolean isRefresh) {
        String url = WEATHER_BASE_URL;
        String result;
        String weatherResult = connServerForResult(url);
        L.i("获得Weather信息————》》》》》》》》》》", weatherResult);


        weatherResult = convetToUTF(weatherResult);

        if (TextUtils.isEmpty(weatherResult))
            weatherResult = getInfoFromFile(WEATHER_INFO_FILENAME);
        parseWeatherInfo(url, weatherResult, true);
    }

    private void getSimpleWeatherInfo(boolean isRefresh) {
        String url = WEATHER_SIMPLE_URL;
        String weatherResult = connServerForResult(url);
        L.i("获得SimpleWeather信息————》》》》》》》", weatherResult);
        if (TextUtils.isEmpty(weatherResult))
            weatherResult = getInfoFromFile(SIMPLE_WEATHER_INFO_FILENAME);
        weatherResult = convetToUTF(weatherResult);
//        weatherResult="{\"weatherinfo\":{\"city\":\"深圳\",\"temp\":\"22\",\"SD\":\"81%\",\"time\":\"16:55\"}}";
        parseSimpleWeatherInfo(url, weatherResult, true);
    }

    private void getPm2d5Info(boolean isRefresh) {
//        String urlPm2d5 = PM2D5_BASE_URL.replace("SHENZHEN",
//                mCurCity.getAllPY().toLowerCase());
        String urlPm2d5 = PM2D5_BASE_URL;


        String pmResult = connServerForResult(urlPm2d5);


        pmResult = convetToUTF(pmResult);

        L.i("获得PM2.5信息————》》》》》》》》》》", pmResult);
        if (TextUtils.isEmpty(pmResult) || pmResult.contains("error")) {// 如果获取失败，则取本地文件中的信息，
            String fileResult = getInfoFromFile(PM2D5_INFO_FILENAME);
            // 只有当本地文件信息与当前城市相匹配时才使用
            if (!TextUtils.isEmpty(fileResult)
                    && fileResult.contains(mCurCity.getCity()))
                pmResult = fileResult;
        }
//		pmResult = getInfoFromFile(PM2D5_INFO_FILENAME);
//        pmResult = "[{\"aqi\":69,\"suggest\":\"极少数敏感人群应减少户外运动\",\"quality\":\"良\",\"pm2_5_24h\":46}]";
//  [{"aqi": 151,"pm2_5_24h": 115,"quality": "中度污染","suggest":"极少数人应该"}]

        parsePm2d5Info(urlPm2d5, pmResult, true);
    }


    private void getAirBallInfo(boolean isRefresh) {

        String urlAirBall = AIRBALL_BASE_URL;

        String pmResult = connServerForResult(urlAirBall);

        pmResult = convetToUTF(pmResult);

        L.i("获得空气果信息————》》》》》》》》》》", pmResult);

//		pmResult = getInfoFromFile(PM2D5_INFO_FILENAME);
//        pmResult = "[{\"aqi\":69,\"suggest\":\"极少数敏感人群应减少户外运动\",\"quality\":\"良\",\"pm2_5_24h\":46}]";
//  [{"aqi": 151,"pm2_5_24h": 115,"quality": "中度污染","suggest":"极少数人应该"}]

        parseAirBallInfo(urlAirBall, pmResult, true);
    }



    private void parseWeatherInfo(String url, String result,
                                  boolean isRefreshWeather) {
        mCurWeatherinfo = null;
        mApplication.setmCurWeatherinfo(null);
        if (!TextUtils.isEmpty(result) && !result.contains("页面没有找到")) {
            // L.i(result);
            Weather weather = mGson.fromJson(result, Weather.class);
            mCurWeatherinfo = weather.getWeatherinfo();
            // L.i(mCurWeatherinfo.toString());
        } else {
            result = "";
        }
        if (isRefreshWeather && !TextUtils.isEmpty(result))
            // save2File(result, WEATHER_INFO_FILENAME);
            ConfigCache.setUrlCache(result, url);
    }

    private void parseSimpleWeatherInfo(String url, String result,
                                        boolean isRefreshWeather) {
        mCurSimpleWeatherinfo = null;
        mApplication.setCurSimpleWeatherinfo(null);
        if (!TextUtils.isEmpty(result) && !result.contains("页面没有找到")) {
            // L.i(result);
            SimpleWeather weather = mGson.fromJson(result, SimpleWeather.class);
            mCurSimpleWeatherinfo = weather.getWeatherinfo();
            // L.i(mCurSimpleWeatherinfo.toString());
            mApplication.setCurSimpleWeatherinfo(mCurSimpleWeatherinfo);
        } else {
            result = "";
        }
        if (isRefreshWeather && !TextUtils.isEmpty(result))
            // save2File(result, SIMPLE_WEATHER_INFO_FILENAME);
            ConfigCache.setUrlCache(result, url);
    }

    /**
     * 解析pm2.5的Json返回值
     *
     * @param url            返回pm2.5数据的网址，便于以后缓存数据
     * @param result         返回的结果
     * @param isRefreshPm2d5 是否重新从网页获取
     */
    private void parsePm2d5Info(String url, String result,
                                boolean isRefreshPm2d5) {
        mCurPm2d5 = null;
        mApplication.setmCurWeatherinfo(null);
        if (!TextUtils.isEmpty(result) && !result.contains("error")) {
            List<Pm2d5> pm2d5s = mGson.fromJson(result,
                    new TypeToken<List<Pm2d5>>() {
                    }.getType()
            );
            mCurPm2d5 = pm2d5s.get(0);
        } else {
            result = "";
        }
        if (isRefreshPm2d5 && !TextUtils.isEmpty(result))
            // save2File(result, PM2D5_INFO_FILENAME);
            ConfigCache.setUrlCache(result, url);
    }


    private void parseAirBallInfo(String url, String result,
                                  boolean isRefreshAirBall) {
        mCurAirBall = null;
        if (!TextUtils.isEmpty(result) && !result.contains("error")) {
            List<AirBall> mlistAirBall = mGson.fromJson(result,
                    new TypeToken<List<AirBall>>() {
                    }.getType()
            );
            mCurAirBall = mlistAirBall.get(mlistAirBall.size() - 1);
        } else {
            result = "";
        }
        if (isRefreshAirBall && !TextUtils.isEmpty(result))
            // save2File(result, PM2D5_INFO_FILENAME);
            ConfigCache.setUrlCache(result, url);
    }


    /**
     * 把信息保存到文件中
     *
     * @param result   要存储的结果
     * @param fileName 文件名
     * @return 是否成功
     */
    private boolean save2File(String result, String fileName) {
        try {
            FileOutputStream fos = MainActivity.this.openFileOutput(fileName,
                    MODE_PRIVATE);
            fos.write(result.toString().getBytes());
            fos.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 从文件中获取信息
     *
     * @param fileName
     * @return
     */
    private String getInfoFromFile(String fileName) {
        String result = "";
        try {
            FileInputStream fis = openFileInput(fileName);
            byte[] buffer = new byte[fis.available()];// 本地文件可以实例化buffer，网络文件不可行
            fis.read(buffer);
            result = new String(buffer);
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 更新天气界面
     */
    private void updateWeatherInfo() {
        if (mCurWeatherinfo != null) {
            mApplication.setmCurWeatherinfo(mCurWeatherinfo);// 保存到全局变量中
            temperatureTv.setText(mCurWeatherinfo.getTemp1());
            cityTv.setText(mCurWeatherinfo.getCity());
            tv11.setText(mCurPm2d5.getSuggest());
            tv22.setText(mCurWeatherinfo.getIndex48_d());
            String wind = mCurWeatherinfo.getWind1();
            if (wind.contains("转")) {
                String[] strs = wind.split("转");
                wind = strs[0];
            }
            windTv.setText(wind);
            String climate = mCurWeatherinfo.getWeather1();
            climateTv.setText(climate);
            mSpUtil.setSimpleClimate(climate);
            String[] strs = {"晴", "晴"};
            if (climate.contains("转")) {// 天气带转字，取前面那部分
                strs = climate.split("转");
                climate = strs[0];
                if (climate.contains("到")) {// 如果转字前面那部分带到字，则取它的后部分
                    strs = climate.split("到");
                    climate = strs[1];
                }
            }
            L.i("处理后的天气为：" + climate);
            if (mApplication.getWeatherIconMap().containsKey(climate)) {
                int iconRes = mApplication.getWeatherIconMap().get(climate);
                weatherImg.setImageResource(iconRes);
            } else {
                // do nothing 没有这样的天气图片

            }
            if (mCurSimpleWeatherinfo != null) {
                String alertinfo = mCurSimpleWeatherinfo.getAlertinfo();
                String level = mCurSimpleWeatherinfo.getLevel();
                String desc = mCurSimpleWeatherinfo.getDescription();
                int len = level.length();
                if (len != 0) {
                    String[] alertInfoStrs = {};
                    String[] alertLevelStrs = {};
                    String[] alertDesc = {};

                    alertInfoStrs = alertinfo.split("@");
                    alertLevelStrs = level.split("@");
                    alertDesc = desc.split("@");


                    int strslen = alertInfoStrs.length;
                    for (int i = 0; i < strslen; i++) {
                        hm.put(alertInfoStrs[i], alertDesc[i]);
                    }
                    Log.v("Amount", String.valueOf(strslen));

                    if (alert_textview_index < strslen && strslen > 1) {

                        alert_tv1.setText(alertInfoStrs[(++alert_textview_index) % strslen]);
                        Log.v("colortext", alertInfoStrs[(alert_textview_index) % strslen]);
                        alert_tv1.setTextColor(getColor(alertInfoStrs[(alert_textview_index) % strslen]));

                        alert_tv2.setText(alertInfoStrs[(++alert_textview_index) % strslen]);

                        alert_tv2.setTextColor(getColor(alertInfoStrs[(alert_textview_index % strslen)]));

                    } else {
                        alert_textview_index = -1;
                        alert_tv1.setText(alertInfoStrs[0]);
                        alert_tv1.setTextColor(getColor(alertInfoStrs[0]));
                    }
                    Log.v("static_i", String.valueOf(alert_textview_index));
                } else {
                    alert_tv1.setText("");
                    alert_tv2.setText("");
                }


                if (!mCurSimpleWeatherinfo.getTime().equals(mSpUtil.getTime())) {
                    mSpUtil.setTime(mCurSimpleWeatherinfo.getTime());
                    mSpUtil.setTimeSamp(System.currentTimeMillis());// 保存一下更新的时间戳
                }
                mSpUtil.setSimpleTemp(mCurSimpleWeatherinfo.getTemp());
                timeTv.setText(TimeUtil.getDay(mSpUtil.getTimeSamp())
                        + mCurSimpleWeatherinfo.getTime() + "发布");
                humidityTv.setText("湿度:" + mCurSimpleWeatherinfo.getSD());
            }
            if (fragments.size() > 0) {
                ((FirstWeatherFragment) mWeatherPagerAdapter.getItem(0))
                        .updateWeather(mCurWeatherinfo);
                ((SecondWeatherFragment) mWeatherPagerAdapter.getItem(1))
                        .updateWeather(mCurWeatherinfo);
            }
        } else {
            temperatureTv.setText("N/A");
            cityTv.setText(mCurCity.getCity());
            windTv.setText("N/A");
            climateTv.setText("N/A");
            weatherImg.setImageResource(R.drawable.biz_plugin_weather_qing);
            tv11.setText("N/A");
            tv22.setText("N/A");
            T.showLong(mApplication, "获取天气信息失败");
        }
    }

    /**
     * 更新pm2.5界面
     */
    private void updatePm2d5Info() {
        if (mCurPm2d5 != null) {
            mApplication.setmCurPm2d5(mCurPm2d5);
            pmQualityTv.setText(mCurPm2d5.getQuality());
            pmDataTv.setText(mCurPm2d5.getPm2_5_24h());
            int pm2_5 = Integer.parseInt(mCurPm2d5.getPm2_5_24h());
            int pm_img = R.drawable.biz_plugin_weather_0_50;
            if (pm2_5 > 300) {
                pm_img = R.drawable.biz_plugin_weather_greater_300;
            } else if (pm2_5 > 200) {
                pm_img = R.drawable.biz_plugin_weather_201_300;
            } else if (pm2_5 > 150) {
                pm_img = R.drawable.biz_plugin_weather_151_200;
            } else if (pm2_5 > 100) {
                pm_img = R.drawable.biz_plugin_weather_101_150;
            } else if (pm2_5 > 50) {
                pm_img = R.drawable.biz_plugin_weather_51_100;
            } else {
                pm_img = R.drawable.biz_plugin_weather_0_50;
            }

            pmImg.setImageResource(pm_img);
        } else {
            pmQualityTv.setText("N/A");
            pmDataTv.setText("N/A");
            pmImg.setImageResource(R.drawable.biz_plugin_weather_0_50);
            T.showLong(mApplication, "未获取到PM2.5数据");
        }
    }

    /**
     * 请求服务器，获取返回数据
     */
    private String connServerForResult(String url) {
        HttpGet httpRequest = new HttpGet(url);
        String strResult = "";
        if (NetUtil.getNetworkState(this) != NetUtil.NETWORN_NONE) {
            try {
                // HttpClient对象
                HttpClient httpClient = new DefaultHttpClient();
                // 获得HttpResponse对象
                HttpResponse httpResponse = httpClient.execute(httpRequest);
                if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK)
                    // 取得返回的数据
                    strResult = EntityUtils.toString(httpResponse.getEntity());

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return strResult; // 返回结果
    }

    /**
     * 将解析到到的iso8859-1字符串转为utf-8
     */
    private static String convetToUTF(String str) {
        String strUTF = null;
        try {
            strUTF = new String(str.getBytes("iso8859-1"), "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return strUTF;
    }

    /**
     * 获得预警信息的颜色等级
     */
    private static int getColor(String alert) {
        if (alert.contains("红色")) {
            return Color.RED;
        }
        if (alert.contains("黄色")) {
            return Color.YELLOW;
        }
        if (alert.contains("橙色")) {
            return Color.rgb(255, 97, 0);
        }
        if (alert.contains("蓝色")) {
            return Color.rgb(58, 95, 205);
        }
        return Color.WHITE;
    }

    /**
     * 百度定位监听器
     */
    BDLocationListener mLocationListener = new BDLocationListener() {

        @Override
        public void onReceivePoi(BDLocation arg0) {
            // do nothing
        }

        /**
         * 获取当地位置的回调函数
         * @param location 位置信息的对象
         */
        @Override
        public void onReceiveLocation(BDLocation location) {
            // mActionBar.setProgressBarVisibility(View.GONE);
            mUpdateBtn.setVisibility(View.VISIBLE);
            mUpdateProgressBar.setVisibility(View.GONE);
            if (location == null || TextUtils.isEmpty(location.getCity())) {
                // T.showShort(getApplicationContext(), "location = null");
                final Dialog dialog = IphoneDialog.getTwoBtnDialog(
                        MainActivity.this, "定位失败", "是否手动选择城市?");
                ((Button) dialog.findViewById(R.id.ok))
                        .setOnClickListener(new OnClickListener() {

                            @Override
                            public void onClick(View v) {
                                startActivityForResult();
                                dialog.dismiss();
                            }
                        });
                dialog.show();
                return;
            }
            String cityName = location.getCity();
            mLocationClient.stop();
            Message msg = mHandler.obtainMessage();
            msg.what = LOACTION_OK;
            msg.obj = cityName;
            mHandler.sendMessage(msg);// 更新天气
        }
    };

    /**
     * 返回城市选择界面选择的城市
     *
     * @param requestCode 请求码
     * @param resultCode  返回码
     * @param data        返回的对象
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0 && resultCode == RESULT_OK) {
            mNewIntentCity = (City) data.getSerializableExtra("city");
            mHandler.sendEmptyMessage(ON_NEW_INTENT);
        }
    }

    @Override
    public void onCityComplite() {
        // do nothing
    }

    /**
     * 网络状态改变弹出提示框
     */
    @Override
    public void onNetChange() {
        if (NetUtil.getNetworkState(this) == NetUtil.NETWORN_NONE)
            T.showLong(this, R.string.net_err);
        // else if (!TextUtils.isEmpty(mSpUtil.getCity())) {
        // String sPCityName = mSpUtil.getCity();
        // mCurCity = mCityDB.getCity(sPCityName);
        // getWeatherInfo(true, true);
        // }
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.title_city_manager:
                startActivityForResult();
                break;
            case R.id.title_setting:
                final SharedPreferences sharedPreferences = getSharedPreferences("controlSetting", Context.MODE_WORLD_WRITEABLE);
                final SharedPreferences.Editor editor = sharedPreferences.edit();
                final Dialog dialog = new Dialog(this);
                dialog.setContentView(R.layout.alert_setting);
                dialog.setTitle("除霾新风控制设置");
                dialog.setCanceledOnTouchOutside(false);

                Window dialogWindow = dialog.getWindow();
                WindowManager.LayoutParams lp = dialogWindow.getAttributes();
                dialogWindow.setGravity(Gravity.CENTER);
                lp.width = 800; // 宽度
                lp.height = 450; // 高度
                lp.alpha = 0.9f; // 透明度
                dialogWindow.setAttributes(lp);
                dialog.show();

                final CheckBox setting_cb = (CheckBox) dialog.findViewById(R.id.setting_cb);
                final EditText setting_et = (EditText) dialog.findViewById(R.id.setting_et);
                final Switch setting_sw = (Switch) dialog.findViewById(R.id.setting_sw);
                final TextView setting_tv = (TextView) dialog.findViewById(R.id.setting_tv);
                final TextView setting_tv2 = (TextView) dialog.findViewById(R.id.setting_tv2);
                final CheckBox setting_hp = (CheckBox) dialog.findViewById(R.id.setting_hp);
                Button setting_bt_cl = (Button) dialog.findViewById(R.id.setting_bt_cl);
                Button setting_bt_ok = (Button) dialog.findViewById(R.id.setting_bt_ok);


                setting_cb.setChecked(sharedPreferences.getBoolean("autoControl", true));
                setting_et.setText(sharedPreferences.getString("time", 2 + ""));
                setting_sw.setChecked(sharedPreferences.getBoolean("manualControl", false));
                setting_hp.setChecked(sharedPreferences.getBoolean("heartProtected", true));

                setting_et.setSelection(setting_et.length());

                if (setting_cb.isChecked()) {
                    setting_tv.setTextColor(Color.DKGRAY);
                    setting_tv2.setTextColor(Color.DKGRAY);
                    setting_sw.setEnabled(false);
                    setting_et.setEnabled(false);
                    setting_tv2.setEnabled(false);
                }

                setting_cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            setting_tv.setTextColor(Color.DKGRAY);
                            setting_tv2.setTextColor(Color.DKGRAY);
                            setting_et.setEnabled(false);
                            setting_tv2.setEnabled(false);
                            setting_sw.setEnabled(false);
                        } else {
                            setting_tv.setTextColor(Color.WHITE);
                            setting_tv2.setTextColor(Color.WHITE);
                            setting_sw.setEnabled(true);
                            setting_et.setEnabled(true);
                            setting_tv2.setEnabled(true);
                            suggestAlertDiaglog();
                        }
                    }

                    private void suggestAlertDiaglog() {
                        int outDoorPm25 = Integer.parseInt(mCurPm2d5.getPm2_5_24h());
                        int guessIndoorPm25 = Integer.parseInt(mCurPm2d5.getPm2_5_24h()) / 2;
                        if (outDoorPm25 > 100) {
                            guessIndoorPm25 = Integer.parseInt(mCurPm2d5.getPm2_5_24h()) / 3;
                        }
                        int time = (int) Math.ceil((float) guessIndoorPm25 / 30);
                        new AlertDialog.Builder(MainActivity.this)
                                .setTitle("建议开启时间")
                                .setMessage("您没有安装室内空气果，根据室外空气数据的估计结果建议除霾机开启" + time + "小时")
                                .show();
                        setting_et.setText(time + "");
                    }


                });

                setting_bt_cl.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                setting_bt_ok.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        editor.putBoolean("autoControl", setting_cb.isChecked());
                        editor.putString("time", setting_et.getText().toString());
                        editor.putBoolean("manualControl", setting_sw.isChecked());
                        editor.putBoolean("heartProtected", setting_hp.isChecked());
                        editor.commit();//提交修改
                        Toast.makeText(getApplicationContext(), "设置已保存", Toast.LENGTH_LONG).show();
                        dialog.dismiss();
                    }
                });
                setting_sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            int rul1 = new Json_Operation().controlAirFliter("1"); //开启除霾设备
                            if (rul1 == 1) {
                                Toast.makeText(getApplicationContext(), "新风除霾系统已打开", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getApplicationContext(), "新风除霾系统打开失败", Toast.LENGTH_SHORT).show();
                                setting_sw.setChecked(false);
                            }
                        } else {
                            int rul2 = new Json_Operation().controlAirFliter("0"); //关闭除霾设备
                            if (rul2 == 1) {
                                Toast.makeText(getApplicationContext(), "新风除霾系统已关闭", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getApplicationContext(), "新风除霾系统关闭失败", Toast.LENGTH_SHORT).show();
                                setting_sw.setChecked(true);
                            }
                        }
                    }
                });
                break;
            case R.id.title_share:

                if (NetUtil.getNetworkState(this) != NetUtil.NETWORN_NONE) {
                    // do something
                } else {
                    T.showShort(this, R.string.net_err);
                }
                break;
            case R.id.title_update_btn:
                if (NetUtil.getNetworkState(this) != NetUtil.NETWORN_NONE) {
                    if (TextUtils.isEmpty(mSpUtil.getCity())) {
                        T.showShort(this, "请先选择城市或定位！");
                    } else {
                        String sPCityName = mSpUtil.getCity();
                        mCurCity = mCityDB.getCity(sPCityName);
                        updateWeather(true);
                    }
                } else {
                    T.showShort(this, R.string.net_err);
                }
                break;
            //TODO  open temputerChart
            case R.id.weather_content:
                Intent i = new Intent(getApplicationContext(), WeathreChartActivity.class);
                startActivity(i);
                break;
            //TODO  open pm2.5Chart
            case R.id.pm2_5_content:
                Intent i2 = new Intent(getApplicationContext(), Pm25ChartActivity.class);
                startActivity(i2);
                break;
            case R.id.alert_tv1:
                if (!hm.isEmpty()) {
                    String desc = hm.get(alert_tv1.getText()).toString();
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("预警信息详情")
                            .setMessage(desc)
                            .show();
                }
                break;
            case R.id.alert_tv2:
                String desc = hm.get(alert_tv2.getText()).toString();
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("预警信息详情")
                        .setMessage(desc)
                        .show();
                break;
            default:
                break;
        }
    }

}
