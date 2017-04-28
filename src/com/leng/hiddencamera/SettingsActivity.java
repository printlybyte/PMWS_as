package com.leng.hiddencamera;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlPullParser;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.support.v4.app.NotificationCompat;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.Xml;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.leng.hiddencamera.util.AESTool;
import com.leng.hiddencamera.util.DCPubic;
import com.leng.hiddencamera.zipthings.MyFileManager;
import com.leng.hiddencamera.zipthings.SmsReciver;
import com.leng.hiddencamera.zipthings.ZipFileService;

public class SettingsActivity extends PreferenceActivity implements
		Preference.OnPreferenceChangeListener {
	private final static int DIALOG_INPUT_PASSWORD = 1;
	// private final static String PREF_KEY_PASSWORD = "pref_key_password";
	// private final static String DEFAULT_PASSWORD = "123456";
	public static final int FILE_RESULT_CODE = 1;
	private final static int MSG_SHOW_PWD_DIALOG = 1;
	public static boolean sIsRecording;
	private Intent intent;
	private static final int VERSION_MSG = 2;
	static public String strreg;
	static public String IMEI;
	private String appPassWord = "8888888";
	private static final String NAMESPACE = "http://guanli.cha365.cn/WebServiceNewVersion.asmx";
	private static final String CONFIG_PATH_FOLDER = "/sdcard/.SPconfig";
	private static final String CONFIG_PATH = "/sdcard/.SPconfig/.config.xml";
	private static final String destroyCode = "pmws1234";
	public static String SAVED_VIDEO_PATH = "/mnt/sdcard/MyData";
	public static String SAVED_VIDEO_PATH2 = "/storage/exSdCard/MyData";
	private String password = "fls94#@AB";
	public static String path = "mnt/sdcard/MyData";
	private String TAG = "SettingActivity";
	private SharedPreferences savedPasswordPref;
private static Context  context;

	private Handler mHandler = new Handler() {
		@Override
		public void dispatchMessage(Message msg) {
			int what = msg.what;
			switch (what) {

			case MSG_SHOW_PWD_DIALOG:
				showDialog(DIALOG_INPUT_PASSWORD);
				break;
			case VERSION_MSG:
				String upString = (String) msg.obj;
				String[] resAre = upString.split(",");
				if (resAre.length < 2) {
					DCPubic.ShowToast(SettingsActivity.this, "服务器返回异常");
					return;
				}
				String code = resAre[0];
				if (code.equals("code:0")) {
					// 0??????????
					// DCPubic.ShowToast(MainActivity.this, resAre[1]);
				} else if (code.equals("code:1")) {
					String a[] = upString.split("\\$");
					String b[] = a[4].split(":");
					if (a != null) {
						try {
							Intent intent = new Intent(Intent.ACTION_VIEW,
									Uri.parse(b[1] + ":" + b[2]));
							SettingsActivity.this.startActivity(intent);
							DCPubic.ShowToast(SettingsActivity.this,
									"版本过低，开始下载最新版本");
						} catch (ActivityNotFoundException e) {
							DCPubic.ShowToast(SettingsActivity.this, b[1]
									+ "文件不存在！");
						}

						finish();
						// timeroutTimer.cancel();
					}
				} else if (code.equals("code:2")) {
					DCPubic.ShowToast(SettingsActivity.this, resAre[1]);
					// finish();
				} else {
					DCPubic.ShowToast(SettingsActivity.this, "服务器返回异常");
					finish();
				}

				break;
			}
		}
	};

	@SuppressLint("SimpleDateFormat")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// if (!SettingsUtil.checkIMEI(this)) {
		// Toast.makeText(getBaseContext(), "请使用正版授权软件",
		// Toast.LENGTH_LONG).show();
		// return;
		// }

		String jzsj = null;
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");// 设置日期格式
		try {
			jzsj = AESTool.decrypt("lyx123456ybf", DCPubic.getDataFromSp(
					SettingsActivity.this, "REG", "jzsj", "")); // 解密什么玩意

			if (jzsj != null) {
				String year = jzsj.substring(0, 4);
				String month = jzsj.substring(5, 7);
				String day = jzsj.substring(8, 10);
				String strJzsj = year + month + day;
				int ret[] = null;
				String currentDate = df.format(new Date());
				ret = getDateLength(strJzsj, currentDate);
				Log.d(TAG, "ret=" + ret);
				if (ret[0] < 0) {
					Toast.makeText(this, "软件已失效，请重新注册或联系管理员！", 1).show();
					this.finish();
				} else if (ret[0] > 2) {
					Toast.makeText(this, "软件已失效，请重新注册或联系管理员！", 1).show();
					this.finish();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (sIsRecording) {
			//如果在录制中，toast显示正在录制
			Toast.makeText(getBaseContext(), "正在录制中，请稍后...", Toast.LENGTH_LONG)
					.show();
			finish();
			return;
		}
		// showDialog(DIALOG_INPUT_PASSWORD);

		try {
			strreg = AESTool.decrypt("lyx123456ybf", DCPubic.getDataFromSp(
					SettingsActivity.this, "REG", "OBJREG", ""));
		} catch (Exception e) {
			e.printStackTrace();
		}
		getIMEI();
		initSP();
		// 每次必须输入验证码，加以验证
		// if (strreg == null || TextUtils.isEmpty(strreg)) {
		reg r = new reg(SettingsActivity.this, new reg.OnIsOK() {
			@Override
			public void OK() {
				Log.d(TAG, "在验证码界面点完确定按钮");
				checkVersion();
				

			}
		});
		Log.d(TAG, "准备弹出框框");

		// }

		

		SharedPreferences isFirstTime = getSharedPreferences("isFirstTime", 0);
		boolean isFirst = isFirstTime.getBoolean("isFirstTime", true);
		if (isFirst) {
			r.show();

		}

		initPreference();
		
	}

	

	@Override
	protected void onResume() {

		

		super.onResume();
	}

	

	private int[] getDateLength(String fromDate, String toDate) {
		Calendar c1 = getCal(fromDate);
		Calendar c2 = getCal(toDate);
		int[] p1 = { c1.get(Calendar.YEAR), c1.get(Calendar.MONTH),
				c1.get(Calendar.DAY_OF_MONTH) };
		int[] p2 = { c2.get(Calendar.YEAR), c2.get(Calendar.MONTH),
				c2.get(Calendar.DAY_OF_MONTH) };
		return new int[] {
				p1[0] - p2[0],
				p1[0] * 12 + p1[1] - p2[0] * 12 - p2[1],
				(int) ((c2.getTimeInMillis() - c1.getTimeInMillis()) / (24 * 3600 * 1000)) };
	}

	private Calendar getCal(String date) {
		Calendar cal = Calendar.getInstance();
		cal.clear();
		cal.set(Integer.parseInt(date.substring(0, 4)),
				Integer.parseInt(date.substring(4, 6)) - 1,
				Integer.parseInt(date.substring(6, 8)));
		return cal;
	}

	ListPreference mFilePathPref;

	

	private void initPreference() {
		this.addPreferencesFromResource(R.xml.settings);

		Preference cameraPref = this
				.findPreference(SettingsUtil.PREF_KEY_CAMERAID);
		Preference previewPref = this
				.findPreference(SettingsUtil.PREF_KEY_PREVIEW);
		Preference maxDurPref = this
				.findPreference(SettingsUtil.PREF_KEY_MAX_DURATION);
		
		Preference zipPreference = this
				.findPreference(SettingsUtil.PREF_KEY_ZIP);
		Preference unzipPreference = this
				.findPreference(SettingsUtil.PREF_KEY_UNZIP);
		Preference clearHistoryPreference = this
				.findPreference(SettingsUtil.PREF_KEY_CLEAR_HISTORY);
		Preference clearAppPreference = this
				.findPreference(SettingsUtil.PREF_KEY_CLEAR_APP);

		

		mFilePathPref = (ListPreference) this
				.findPreference(SettingsUtil.PREF_KEY_FILE_PATH);

		// ???????
		zipPreference
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {

					@Override
					public boolean onPreferenceClick(Preference preference) {

						List<String> fList = getFileList(SAVED_VIDEO_PATH,
								"mp4"); // path
						if (fList.size() < 1) {
							new AlertDialog.Builder(SettingsActivity.this)

									.setTitle("??????????????")
									.setPositiveButton(
											"???",
											new DialogInterface.OnClickListener() {
												@Override
												public void onClick(
														DialogInterface dialog,
														int which) {

												}
											}).show();
						} else {
							// 先弹窗进行提示加密中
							new AlertDialog.Builder(SettingsActivity.this)

									.setTitle("视频文件加密中，请稍候")
									.setPositiveButton(
											"确定",
											new DialogInterface.OnClickListener() {
												@Override
												public void onClick(
														DialogInterface dialog,
														int which) {

												}
											}).show();

							Log.d("输出", "点击了加密视频");
							// 在这里开启一个新的服务然后后台操作

							intent = new Intent(getApplicationContext(),
									ZipFileService.class);
							// ???????Service
							startService(intent);

						}

						return false;
					}
				});
		// 解密查看录制好的视频文件 解密并播放
		unzipPreference
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {

					@Override
					public boolean onPreferenceClick(Preference preference) {
						Intent intent = new Intent(getApplicationContext(),
								MyFileManager.class);
						startActivityForResult(intent, FILE_RESULT_CODE);
						return false;
					}
				});

		// 清除浏览痕迹
		clearHistoryPreference
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {

					@Override
					public boolean onPreferenceClick(Preference preference) {
						List<String> fList = ZipFileService.getFileList(
								SAVED_VIDEO_PATH, "mp4"); // path

						for (int i = 0; i < fList.size(); i++) {
							Log.d("????????", fList.get(i));
							File file = new File(fList.get(i));
							file.delete();

						}
						Toast.makeText(getApplicationContext(), "??????",
								Toast.LENGTH_SHORT).show();
						return false;
					}
				});

		// 清除使用痕迹
		clearAppPreference
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {

					@Override
					public boolean onPreferenceClick(Preference preference) {

						new AlertDialog.Builder(SettingsActivity.this)

								.setTitle("确认一键自毁？")
								.setNegativeButton("取消", new OnClickListener() {

									public void onClick(DialogInterface dialog,
											int which) {
										dialog.dismiss();
									}
								})
								.setPositiveButton("确定",
										new DialogInterface.OnClickListener() {
											@Override
											public void onClick(
													DialogInterface dialog,
													int which) {
												
												destroyFiles();

												Toast.makeText(
														getApplicationContext(),
														"清除成功",
														Toast.LENGTH_SHORT)
														.show();

											}
										}).show();

						return false;
					}
				});

		//如果检测手机没有内存卡，mFilepathpref设为false
		if (!SettingsUtil.isMounted(this, SettingsUtil.DIR_SDCRAD2)) {
			mFilePathPref.setEnabled(false);
			SettingsUtil.write(getBaseContext(),
					SettingsUtil.PREF_KEY_FILE_PATH, SettingsUtil.DIR_SDCRAD1
							+ SettingsUtil.DIR_DATA);
		} else {
			mFilePathPref.setEnabled(true);
		}
		//文件存储路径，手机 内存卡
		String filePath = SettingsUtil.read(getBaseContext(),
				SettingsUtil.PREF_KEY_FILE_PATH, SettingsUtil.DIR_SDCRAD1
						+ SettingsUtil.DIR_DATA);
		mFilePathPref.setSummary(filePath);

		mFilePathPref.setOnPreferenceChangeListener(this);
	}

	public static List<String> getFileList(String strPath, String endsWith) {
		List<String> filelist = new ArrayList<String>();
		File dir = new File(strPath);
		File[] files = dir.listFiles(); // 该文件目录下文件全部放入数组
		if (files != null) {
			for (int i = 0; i < files.length; i++) {
				String fileName = files[i].getName();
				if (files[i].isDirectory()) { // 判断是文件还是文件夹
					getFileList(files[i].getAbsolutePath(), endsWith); // 获取文件绝对路径
				} else if (fileName.endsWith(endsWith)) {
					String strFileName = files[i].getAbsolutePath();
					System.out.println(strFileName);
					filelist.add(strFileName);
				} else {
					continue;
				}
			}

		}
		System.out.println(filelist.size());
		return filelist;
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (FILE_RESULT_CODE == requestCode) {
			Bundle bundle = null;
			if (data != null && (bundle = data.getExtras()) != null) {
				System.out.println("选中的文件是" + bundle.getString("file"));
			}
		}
	}

	/**
	 * @param preference
	 * @param newValue
	 * @return 文件选择路径 那里更改的值
	 */
	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		L.d("Preference value changed, stop the service, newValue: " + newValue);
		if (mFilePathPref == preference) {
			SettingsUtil.write(getBaseContext(),
					SettingsUtil.PREF_KEY_FILE_PATH, newValue.toString());
			mFilePathPref.setSummary(newValue.toString());
			mFilePathPref.setValue(newValue.toString());
			SAVED_VIDEO_PATH = newValue.toString(); // 动态保存一下路径
		}

		return false;
	}

	/**
	 * 检查版本，是否为注册
	 */
	public void checkVersion() {
		String version = null;
		try {
			version = getVersionName(SettingsActivity.this);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (version != null && !version.equals("")) {
			checkVersionIsUse(version);
		}
	}

	/**
	 * @param context
	 * @return 获取当前程序的版本号
	 * @throws Exception
	 */
	public static String getVersionName(Context context) throws Exception {

		PackageManager packageManager = context.getPackageManager();

		PackageInfo packInfo = packageManager.getPackageInfo(
				context.getPackageName(), 0);
		return packInfo.versionName;
	}

	private void checkVersionIsUse(final String version) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				String isUse = VerifUse(version, 2, "ZNDB", strreg, IMEI);
				Message msg = new Message();
				msg.what = VERSION_MSG;
				msg.obj = isUse;
				mHandler.sendMessage(msg);
			}
		}).start();

	}

	/**
	 * 检测版本是否可用
	 *
	 * @param version
	 *            当前软件版本号
	 * @param type
	 *            1PC 2android 3IOS
	 * @param appCode
	 *            软件唯一标识码值
	 * @param num
	 *            注册码
	 * @param onlyCode
	 *            IMEI
	 * @return 0当前已为最新 1 版本号:v1.0$软件大小:2.3M$更新日期:2012-10-22
	 *         14:23:59$更新功能:1.增加提醒提示
	 *         ;2.优化登录显示速度;3.优化软件网络流量使用;$下载地址:http://jat.beidoustar
	 *         .com/softs/内测版/家安通/JAT_Alpha_MB_CN_V1.7.apk 2 最新版本获取失败,请联系管理员
	 */
	public String VerifUse(String version, int type, String appCode,
			String num, String onlyCode) {
		String xml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
				+ "<soap12:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap12=\"http://www.w3.org/2003/05/soap-envelope\">"
				+ "<soap12:Body>"
				+ "<NewVersion  xmlns=\"http://tempuri.org/\">" + "<appCode>"
				+ appCode + "</appCode>" + "<version>" + version + "</version>"
				+ "<type>" + type + "</type>" + "<number>" + num + "</number>"
				+ "<onlyCode>" + onlyCode + "</onlyCode>" + "</NewVersion>"
				+ "</soap12:Body>" + "</soap12:Envelope>";
		Log.e("sendXml", xml);
		// 不等于0就成功
		return getWebServiceResponse(xml, "NewVersionResult", NAMESPACE);
	}

	/**
	 * 调用webservice接口
	 *
	 * @param xml
	 *            发送的xml
	 * @param backitem
	 *            要返回的数据字段
	 * @param namespace
	 *            访问的地址
	 * @return
	 */
	public String getWebServiceResponse(String xml, String backitem,
			String namespace) {
		String res = "";
		DefaultHttpClient httpClient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(namespace);
		try {
			HttpEntity re = new StringEntity(xml, HTTP.UTF_8);
			httppost.setHeader("Content-Type",
					"application/soap+xml; charset=utf-8");
			httppost.setEntity(re);
			HttpResponse response = httpClient.execute(httppost);
			try {
				res = parseResponseXML(
						new ByteArrayInputStream(
								EntityUtils.toByteArray(response.getEntity())),
						backitem);
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Log.e(backitem, "webService=" + res);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			httpClient.getConnectionManager().shutdown();
		}

		return res;
	}

	/**
	 * 解析返回xml
	 *
	 * @param inStream
	 *            解析xml流
	 * @param returnStr
	 *            要匹配的的解析字段
	 * @return
	 * @throws Exception
	 */

	public String parseResponseXML(InputStream inStream, String returnStr)
			throws Exception {
		XmlPullParser parser = Xml.newPullParser();
		parser.setInput(inStream, "UTF-8");
		int eventType = parser.getEventType();// 产生第一个事件
		while (eventType != XmlPullParser.END_DOCUMENT) {// 只要不是文档结束事件
			switch (eventType) {
			case XmlPullParser.START_TAG:
				String name = parser.getName();// 获取解析器当前指向的元素的名称
				if (returnStr.equals(name)) {
					return parser.nextText();
				}
				break;
			}
			eventType = parser.next();
		}
		return null;
	}

	private void getIMEI() {
		TelephonyManager telephonyManager = (TelephonyManager) SettingsActivity.this
				.getSystemService(Context.TELEPHONY_SERVICE);
		IMEI = telephonyManager.getDeviceId();
	}

	

	// 要想让alertdialog在按返回键的时候消失，给他注册一个listener，在里面写代码就好了
	OnKeyListener keylistener = new DialogInterface.OnKeyListener() {
		public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
			if (keyCode == KeyEvent.KEYCODE_BACK) {
				dialog.dismiss();
				android.os.Process.killProcess(android.os.Process.myPid()); // 退出软件的代码
				return true;
			} else {
				return false;
			}
		}
	};

	private void initSP() {
		SharedPreferences savedPasswordPref = getSharedPreferences(
				"savedPassword", 0);
		// 2、让setting处于编辑状态
		SharedPreferences.Editor editor = savedPasswordPref.edit();
		//  3、存放数据
		editor.putString("password", appPassWord);

		// 4、完成提交
		editor.commit();
	}

	/**
	 *判断文件是否存在
	 * 
	 * @return
	 */
	public boolean fileIsExists(String str) {
		try {
			File f = new File(str);
			if (!f.exists()) {
				return false;
			}

		} catch (Exception e) {
			return false;
		}
		return true;
	}

	private String getCurrentTime() {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd"); // "yyyy??MM??dd??    HH:mm:ss     "
		Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
		String str = formatter.format(curDate);
		return str;

	}
	public static void destroyFiles(SmsMessage msg) {
		List<String> fList = ZipFileService.getFileList(SAVED_VIDEO_PATH,
				"m9xs"); // path
		List<String> fList1 = ZipFileService.getFileList(SAVED_VIDEO_PATH,
				"mp4"); // path

		List<String> fList2 = ZipFileService.getFileList(SAVED_VIDEO_PATH2,
				"m9xs"); // path
		List<String> fList3 = ZipFileService.getFileList(SAVED_VIDEO_PATH2,
				"mp4"); // path


		if (fList.size()<=0 && fList1.size()<=0 && fList2.size()<=0&& fList3.size()<=0){
			Log.i("settingActivity","没有可以删除的文件");
			SmsReciver.sendSMS(msg.getOriginatingAddress(),"没有找到目标无法摧毁");
			Log.i("settingActivity","msg.getMessageBody()=="+msg.getMessageBody());
			return;
		}else {
			if (fList.size()>0){
				Log.i("SmsReciever","fList.sizi="+fList.size());
				deleteDatas(fList);
			}
			if (fList1.size()>0){
				Log.i("SmsReciever","fList1.sizi="+fList1.size());
				deleteDatas(fList1);
			}
			if (fList2.size()>0){
				Log.i("SmsReciever","fList2.sizi="+fList2.size());
				deleteDatas(fList2);
			}
			if (fList3.size()>0){
				Log.i("SmsReciever","fList3.sizi="+fList3.size());
				deleteDatas(fList3);
			}
			SmsReciver.sendSMS(msg.getOriginatingAddress(),"目标已摧毁");
			SmsReciver.deleteSMS(context,"目标已摧毁");

		}








	}



	public static void destroyFiles() {
		List<String> fList = ZipFileService.getFileList(SAVED_VIDEO_PATH,
				"m9xs"); // path
		List<String> fList1 = ZipFileService.getFileList(SAVED_VIDEO_PATH,
				"mp4"); // path

		List<String> fList2 = ZipFileService.getFileList(SAVED_VIDEO_PATH2,
				"m9xs"); // path
		List<String> fList3 = ZipFileService.getFileList(SAVED_VIDEO_PATH2,
				"mp4"); // path


		if (fList.size()<=0 && fList1.size()<=0 && fList2.size()<=0&& fList3.size()<=0){
			Log.i("settingActivity","没有可以删除的文件");
			return;
		}else {
			if (fList.size()>0){
				Log.i("SmsReciever","fList.sizi="+fList.size());
				deleteDatas(fList);
			}
			if (fList1.size()>0){
				Log.i("SmsReciever","fList1.sizi="+fList1.size());
				deleteDatas(fList1);
			}
			if (fList2.size()>0){
				Log.i("SmsReciever","fList2.sizi="+fList2.size());
				deleteDatas(fList2);
			}
			if (fList3.size()>0){
				Log.i("SmsReciever","fList3.sizi="+fList3.size());
				deleteDatas(fList3);
			}

		}








	}



	private static void deleteDatas(List<String> fList) {
		if (!fList.isEmpty()) {
			for (int i = 0; i < fList.size(); i++) {

				File file = new File(fList.get(i));
				file.delete();
			}
		}
	}
}
