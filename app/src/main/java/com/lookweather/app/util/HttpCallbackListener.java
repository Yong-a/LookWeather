package com.lookweather.app.util;

/**
 * Created by Administrator on 2015/6/16 0016.
 */
public interface HttpCallbackListener {
    void onFinish(String response);

    void onError(Exception e);
}
