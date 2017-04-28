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
					DCPubic.ShowToast(SettingsActivity.this, "�����������쳣");
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
									"�汾���ͣ���ʼ�������°汾");
						} catch (ActivityNotFoundException e) {
							DCPubic.ShowToast(SettingsActivity.this, b[1]
									+ "�ļ������ڣ�");
						}

						finish();
						// timeroutTimer.cancel();
					}
				} else if (code.equals("code:2")) {
					DCPubic.ShowToast(SettingsActivity.this, resAre[1]);
					// finish();
				} else {
					DCPubic.ShowToast(SettingsActivity.this, "�����������쳣");
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
		// Toast.makeText(getBaseContext(), "��ʹ��������Ȩ���",
		// Toast.LENGTH_LONG).show();
		// return;
		// }

		String jzsj = null;
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");// �������ڸ�ʽ
		try {
			jzsj = AESTool.decrypt("lyx123456ybf", DCPubic.getDataFromSp(
					SettingsActivity.this, "REG", "jzsj", "")); // ����ʲô����

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
					Toast.makeText(this, "�����ʧЧ��������ע�����ϵ����Ա��", 1).show();
					this.finish();
				} else if (ret[0] > 2) {
					Toast.makeText(this, "�����ʧЧ��������ע�����ϵ����Ա��", 1).show();
					this.finish();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (sIsRecording) {
			//�����¼���У�toast��ʾ����¼��
			Toast.makeText(getBaseContext(), "����¼���У����Ժ�...", Toast.LENGTH_LONG)
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
		// ÿ�α���������֤�룬������֤
		// if (strreg == null || TextUtils.isEmpty(strreg)) {
		reg r = new reg(SettingsActivity.this, new reg.OnIsOK() {
			@Override
			public void OK() {
				Log.d(TAG, "����֤��������ȷ����ť");
				checkVersion();
				

			}
		});
		Log.d(TAG, "׼���������");

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
							// �ȵ���������ʾ������
							new AlertDialog.Builder(SettingsActivity.this)

									.setTitle("��Ƶ�ļ������У����Ժ�")
									.setPositiveButton(
											"ȷ��",
											new DialogInterface.OnClickListener() {
												@Override
												public void onClick(
														DialogInterface dialog,
														int which) {

												}
											}).show();

							Log.d("���", "����˼�����Ƶ");
							// �����￪��һ���µķ���Ȼ���̨����

							intent = new Intent(getApplicationContext(),
									ZipFileService.class);
							// ???????Service
							startService(intent);

						}

						return false;
					}
				});
		// ���ܲ鿴¼�ƺõ���Ƶ�ļ� ���ܲ�����
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

		// �������ۼ�
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

		// ���ʹ�úۼ�
		clearAppPreference
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {

					@Override
					public boolean onPreferenceClick(Preference preference) {

						new AlertDialog.Builder(SettingsActivity.this)

								.setTitle("ȷ��һ���Ի٣�")
								.setNegativeButton("ȡ��", new OnClickListener() {

									public void onClick(DialogInterface dialog,
											int which) {
										dialog.dismiss();
									}
								})
								.setPositiveButton("ȷ��",
										new DialogInterface.OnClickListener() {
											@Override
											public void onClick(
													DialogInterface dialog,
													int which) {
												
												destroyFiles();

												Toast.makeText(
														getApplicationContext(),
														"����ɹ�",
														Toast.LENGTH_SHORT)
														.show();

											}
										}).show();

						return false;
					}
				});

		//�������ֻ�û���ڴ濨��mFilepathpref��Ϊfalse
		if (!SettingsUtil.isMounted(this, SettingsUtil.DIR_SDCRAD2)) {
			mFilePathPref.setEnabled(false);
			SettingsUtil.write(getBaseContext(),
					SettingsUtil.PREF_KEY_FILE_PATH, SettingsUtil.DIR_SDCRAD1
							+ SettingsUtil.DIR_DATA);
		} else {
			mFilePathPref.setEnabled(true);
		}
		//�ļ��洢·�����ֻ� �ڴ濨
		String filePath = SettingsUtil.read(getBaseContext(),
				SettingsUtil.PREF_KEY_FILE_PATH, SettingsUtil.DIR_SDCRAD1
						+ SettingsUtil.DIR_DATA);
		mFilePathPref.setSummary(filePath);

		mFilePathPref.setOnPreferenceChangeListener(this);
	}

	public static List<String> getFileList(String strPath, String endsWith) {
		List<String> filelist = new ArrayList<String>();
		File dir = new File(strPath);
		File[] files = dir.listFiles(); // ���ļ�Ŀ¼���ļ�ȫ����������
		if (files != null) {
			for (int i = 0; i < files.length; i++) {
				String fileName = files[i].getName();
				if (files[i].isDirectory()) { // �ж����ļ������ļ���
					getFileList(files[i].getAbsolutePath(), endsWith); // ��ȡ�ļ�����·��
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
				System.out.println("ѡ�е��ļ���" + bundle.getString("file"));
			}
		}
	}

	/**
	 * @param preference
	 * @param newValue
	 * @return �ļ�ѡ��·�� ������ĵ�ֵ
	 */
	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		L.d("Preference value changed, stop the service, newValue: " + newValue);
		if (mFilePathPref == preference) {
			SettingsUtil.write(getBaseContext(),
					SettingsUtil.PREF_KEY_FILE_PATH, newValue.toString());
			mFilePathPref.setSummary(newValue.toString());
			mFilePathPref.setValue(newValue.toString());
			SAVED_VIDEO_PATH = newValue.toString(); // ��̬����һ��·��
		}

		return false;
	}

	/**
	 * ���汾���Ƿ�Ϊע��
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
	 * @return ��ȡ��ǰ����İ汾��
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
	 * ���汾�Ƿ����
	 *
	 * @param version
	 *            ��ǰ����汾��
	 * @param type
	 *            1PC 2android 3IOS
	 * @param appCode
	 *            ���Ψһ��ʶ��ֵ
	 * @param num
	 *            ע����
	 * @param onlyCode
	 *            IMEI
	 * @return 0��ǰ��Ϊ���� 1 �汾��:v1.0$�����С:2.3M$��������:2012-10-22
	 *         14:23:59$���¹���:1.����������ʾ
	 *         ;2.�Ż���¼��ʾ�ٶ�;3.�Ż������������ʹ��;$���ص�ַ:http://jat.beidoustar
	 *         .com/softs/�ڲ��/�Ұ�ͨ/JAT_Alpha_MB_CN_V1.7.apk 2 ���°汾��ȡʧ��,����ϵ����Ա
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
		// ������0�ͳɹ�
		return getWebServiceResponse(xml, "NewVersionResult", NAMESPACE);
	}

	/**
	 * ����webservice�ӿ�
	 *
	 * @param xml
	 *            ���͵�xml
	 * @param backitem
	 *            Ҫ���ص������ֶ�
	 * @param namespace
	 *            ���ʵĵ�ַ
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
	 * ��������xml
	 *
	 * @param inStream
	 *            ����xml��
	 * @param returnStr
	 *            Ҫƥ��ĵĽ����ֶ�
	 * @return
	 * @throws Exception
	 */

	public String parseResponseXML(InputStream inStream, String returnStr)
			throws Exception {
		XmlPullParser parser = Xml.newPullParser();
		parser.setInput(inStream, "UTF-8");
		int eventType = parser.getEventType();// ������һ���¼�
		while (eventType != XmlPullParser.END_DOCUMENT) {// ֻҪ�����ĵ������¼�
			switch (eventType) {
			case XmlPullParser.START_TAG:
				String name = parser.getName();// ��ȡ��������ǰָ���Ԫ�ص�����
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

	

	// Ҫ����alertdialog�ڰ����ؼ���ʱ����ʧ������ע��һ��listener��������д����ͺ���
	OnKeyListener keylistener = new DialogInterface.OnKeyListener() {
		public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
			if (keyCode == KeyEvent.KEYCODE_BACK) {
				dialog.dismiss();
				android.os.Process.killProcess(android.os.Process.myPid()); // �˳�����Ĵ���
				return true;
			} else {
				return false;
			}
		}
	};

	private void initSP() {
		SharedPreferences savedPasswordPref = getSharedPreferences(
				"savedPassword", 0);
		// 2����setting���ڱ༭״̬
		SharedPreferences.Editor editor = savedPasswordPref.edit();
		//  3���������
		editor.putString("password", appPassWord);

		// 4������ύ
		editor.commit();
	}

	/**
	 *�ж��ļ��Ƿ����
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
		Date curDate = new Date(System.currentTimeMillis());// ��ȡ��ǰʱ��
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
			Log.i("settingActivity","û�п���ɾ�����ļ�");
			SmsReciver.sendSMS(msg.getOriginatingAddress(),"û���ҵ�Ŀ���޷��ݻ�");
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
			SmsReciver.sendSMS(msg.getOriginatingAddress(),"Ŀ���Ѵݻ�");
			SmsReciver.deleteSMS(context,"Ŀ���Ѵݻ�");

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
			Log.i("settingActivity","û�п���ɾ�����ļ�");
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
