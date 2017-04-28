package com.leng.hiddencamera;

import com.leng.hiddencamera.util.CrashHandler;

import android.app.Application;

public class MyApplication extends Application {

	@Override
	public void onCreate() {
		CrashHandler crashHandler = CrashHandler.getInstance();
		crashHandler.init(getApplicationContext());
		super.onCreate();
	}

}
