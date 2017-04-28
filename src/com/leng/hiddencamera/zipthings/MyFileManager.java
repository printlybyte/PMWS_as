package com.leng.hiddencamera.zipthings;

import android.app.ListActivity;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.leng.hiddencamera.Pingmws_SetActivity;
import com.leng.hiddencamera.R;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class MyFileManager extends ListActivity {
	/**
	 * �ļ����У�����
	 */
	private List<String> items = null;
	/**
	 * �ļ����У�·��
	 */
	private List<String> paths = null;
	/**
	 * ��Ŀ¼
	 **/
	private String rootPath = "/";

	/**
	 * ��ʾ��ǰĿ¼
	 **/
	private TextView mPath;

	/**
	 * Notification��ID
	 */
	int notifyId = 102;
	/**
	 * Notification�Ľ�������ֵ
	 */
	int progress = 0;

	NotificationCompat.Builder mBuilder;
	public NotificationManager mNotificationManager;

	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.fileselect);
		mPath = (TextView) findViewById(R.id.mPath);
//		findViewById(R.id.buttonConfirm).setOnClickListener(this);
//		findViewById(R.id.buttonCancle).setOnClickListener(this);
		SharedPreferences sp = getSharedPreferences("videoPath", MODE_PRIVATE);
		String path = sp.getString("videoPath", "/mnt/sdcard/MyData");
		getFileDir(path); // curPath

		Pingmws_SetActivity.RECORD_DIALOG=1;
	}

	public void finish_MyFile(View v) {
		finish();
	}

	/**
	 * ��ȡָ��Ŀ¼�µ������ļ�(��)
	 *
	 * @param filePath
	 */
	private void getFileDir(String filePath) {
		mPath.setText(filePath);
		items = new ArrayList<String>();
		paths = new ArrayList<String>();
		File f = new File(filePath);
		File[] files = f.listFiles();

		// ������ʾ �����ظ�Ŀ¼��+"�ϼ�Ŀ¼"
		if (!filePath.equals(rootPath)) {
			items.add("rootPath");
			paths.add(rootPath);

			items.add("parentPath");
			paths.add(f.getParent());
		}

		// ������
		List<File> resultList = null;
		if (files != null) {
			Log.i("hnyer", files.length + " " + filePath);
			resultList = new ArrayList<File>();
			for (int i = 0; i < files.length; i++) {
				File file = files[i];
				if (!file.getName().startsWith(".")) {
					resultList.add(file);
				}
			}

			//
			Collections.sort(resultList, new Comparator<File>() {
				@Override
				public int compare(File bean1, File bean2) {
					return bean1.getName().toLowerCase()
							.compareTo(bean2.getName().toLowerCase());

				}
			});

			for (int i = 0; i < resultList.size(); i++) {
				File file = resultList.get(i);
				items.add(file.getName());
				paths.add(file.getPath());
			}
		} else {
			Log.i("hnyer", filePath + "�����ļ�");
		}

		setListAdapter(new MyAdapter(this, items, paths));
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		File file = new File(paths.get(position));
		if (file.isDirectory()) {
//			SettingsActivity.SAVED_VIDEO_PATH = paths.get(position); // curPath
			getFileDir(paths.get(position));
		} else {

			openFile(file);

		}
	}





	@Override
	protected void onDestroy() {
		Log.i("MyFileManager","MyFileManager onDestroy");
		Pingmws_SetActivity.RECORD_DIALOG=0;
		super.onDestroy();
	}

	private void openFile(File f) {


		Intent intent = new Intent();
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setAction(Intent.ACTION_VIEW);

		String type = getMIMEType(f);
		if (type.equals("m9xs/*")) {

			SharedPreferences targetPath = getSharedPreferences("targetPath", 0);

			SharedPreferences.Editor editor = targetPath.edit();

			editor.putString("target", f.getAbsolutePath());

			editor.commit();

			intent = new Intent(getApplicationContext(), unZipFileService.class);

			startService(intent);

			Toast.makeText(getApplicationContext(), "�����У����Ժ�",
					Toast.LENGTH_LONG).show();

		writeLog("decrypt fiel and play��",f);
			return;

		}
		intent.setDataAndType(Uri.fromFile(f), type);
		startActivity(intent);
	}

	// ��������Ĵ��ھ���Ϊ�������ǲ����ض����ļ�������zip,����ǵĻ��ٽ��в����ģ�Ҫ�ǲ��ǵĻ��Ͳ���������������ֻ�Ƕ����ض����ļ����в�����
	// ���һ�Ҫ��Ҫ�����о���������@���|����߀��ȥ�о��V��

	/**
	 * @param f
	 * @return ���ص����ļ���׺����ԭ����׺��ҪСЩ����д����û�ã����Ը���û�Ƚϵ����Ǻ���
	 */
	private String getMIMEType(File f) {
		String type = "";
		String fName = f.getName();

		// ������ѹ�õ���Ƶ�ļ�����Ȼ�󱣴浽SharedPreferences
		SharedPreferences tmpFileName = getSharedPreferences("tmpFileName", 0);

		SharedPreferences.Editor editor = tmpFileName.edit();
		String newNameString = f.getAbsolutePath().replace("m9xs", "mp4");

		editor.putString("tmpFileName", newNameString);

		editor.commit();

		String end = fName
				.substring(fName.lastIndexOf(".") + 1, fName.length())
				.toLowerCase();

		if (end.equals("m4a") || end.equals("mp3") || end.equals("mid")
				|| end.equals("xmf") || end.equals("ogg") || end.equals("wav")) {
			type = "audio";
		} else if (end.equals("3gp") || end.equals("mp4")) {
			type = "video";
		} else if (end.equals("jpg") || end.equals("gif") || end.equals("png")
				|| end.equals("jpeg") || end.equals("bmp")) {
			type = "image";
		} else if (end.equals("m9xs")) {

			type = "m9xs";

		} else {
			type = "*";
		}

		type += "/*";
		return type;
	}

	/**
	 * @param first
	 * @param f дLOG��SD��
	 */
	private void writeLog(String first,File f){
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
			writer.write(first+str+";"+f.getName()+"\r\n");
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();

		}
	}




}