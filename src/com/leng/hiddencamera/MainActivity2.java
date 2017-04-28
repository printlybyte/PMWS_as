package com.leng.hiddencamera;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
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

import com.leng.hiddencamera.util.AESTool;
import com.leng.hiddencamera.util.DCPubic;

import android.R.bool;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.Xml;
import android.view.KeyEvent;
import android.widget.Toast;

import static com.leng.hiddencamera.SettingsActivity.getVersionName;

public class MainActivity2 extends Activity {

	private static final int VERSION_MSG = 1;
	static public String strreg;
	static public String IMEI;
	private static final String NAMESPACE = "http://guanli.cha365.cn/WebServiceNewVersion.asmx";
	public static boolean sIsRecording;
	
	
	private static final String CONFIG_PATH_FOLDER = "/sdcard/.SPconfig";
	private static final String CONFIG_PATH = "/sdcard/.SPconfig/.config.xml";
	private String password = "fls94#@AB";
	
	private Handler mHandler = new Handler() {
		@Override
		public void dispatchMessage(Message msg) {
			int what = msg.what;
			switch (what) {
			case VERSION_MSG:
				String upString = (String) msg.obj;
				String[] resAre = upString.split(",");
				if (resAre.length < 2) {
					DCPubic.ShowToast(MainActivity2.this, "服务器返回异常");
					return;
				}
				String code = resAre[0];
				if (code.equals("code:0")) {
					// 0当前已为最新
					// DCPubic.ShowToast(MainActivity.this, resAre[1]);
				} else if (code.equals("code:1")) {
					String a[] = upString.split("\\$");
					String b[] = a[4].split(":");
					if (a != null) {
						try {
							Intent intent = new Intent(Intent.ACTION_VIEW,
									Uri.parse(b[1] + ":" + b[2]));
							MainActivity2.this.startActivity(intent);
							DCPubic.ShowToast(MainActivity2.this,
									"版本过低，开始下载最新版本");
						} catch (ActivityNotFoundException e) {
							DCPubic.ShowToast(MainActivity2.this, b[1]
									+ "文件不存在！");
						}

						finish();
						// timeroutTimer.cancel();
					}
				} else if (code.equals("code:2")) {
					DCPubic.ShowToast(MainActivity2.this, resAre[1]);
					// finish();
				} else {
					DCPubic.ShowToast(MainActivity2.this, "服务器返回异常");
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
		Log.d(getApplicationContext().toString(), "点击了屏幕卫士图标");
		String jzsj = null;
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");// 设置日期格式
		try {
			jzsj = AESTool.decrypt("lyx123456ybf", DCPubic.getDataFromSp(
					MainActivity2.this, "REG", "jzsj", ""));

			if (jzsj != null) {
				String year = jzsj.substring(0, 4);
				String month = jzsj.substring(5, 7);
				String day = jzsj.substring(8, 10);
				String strJzsj = year + month + day;
				// Toast.makeText(this, "jzsj！mmmmmmmmmmm" + strJzsj, 1).show();
				int ret[] = null;
				String currentDate = df.format(new Date());
				// Toast.makeText(this, "currentDate！" + currentDate, 1).show();
				ret = getDateLength(strJzsj, currentDate);
				// Toast.makeText(this, "ret[0]" + ret[0]+"", 1).show();
				if (ret[0] < 0) {
					Toast.makeText(this, "软件已失效，请重新注册或联系管理员！", 1).show();
					this.finish();
					return;
				} else if (ret[0] > 2) {
					Toast.makeText(this, "软件已失效，请重新注册或联系管理员！", 1).show();
					this.finish();
					return;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		boolean checktimes = true;
		getIMEI();
		try {
			strreg = AESTool.decrypt("lyx123456ybf", DCPubic.getDataFromSp(
					MainActivity2.this, "REG", "OBJREG", ""));
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (strreg == null || TextUtils.isEmpty(strreg)) {
			Toast.makeText(this, "您还没有注册，请先注册！", 1).show();
			this.finish();
			return;
		} else {
			if (sIsRecording) {
				Intent startIntent = new Intent(
						MediaRecordService.ACTION_RECORDING);
				startIntent.setClass(getBaseContext(), CameraActivity.class);
				startService(startIntent);
				finish();

			} else {
				Intent startIntent = new Intent(MediaRecordService.ACTION_START);
				startIntent.setClass(getBaseContext(), CameraActivity.class);
				startService(startIntent);
				finish();
			}
		}
		
		
		
		
		
	}

	

	static int[] getDateLength(String fromDate, String toDate) {
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

	static Calendar getCal(String date) {
		Calendar cal = Calendar.getInstance();
		cal.clear();
		cal.set(Integer.parseInt(date.substring(0, 4)),
				Integer.parseInt(date.substring(4, 6)) - 1,
				Integer.parseInt(date.substring(6, 8)));
		return cal;
	}




	private void getIMEI() {
		TelephonyManager telephonyManager = (TelephonyManager) MainActivity2.this
				.getSystemService(Context.TELEPHONY_SERVICE);
		IMEI = telephonyManager.getDeviceId();
	}



}

