package com.leng.hiddencamera.zipthings;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.leng.hiddencamera.R;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class unZipFileService extends Service {
	public static String path = "mnt/sdcard/MyData";
	private static String password = "fls94#@AB";

	/** Notification��ID */
	int notifyId = 102;
	/** Notification�Ľ�������ֵ */
	int progress = 0;
	NotificationCompat.Builder mBuilder;
	public NotificationManager mNotificationManager;
	Handler handler;
	private String TAG="unZipFileService";

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {




		new Thread(new Runnable() {

			@Override
			public void run() {
				SharedPreferences targetPath = getSharedPreferences(
						"targetPath", 0);
				final String target = targetPath.getString("target", "");
				Log.i(TAG, "��Ҫ�����ļ�--"+target);
				
				//����֪ͨ�Ĵ���
				 
				NotificationCompat.Builder builder = new NotificationCompat.Builder(
						getApplicationContext());

				// ����֪ͨ�Ļ�����Ϣ��icon�����⡢����  
				builder.setSmallIcon(R.drawable.ic_app);
				builder.setContentTitle("��Ļ��ʿ");
				builder.setContentText("���ڽӽ��ܣ����Ժ󣬽�������Զ�����");

				// ����֪ͨ�������
				Notification notification = builder.build();
				notification.flags |= Notification.FLAG_NO_CLEAR;

				// ����֪ͨ id ��Ҫ��Ӧ����Ψһ
				NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
				notificationManager.notify(1, notification);
				
				
				//������7.25�� �µ� �����ļ� �޸��ļ��ķ���
				//�����ļ���
				String newFileName=target.replace(".m9xs", ".mp4");
				
				
				try {
					AddFilesWithAESEncryption.repairFile(target, newFileName);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					Log.i(TAG, "���ܵ�ʱ��׽���쳣");

					//����Toast�ķ���
//					handler=new Handler(Looper.getMainLooper());
//					handler.post(new Runnable(){
//						public void run(){
//							Toast.makeText(getApplicationContext(),"�洢�ռ䲻�㲻�ܽ��ܣ��������"+ZipFileService.FormetFileSize(ZipFileService.getFileSize(target)+800*1024*1024)+"�ռ�֮���ٽ��ܲ鿴",Toast.LENGTH_LONG).show();
//						}
//					});

					AlertActivity.MESSAGE="�洢�ռ䲻�㲻�ܽ��ܣ��������"+ZipFileService.FormetFileSize(ZipFileService.getFileSize(target)+800*1024*1024)+"�ռ�֮���ٽ��ܲ鿴";

					//��dialog�ķ�ʽչʾһ��activity
					Intent it =new Intent(getApplicationContext(),AlertActivity.class);
					it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					startActivity(it);


					e.printStackTrace();
					stopSelf();
					return;
				}
				
				
				// ִ�н�ѹ�� ���ܵĴ���
//				AddFilesWithAESEncryption.unZipFilesWithPassword(target,
//						SettingsActivity.SAVED_VIDEO_PATH, password); // path

				// �����Ƿ���֪ͨ�Ĵ�����
				AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);

				long triggerAtTime = SystemClock.elapsedRealtime();// ���ʱ�������ѹ�����֮���ټ�1��ͷ��͹㲥��Ȼ�����֪ͨ�ͺ�
				// �˴����ÿ���AlarmReceiver���Service
				Intent i2 = new Intent(getApplicationContext(),
						unZippAlarmReceiver.class);
				PendingIntent pi = PendingIntent.getBroadcast(
						getApplicationContext(), 0, i2, 0);
//				sendBroadcast(i2);

				manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
						triggerAtTime, pi);

				Log.i(TAG, "����ִ�����");

				stopSelf();

			}
		}).start();

		return super.onStartCommand(intent, flags, startId);

	}





	private String GetTime() {
		SimpleDateFormat formatter = new SimpleDateFormat(
				"yyyy��MM��dd��    HH:mm:ss     ");
		Date curDate = new Date(System.currentTimeMillis());// ��ȡ��ǰʱ��
		String str = formatter.format(curDate);
		return str;
	}

	@Override
	public void onDestroy() {
		Log.i(TAG,"unZipFileService is Desotrying");
		super.onDestroy();

		// ��Service������ر�AlarmManager
//		AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
//		Intent i = new Intent(this, AlarmReceiver.class);
//		PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, 0);
//		manager.cancel(pi);

	}
}