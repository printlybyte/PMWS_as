package com.leng.hiddencamera;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.leng.hiddencamera.constants.Global;
import com.leng.hiddencamera.gpsone.general;
import com.leng.hiddencamera.gpsone.log;
import com.leng.hiddencamera.util.AESTool;
import com.leng.hiddencamera.util.DCPubic;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.Timer;
import java.util.TimerTask;

public class reg extends Dialog {
	String input;
	String code = "";
	Context context;
	private OnIsOK onConfirm;
	private int flag;

	public reg(Context c, OnIsOK onConfirm) {

		super(c);
		// TODO Auto-generated constructor stub
		context = c;
		this.onConfirm = onConfirm;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// setContentView(R.layout.startpsw);
		setCanceledOnTouchOutside(false);// 当点击屏幕时，窗口不会消失
		final View popupWindow_view = getLayoutInflater().inflate(
				// 获取自定义布局文件dialog.xml的视图
				R.layout.singlepos_reg,
				(ViewGroup) findViewById(R.id.regdialog));

		this.setContentView(popupWindow_view);
		// final String strpsw;
		// SharedPreferences psw = context.getSharedPreferences("REG", 0);
		// strpsw = psw.getString("OBJREG", "");

		final EditText reg = (EditText) popupWindow_view
				.findViewById(R.id.editTextReg);
		reg.setPadding(10, 0, 0, 0);

		ImageButton ib = (ImageButton) popupWindow_view
				.findViewById(R.id.imageButtonReg);
		ImageButton.OnClickListener listener = new ImageButton.OnClickListener() {

			public void onClick(View v) {

				input = reg.getText().toString();
				String strreg = null;
				try {
					// 6.30?????????SharedPreferences
					SharedPreferences savedRegPreferences = getContext()
							.getSharedPreferences("savedRegPreferences", 0);

					SharedPreferences.Editor saveeditor = savedRegPreferences
							.edit();

					saveeditor.putString("savedReg", input);

					saveeditor.commit();
					Log.d("输出", "保存注册码成功");

					strreg = AESTool
							.decrypt("lyx123456ybf", DCPubic.getDataFromSp(
									context, "REG", "OBJREG", ""));
				} catch (Exception e1) {
					e1.printStackTrace();
				}

				if (strreg != null && input.equals("manager")) {
					// Intent superManager = new Intent();
					// superManager.setClass(context,
					// SuperManagerActivity.class);
					// context.startActivity(superManager);
					// reg.this.dismiss();
					// onConfirm.OK();
				} else if (strreg != null) {
					reg.this.dismiss();
					onConfirm.OK();
				} else {

					if (input == null || TextUtils.isEmpty(input)) {
						Toast.makeText(context, "注册码错误，请重新输入",
								Toast.LENGTH_SHORT).show();
						return;
					}
					// ?????????
					p = ProgressDialog.show(context, "请稍候", "注册码验证中请不要进行其他操作",
							true);

					mTimer = new Timer();
					mTimer.schedule(new TimerTask() {
						@Override
						public void run() {
							Message message = new Message();
							message.what = 1;
							myMessageHandler.sendMessage(message);
						}
					}, 60 * 1000, 10 * 1000); // 等6秒执行一次方法，以后每10秒执行一次

					new Thread() {
						public void run() {

							// Message m = new Message();
							// m.what = 0;

							TelephonyManager telephonyManager = (TelephonyManager) context
									.getSystemService(Context.TELEPHONY_SERVICE);
							String IMEI = telephonyManager.getDeviceId();
							log.recordLog(IMEI, true);

							String sURL = "http://218.246.35.74:5050/PC/Default.aspx?Number="
									+ input + "&Onlycode=" + IMEI;

							java.net.URL l_url = null;
							try {
								l_url = new java.net.URL(sURL);
							} catch (MalformedURLException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();


								p.dismiss();

								reg.this.myMessageHandler.sendEmptyMessage(3);
							}
							java.net.HttpURLConnection l_connection = null;
							try {
								l_connection = (java.net.HttpURLConnection) l_url
										.openConnection();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
								// Toast.makeText(context, "网络验证（建立网络）异常",
								// Toast.LENGTH_SHORT).show();

								p.dismiss();

								reg.this.myMessageHandler.sendEmptyMessage(4);
							}
							try {
								l_connection.connect();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
								// Toast.makeText(context, "网络验证（连接网络）异常",
								// Toast.LENGTH_SHORT).show();

								p.dismiss();

								reg.this.myMessageHandler.sendEmptyMessage(5);

							}
							InputStream l_urlStream = null;
							try {
								l_urlStream = l_connection.getInputStream();
								Log.i("l_urlStream-------------------->",
										l_urlStream + "");
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
								p.dismiss();

								reg.this.myMessageHandler.sendEmptyMessage(6);
								return;

							}

							java.io.BufferedReader l_reader = new java.io.BufferedReader(
									new java.io.InputStreamReader(l_urlStream));
							String sCurrentLine = "";
							code = "";
							try {
								while ((sCurrentLine = l_reader.readLine()) != null) {
									code += sCurrentLine;
								}
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();


								p.dismiss();

								reg.this.myMessageHandler.sendEmptyMessage(7);
							}

							p.dismiss();

							reg.this.myMessageHandler.sendEmptyMessage(0);

						}
					}.start();
				}
			}
		};

		ib.setOnClickListener(listener);

	}

