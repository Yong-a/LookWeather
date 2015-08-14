package com.lookweather.app.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lookweather.app.R;
import com.lookweather.app.service.AutoUpdateService;
import com.lookweather.app.util.HttpCallbackListener;
import com.lookweather.app.util.HttpUtil;
import com.lookweather.app.util.Utility;

import java.util.HashMap;
import java.util.Map;

/**
 * 项目名称：LookWeather
 * 类描述：显示天气Activity
 * 创建人：Yong_a
 * 创建时间：2015/8/11 21:55
 * 修改人：Yong-a
 * 修改时间：2015/8/11 21:55
 * 修改备注：修改文档注释
 */
public class WeatherActivity extends Activity implements View.OnClickListener {
    private LinearLayout weatherInfoLayout;
    /**
     * 用于显示城市名
     */
    private TextView cityNameText;
    /**
     * 用于显示发布时间
     */
    private TextView publishText;
    /**
     * 用于显示天气描述信息
     */
    private TextView weatherDespText;
    /**
     * 用于显示气温1
     */
    private TextView temp1Text;
    /**
     * 用于显示气温2
     */
    private TextView temp2Text;
    /**
     * 用于显示当前日期
     */
    private TextView currentDateText;
    /**
     * 切换城市按钮
     */
    private Button switchCity;
    /**
     * 更新天气按钮
     */
    private Button refreshWeather;
    /**
     * 菜单按钮
     */
    private Button menu;
    /**
     * 背景图片
     */
    private View weatherBg;
    /**
     * 设置dialog布局
     */
    private EditText ed_pl;
    private Button ok;
    private Button cancel;
    private AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.weather_layout);
        // 初始化各控件
        weatherInfoLayout = (LinearLayout) findViewById(R.id.weather_info_layout);
        cityNameText = (TextView) findViewById(R.id.city_name);
        publishText = (TextView) findViewById(R.id.publish_text);
        weatherDespText = (TextView) findViewById(R.id.weather_desp);
        temp1Text = (TextView) findViewById(R.id.temp1);
        temp2Text = (TextView) findViewById(R.id.temp2);
        currentDateText = (TextView) findViewById(R.id.current_date);
        String countyCode = getIntent().getStringExtra("county_code");
        if (!TextUtils.isEmpty(countyCode)) {
            // 有县级代号时就去查询天气
            publishText.setText("同步中...");
            weatherInfoLayout.setVisibility(View.INVISIBLE);
            cityNameText.setVisibility(View.INVISIBLE);
            queryWeatherCode(countyCode);
        } else {
            // 没有县级代号时就直接显示本地天气
            showWeather();
        }
        switchCity = (Button) findViewById(R.id.switch_city);
        refreshWeather = (Button) findViewById(R.id.refresh_weather);
        menu = (Button) findViewById(R.id.menu);
        switchCity.setOnClickListener(this);
        refreshWeather.setOnClickListener(this);
        menu.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.switch_city:
                Intent intent = new Intent(this, ChooseAreaActivity.class);
                intent.putExtra("from_weather_activity", true);
                startActivity(intent);
                finish();
                break;
            case R.id.refresh_weather:
                publishText.setText(" 同步中...");
                SharedPreferences prefs = PreferenceManager.
                        getDefaultSharedPreferences(this);
                String weatherCode = prefs.getString("weather_code", "");
                if (!TextUtils.isEmpty(weatherCode)) {
                    queryWeatherInfo(weatherCode);
                }
                break;
            case R.id.menu:
                showDialog();
            default:
                break;
        }
    }

    /**
     * 查询县级代号所对应的天气代号。
     */
    private void queryWeatherCode(String countyCode) {
        String address = "http://www.weather.com.cn/data/list3/city" + countyCode + ".xml";
        queryFromServer(address, "countyCode");
    }

    /**
     * 查询天气代号所对应的天气。
     */
    private void queryWeatherInfo(String weatherCode) {
        String address = "http://www.weather.com.cn/data/cityinfo/" + weatherCode + ".html";
        queryFromServer(address, "weatherCode");
    }

    /**
     * 根据传入的地址和类型去向服务器查询天气代号或者天气信息。
     */
    private void queryFromServer(final String address, final String type) {
        HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
            @Override
            public void onFinish(final String response) {
                if ("countyCode".equals(type)) {
                    if (!TextUtils.isEmpty(response)) {
                        // 从服务器返回的数据中解析出天气代号
                        String[] array = response.split("\\|");
                        if (array != null && array.length == 2) {
                            String weatherCode = array[1];
                            queryWeatherInfo(weatherCode);
                        }
                    }
                } else if ("weatherCode".equals(type)) {
                    // 处理服务器返回的天气信息
                    Utility.handleWeatherResponse(WeatherActivity.this,
                            response);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showWeather();
                        }
                    });
                }
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        publishText.setText("同步失败");
                    }
                });
            }
        });
    }

    /**
     * 从SharedPreferences文件中读取存储的天气信息，并显示到界面上。
     */
    private void showWeather() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        cityNameText.setText(prefs.getString("city_name", ""));
        temp1Text.setText(prefs.getString("temp1", ""));
        temp2Text.setText(prefs.getString("temp2", ""));
        weatherDespText.setText(prefs.getString("weather_desp", ""));
        WeatherKind myWeather = weatherkind.get(prefs.getString("weather_desp", ""));
        if (myWeather != null) {
            changeBackground(myWeather);
        }
        publishText.setText("今天" + prefs.getString("publish_time", "") + "发布");
        currentDateText.setText(prefs.getString("current_date", ""));
        weatherInfoLayout.setVisibility(View.VISIBLE);
        cityNameText.setVisibility(View.VISIBLE);
        Intent intent = new Intent(this, AutoUpdateService.class);
        startService(intent);
    }
    private enum WeatherKind {
        cloudy, fog, hailstone, light_rain, moderte_rain, overcast, rain_snow,
        sand_strom, rainstorm, shower_rain, snow, sunny, thundershower,thundershower_shower_rain,
        shower_rain_moderte_rain,cloudy_sunny,shower_rain_thundershower;
    }
    private static Map<String, WeatherKind> weatherkind = new HashMap<String, WeatherKind>();
    static {
        weatherkind.put("多云", WeatherKind.cloudy);
        weatherkind.put("雾", WeatherKind.fog);
        weatherkind.put("冰雹", WeatherKind.hailstone);
        weatherkind.put("小雨", WeatherKind.light_rain);
        weatherkind.put("中雨", WeatherKind.moderte_rain);
        weatherkind.put("阴", WeatherKind.overcast);
        weatherkind.put("雨加雪", WeatherKind.rain_snow);
        weatherkind.put("沙尘暴", WeatherKind.sand_strom);
        weatherkind.put("暴雨", WeatherKind.rainstorm);
        weatherkind.put("阵雨", WeatherKind.shower_rain);
        weatherkind.put("小雪", WeatherKind.snow);
        weatherkind.put("晴", WeatherKind.sunny);
        weatherkind.put("雷阵雨", WeatherKind.thundershower);
        weatherkind.put("雷阵雨转阵雨", WeatherKind.thundershower_shower_rain);
        weatherkind.put("阵雨转中雨", WeatherKind.shower_rain_moderte_rain);
        weatherkind.put("多云转晴", WeatherKind.cloudy_sunny);
        weatherkind.put("阵雨转雷阵雨", WeatherKind.shower_rain_thundershower);
    }
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void changeBackground(WeatherKind weather) {
        weatherBg = findViewById(R.id.weather_background);
        switch (weather) {
            case cloudy_sunny:
            case cloudy:
                weatherBg.setBackground(this.getResources().getDrawable(
                        R.drawable.cloudy));
                break;
            case fog:
                weatherBg.setBackground(this.getResources().getDrawable(R.drawable.fog));
                break;
            case hailstone:
                weatherBg.setBackground(this.getResources().getDrawable(
                        R.drawable.hailstone));
                break;
            case light_rain:
                weatherBg.setBackground(this.getResources().getDrawable(
                        R.drawable.light_rain));
                break;
            case moderte_rain:
                weatherBg.setBackground(this.getResources().getDrawable(
                        R.drawable.moderte_rain));
                break;
            case overcast:
                weatherBg.setBackground(this.getResources().getDrawable(
                        R.drawable.overcast));
                break;
            case rain_snow:
                weatherBg.setBackground(this.getResources().getDrawable(
                        R.drawable.rain_snow));
                break;
            case rainstorm:
                weatherBg.setBackground(this.getResources().getDrawable(
                        R.drawable.rainstorm));
                break;
            case sand_strom:
                weatherBg.setBackground(this.getResources().getDrawable(
                        R.drawable.sand_strom));
                break;
            case shower_rain_thundershower:
            case shower_rain_moderte_rain:
            case shower_rain:
                weatherBg.setBackground(this.getResources().getDrawable(
                        R.drawable.shower_rain));
                break;
            case snow:
                weatherBg.setBackground(this.getResources().getDrawable(R.drawable.snow));
                break;
            case sunny:
                weatherBg.setBackground(this.getResources()
                        .getDrawable(R.drawable.sunny));
                break;
            case thundershower_shower_rain:
            case thundershower:
                weatherBg.setBackground(this.getResources().getDrawable(
                        R.drawable.thundershower));
                break;
            default:
        }

    }
    /**
     * 设置密码对话框
     */
    private void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(WeatherActivity.this);
        View view=	View.inflate(WeatherActivity.this, R.layout.dialog_layout, null);
        ed_pl = (EditText) view.findViewById(R.id.ed_pl);
        ok = (Button) view.findViewById(R.id.ok);
        cancel = (Button) view.findViewById(R.id.cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //把这个对话框取消掉
                return;
            }
        });
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


            }
        });
        dialog = builder.create();
        dialog.setView(view,0,0,0,0);
        dialog.show();
    }
}