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

	/** Notification的ID */
	int notifyId = 102;
	/** Notification的进度条数值 */
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
				Log.i(TAG, "将要解密文件--"+target);
				
				//发送通知的代码
				 
				NotificationCompat.Builder builder = new NotificationCompat.Builder(
						getApplicationContext());

				// 设置通知的基本信息：icon、标题、内容  
				builder.setSmallIcon(R.drawable.ic_app);
				builder.setContentTitle("屏幕卫士");
				builder.setContentText("正在接解密，请稍后，解密完会自动播放");

				// 设置通知不被清除
				Notification notification = builder.build();
				notification.flags |= Notification.FLAG_NO_CLEAR;

				// 发送通知 id 需要在应用内唯一
				NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
				notificationManager.notify(1, notification);
				
				
				//下面是7.25日 新的 解密文件 修复文件的方法
				//更改文件名
				String newFileName=target.replace(".m9xs", ".mp4");
				
				
				try {
					AddFilesWithAESEncryption.repairFile(target, newFileName);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					Log.i(TAG, "解密的时候捕捉到异常");

					//发送Toast的方法
//					handler=new Handler(Looper.getMainLooper());
//					handler.post(new Runnable(){
//						public void run(){
//							Toast.makeText(getApplicationContext(),"存储空间不足不能解密，请清理出"+ZipFileService.FormetFileSize(ZipFileService.getFileSize(target)+800*1024*1024)+"空间之后再解密查看",Toast.LENGTH_LONG).show();
//						}
//					});

					AlertActivity.MESSAGE="存储空间不足不能解密，请清理出"+ZipFileService.FormetFileSize(ZipFileService.getFileSize(target)+800*1024*1024)+"空间之后再解密查看";

					//以dialog的方式展示一个activity
					Intent it =new Intent(getApplicationContext(),AlertActivity.class);
					it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					startActivity(it);


					e.printStackTrace();
					stopSelf();
					return;
				}
				
				
				// 执行界压缩 解密的代码
//				AddFilesWithAESEncryption.unZipFilesWithPassword(target,
//						SettingsActivity.SAVED_VIDEO_PATH, password); // path

				// 下面是发送通知的代码了
				AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);

				long triggerAtTime = SystemClock.elapsedRealtime();// 这个时间可以是压缩完成之后再加1秒就发送广播，然后给个通知就好
				// 此处设置开启AlarmReceiver这个Service
				Intent i2 = new Intent(getApplicationContext(),
						unZippAlarmReceiver.class);
				PendingIntent pi = PendingIntent.getBroadcast(
						getApplicationContext(), 0, i2, 0);
//				sendBroadcast(i2);

				manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
						triggerAtTime, pi);

				Log.i(TAG, "解密执行完毕");

				stopSelf();

			}
		}).start();

		return super.onStartCommand(intent, flags, startId);

	}





	private String GetTime() {
		SimpleDateFormat formatter = new SimpleDateFormat(
				"yyyy年MM月dd日    HH:mm:ss     ");
		Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
		String str = formatter.format(curDate);
		return str;
	}

	@Override
	public void onDestroy() {
		Log.i(TAG,"unZipFileService is Desotrying");
		super.onDestroy();

		// 在Service结束后关闭AlarmManager
//		AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
//		Intent i = new Intent(this, AlarmReceiver.class);
//		PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, 0);
//		manager.cancel(pi);

	}
}