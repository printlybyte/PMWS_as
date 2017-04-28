package com.leng.hiddencamera.zipthings;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.leng.hiddencamera.R;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        //����֪ͨ���ݲ���onReceive()�������ִ��ʱ����
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification=new Notification(R.drawable.ic_app,""
        ,System.currentTimeMillis());
        notification.setLatestEventInfo(context, "��Ļ��ʿ",
                "�������", null);
        notification.defaults = Notification.DEFAULT_ALL;
        manager.notify(1, notification);

        //�ٴο���LongRunningService������񣬴Ӷ�����
//        Intent i = new Intent(context, LongRunningService.class);
//        context.startService(i);
    }

}