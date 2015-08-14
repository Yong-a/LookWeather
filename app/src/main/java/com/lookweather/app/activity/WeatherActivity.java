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
 * ��Ŀ���ƣ�LookWeather
 * ����������ʾ����Activity
 * �����ˣ�Yong_a
 * ����ʱ�䣺2015/8/11 21:55
 * �޸��ˣ�Yong-a
 * �޸�ʱ�䣺2015/8/11 21:55
 * �޸ı�ע���޸��ĵ�ע��
 */
public class WeatherActivity extends Activity implements View.OnClickListener {
    private LinearLayout weatherInfoLayout;
    /**
     * ������ʾ������
     */
    private TextView cityNameText;
    /**
     * ������ʾ����ʱ��
     */
    private TextView publishText;
    /**
     * ������ʾ����������Ϣ
     */
    private TextView weatherDespText;
    /**
     * ������ʾ����1
     */
    private TextView temp1Text;
    /**
     * ������ʾ����2
     */
    private TextView temp2Text;
    /**
     * ������ʾ��ǰ����
     */
    private TextView currentDateText;
    /**
     * �л����а�ť
     */
    private Button switchCity;
    /**
     * ����������ť
     */
    private Button refreshWeather;
    /**
     * �˵���ť
     */
    private Button menu;
    /**
     * ����ͼƬ
     */
    private View weatherBg;
    /**
     * ����dialog����
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
        // ��ʼ�����ؼ�
        weatherInfoLayout = (LinearLayout) findViewById(R.id.weather_info_layout);
        cityNameText = (TextView) findViewById(R.id.city_name);
        publishText = (TextView) findViewById(R.id.publish_text);
        weatherDespText = (TextView) findViewById(R.id.weather_desp);
        temp1Text = (TextView) findViewById(R.id.temp1);
        temp2Text = (TextView) findViewById(R.id.temp2);
        currentDateText = (TextView) findViewById(R.id.current_date);
        String countyCode = getIntent().getStringExtra("county_code");
        if (!TextUtils.isEmpty(countyCode)) {
            // ���ؼ�����ʱ��ȥ��ѯ����
            publishText.setText("ͬ����...");
            weatherInfoLayout.setVisibility(View.INVISIBLE);
            cityNameText.setVisibility(View.INVISIBLE);
            queryWeatherCode(countyCode);
        } else {
            // û���ؼ�����ʱ��ֱ����ʾ��������
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
                publishText.setText(" ͬ����...");
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
     * ��ѯ�ؼ���������Ӧ���������š�
     */
    private void queryWeatherCode(String countyCode) {
        String address = "http://www.weather.com.cn/data/list3/city" + countyCode + ".xml";
        queryFromServer(address, "countyCode");
    }

    /**
     * ��ѯ������������Ӧ��������
     */
    private void queryWeatherInfo(String weatherCode) {
        String address = "http://www.weather.com.cn/data/cityinfo/" + weatherCode + ".html";
        queryFromServer(address, "weatherCode");
    }

    /**
     * ���ݴ���ĵ�ַ������ȥ���������ѯ�������Ż���������Ϣ��
     */
    private void queryFromServer(final String address, final String type) {
        HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
            @Override
            public void onFinish(final String response) {
                if ("countyCode".equals(type)) {
                    if (!TextUtils.isEmpty(response)) {
                        // �ӷ��������ص������н�������������
                        String[] array = response.split("\\|");
                        if (array != null && array.length == 2) {
                            String weatherCode = array[1];
                            queryWeatherInfo(weatherCode);
                        }
                    }
                } else if ("weatherCode".equals(type)) {
                    // ������������ص�������Ϣ
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
                        publishText.setText("ͬ��ʧ��");
                    }
                });
            }
        });
    }

    /**
     * ��SharedPreferences�ļ��ж�ȡ�洢��������Ϣ������ʾ�������ϡ�
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
        publishText.setText("����" + prefs.getString("publish_time", "") + "����");
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
        weatherkind.put("����", WeatherKind.cloudy);
        weatherkind.put("��", WeatherKind.fog);
        weatherkind.put("����", WeatherKind.hailstone);
        weatherkind.put("С��", WeatherKind.light_rain);
        weatherkind.put("����", WeatherKind.moderte_rain);
        weatherkind.put("��", WeatherKind.overcast);
        weatherkind.put("���ѩ", WeatherKind.rain_snow);
        weatherkind.put("ɳ����", WeatherKind.sand_strom);
        weatherkind.put("����", WeatherKind.rainstorm);
        weatherkind.put("����", WeatherKind.shower_rain);
        weatherkind.put("Сѩ", WeatherKind.snow);
        weatherkind.put("��", WeatherKind.sunny);
        weatherkind.put("������", WeatherKind.thundershower);
        weatherkind.put("������ת����", WeatherKind.thundershower_shower_rain);
        weatherkind.put("����ת����", WeatherKind.shower_rain_moderte_rain);
        weatherkind.put("����ת��", WeatherKind.cloudy_sunny);
        weatherkind.put("����ת������", WeatherKind.shower_rain_thundershower);
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
     * ��������Ի���
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
                //������Ի���ȡ����
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