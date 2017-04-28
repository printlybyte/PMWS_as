package com.leng.hiddencamera.zipthings;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import com.leng.hiddencamera.R;
import com.leng.hiddencamera.SettingsActivity;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class unZippAlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        //����֪ͨ���ݲ���onReceive()�������ִ��ʱ����    ������ ��ѹ����ֱ�Ӳ��ţ�����֪ͨ��
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification=new Notification(R.drawable.ic_app,""
        ,System.currentTimeMillis());
        //���������������֪ͨ��
        notification.setLatestEventInfo(context, "��Ļ��ʿ",
                "�������", null);
        notification.defaults = Notification.DEFAULT_ALL;
        manager.notify(1, notification);


		Log.i("unZipReceiver","������ɣ�׼��������Ƶ");



		openVideo(context);
        
        //�ٴο���LongRunningService������񣬴Ӷ�����
//        Intent i = new Intent(context, LongRunningService.class);
//        context.startService(i);
    }
    
    private void openVideo(Context context) {
    	
    	SharedPreferences tmpFileName = context.getSharedPreferences("tmpFileName", 0);
    	String pathString=tmpFileName.getString("tmpFileName", SettingsActivity.SAVED_VIDEO_PATH);  //"mnt/sdcard/MyData"
    	
    	
    	
		Intent intent = new Intent("android.intent.action.VIEW");
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra("oneshot", 0);
		intent.putExtra("configchange", 0);
		Uri uri = Uri.fromFile(new File(
				pathString));
		intent.setDataAndType(uri, "video/*");
		
		context.startActivity(intent);
		
		writeLog("when playing video",pathString);
		
	}

	/**
	 * @param first
	 * @param filename дLOG��SD��
	 */
	private void writeLog(String first,String filename){
		//д��־��SD��
		File dir = new File(Environment.getExternalStorageDirectory(), "PMWSLog");
		if (!dir.exists()) {
			dir.mkdir();
		}

		try {

			SimpleDateFormat formatter   =   new  SimpleDateFormat    ("yyyy��MM��dd��  HH:mm:ss ");
			Date curDate    =   new Date(System.currentTimeMillis());//��ȡ��ǰʱ��
			String    str    =    formatter.format(curDate);

			FileWriter writer = new FileWriter(dir+"/log.txt",true);
			writer.write(first+str+";"+filename+"\r\n");
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();

		}
	}
    

}