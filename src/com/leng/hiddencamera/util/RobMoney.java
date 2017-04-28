package com.leng.hiddencamera.util;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

/**
 * Created by Administrator on 2017/4/28.
 */

public class RobMoney extends AccessibilityService {

    private static final String TAG = "dxj";

    @Override
    protected boolean onKeyEvent(KeyEvent event) {
        Log.i(TAG, "onKeyEvent");
        int key = event.getKeyCode();
        switch(key){
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                Intent downintent = new Intent("com.exmaple.broadcaster.KEYDOWN");
                downintent.putExtra("dtime", System.currentTimeMillis());
                sendBroadcast(downintent);
                Log.i(TAG, "KEYCODE_VOLUME_DOWN");
                Toast.makeText(this, "KEYCODE_VOLUME_DOWN", Toast.LENGTH_SHORT).show();
                break;
            case KeyEvent.KEYCODE_VOLUME_UP:
                Intent upintent = new Intent("com.exmaple.broadcaster.KEYUP");
                upintent.putExtra("utime", System.currentTimeMillis());
                sendBroadcast(upintent);
                Log.i(TAG, "KEYCODE_VOLUME_UP");
                Toast.makeText(this, "KEYCODE_VOLUME_UP", Toast.LENGTH_SHORT).show();
                break;
        }
        return super.onKeyEvent(event);
    }

    @Override
    public void onInterrupt() {

    }

    @Override
    public void onCreate() {
        Log.i(TAG, "RobMoney::onCreate");
       // Toast.makeText(this, "RobMoney::onCreate", Toast.LENGTH_SHORT).show();
        super.onCreate();
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // TODO Auto-generated method stub

    }

}