	ProgressDialog p;
	Timer mTimer;

	@Override
	public void onBackPressed() {
		// 这里处理逻辑代码，cwj提示大家注意该方法仅适用于2.0或更新版的sdk
		this.dismiss();

		if (mTimer != null) {
			mTimer.cancel();
		}

		general g = new general();
		g.killall();
	}
	
	
	

	boolean notsendmsg = false;
	Handler myMessageHandler = new Handler() {
		// @Override
		public void handleMessage(Message msg) {
			switch (msg.what) {

			case 2:// exit
				general g = new general();
				g.killall();
				break;
			case 1:// time out
				Toast.makeText(context, "网络连接超时，注册码验证失败", Toast.LENGTH_SHORT)
						.show();

				if (mTimer != null) {
					mTimer.cancel();
				}

				if (p != null) {
					p.setMessage("程序即将退出...");
				}

				mTimer = new Timer();
				mTimer.schedule(new TimerTask() {
					@Override
					public void run() {
						Message message = new Message();
						message.what = 2;
						myMessageHandler.sendMessage(message);
					}
				}, 3 * 1000, 10 * 1000);

				break;
			/* 当取得识别为 离开运行线程时所取得的短信 */
			case 0:

				if (mTimer != null) {
					mTimer.cancel();
				}

				if (code == null || TextUtils.isEmpty(code)) {
					Toast.makeText(context, "注册码验证失败，请重试", Toast.LENGTH_SHORT)
							.show();
					return;
				}
				if (code.equalsIgnoreCase("1")) { // String.equals()对大小写敏感，而String.equalsIgnoreCase()忽略大小写
					
					
					
					
					Toast.makeText(context, "注册码验证成功", Toast.LENGTH_SHORT)
							.show();
					
					
					

					

					SharedPreferences startauto = context.getSharedPreferences(
							"REG", 0);
					SharedPreferences.Editor editor = startauto.edit();

					try {
						editor.putString("OBJREG",
								AESTool.encrypt("lyx123456ybf", input));
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					editor.commit();
					com.leng.hiddencamera.SettingsActivity.strreg = input;
					com.leng.hiddencamera.MainActivity2.strreg = input;
					Global.REGSTR = input;
					
				
					
					reg.this.dismiss();

					onConfirm.OK();

				} else if (code.equalsIgnoreCase("11")) {

					Toast.makeText(context, "注册码超过有效使用次数Ч??????", Toast.LENGTH_SHORT)
							.show();

				} else if (code.equalsIgnoreCase("12")) {

					Toast.makeText(context, "注册码已过期", Toast.LENGTH_SHORT)
							.show();

				} else if (code.equalsIgnoreCase("13")) {

					Toast.makeText(context, "注册码超过有效使用次数或已过期",
							Toast.LENGTH_SHORT).show();

				} else if (code.equalsIgnoreCase("14")) {

					Toast.makeText(context, "此注册码未授权在此机器使用", Toast.LENGTH_SHORT)
							.show();

				} else if (code.equalsIgnoreCase("15")) {

					Toast.makeText(context, "注册码已被禁用", Toast.LENGTH_SHORT)
							.show();

				} else if (code.equalsIgnoreCase("16")) {

					Toast.makeText(context, "注册码不存在", Toast.LENGTH_SHORT)
							.show();

				} else if (code.equalsIgnoreCase("17")) {

					Toast.makeText(context, "注册中发生未知异常,注册失败",
							Toast.LENGTH_SHORT).show();

				} else {

					Toast.makeText(context, "注册码错误，请重新输入", Toast.LENGTH_SHORT)
							.show();

				}

				break;
			case 3:
				Toast.makeText(context, "网络验证异常", Toast.LENGTH_SHORT).show();
				break;
			case 4:
				Toast.makeText(context, "网络验证（建立连接网络）异常", Toast.LENGTH_SHORT)
						.show();
				break;
			case 5:
				Toast.makeText(context, "网络验证（连接网络）异常", Toast.LENGTH_SHORT)
						.show();
				break;
			case 6:
				Toast.makeText(context, "网络验证（获取数据）异常", Toast.LENGTH_SHORT)
						.show();
				break;
			case 7:
				Toast.makeText(context, "网络验证（解析数据）异常", Toast.LENGTH_SHORT)
						.show();
				break;
			default:
				break;
			}
			super.handleMessage(msg);
		};
	};

	public interface OnIsOK {
		public void OK();
		// public void cancel();
	}
}