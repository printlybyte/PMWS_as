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
	 * 文件（夹）名字
	 */
	private List<String> items = null;
	/**
	 * 文件（夹）路径
	 */
	private List<String> paths = null;
	/**
	 * 根目录
	 **/
	private String rootPath = "/";

	/**
	 * 显示当前目录
	 **/
	private TextView mPath;

	/**
	 * Notification的ID
	 */
	int notifyId = 102;
	/**
	 * Notification的进度条数值
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
	 * 获取指定目录下的所有文件(夹)
	 *
	 * @param filePath
	 */
	private void getFileDir(String filePath) {
		mPath.setText(filePath);
		items = new ArrayList<String>();
		paths = new ArrayList<String>();
		File f = new File(filePath);
		File[] files = f.listFiles();

		// 用来显示 “返回根目录”+"上级目录"
		if (!filePath.equals(rootPath)) {
			items.add("rootPath");
			paths.add(rootPath);

			items.add("parentPath");
			paths.add(f.getParent());
		}

		// 先排序
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
			Log.i("hnyer", filePath + "无子文件");
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

			Toast.makeText(getApplicationContext(), "解密中，请稍后",
					Toast.LENGTH_LONG).show();

		writeLog("decrypt fiel and play；",f);
			return;

		}
		intent.setDataAndType(Uri.fromFile(f), type);
		startActivity(intent);
	}

	// 这个方法的存在就是为了区分是不是特定的文件，比如zip,如果是的话再进行操作的，要是不是的话就不操作，鉴于现在只是对于特定的文件进行操作，
	// 那我还要不要继续研究这个类型這個東西，還是去研究廣播

	/**
	 * @param f
	 * @return 返回的是文件后缀名，原来后缀名要小些，大写根本没用，所以根本没比较到，呵呵了
	 */
	private String getMIMEType(File f) {
		String type = "";
		String fName = f.getName();

		// 将来解压好的视频文件名，然后保存到SharedPreferences
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
	 * @param f 写LOG到SD卡
	 */
	private void writeLog(String first,File f){
		//写日志到SD卡
		File dir = new File(Environment.getExternalStorageDirectory(), "PMWSLog");
		if (!dir.exists()) {
			dir.mkdir();
		}

		try {

			SimpleDateFormat formatter   =   new  SimpleDateFormat    ("yyyy年MM月dd日  HH:mm:ss ");
			Date curDate    =   new Date(System.currentTimeMillis());//获取当前时间
			String    str    =    formatter.format(curDate);

			FileWriter writer = new FileWriter(dir+"/log.txt",true);
			writer.write(first+str+";"+f.getName()+"\r\n");
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();

		}
	}




}