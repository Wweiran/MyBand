package com.example.administrator.myband;

import android.app.Application;

import com.baidu.mapapi.SDKInitializer;

/**
 * Created by WANGWEIRAN on 2017/5/24.
 */

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        SDKInitializer.initialize(this);
    }
}
