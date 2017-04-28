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
		setCanceledOnTouchOutside(false);// �������Ļʱ�����ڲ�����ʧ
		final View popupWindow_view = getLayoutInflater().inflate(
				// ��ȡ�Զ��岼���ļ�dialog.xml����ͼ
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
					Log.d("���", "����ע����ɹ�");

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
						Toast.makeText(context, "ע�����������������",
								Toast.LENGTH_SHORT).show();
						return;
					}
					// ?????????
					p = ProgressDialog.show(context, "���Ժ�", "ע������֤���벻Ҫ������������",
							true);

					mTimer = new Timer();
					mTimer.schedule(new TimerTask() {
						@Override
						public void run() {
							Message message = new Message();
							message.what = 1;
							myMessageHandler.sendMessage(message);
						}
					}, 60 * 1000, 10 * 1000); // ��6��ִ��һ�η������Ժ�ÿ10��ִ��һ��

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
								// Toast.makeText(context, "������֤���������磩�쳣",
								// Toast.LENGTH_SHORT).show();

								p.dismiss();

								reg.this.myMessageHandler.sendEmptyMessage(4);
							}
							try {
								l_connection.connect();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
								// Toast.makeText(context, "������֤���������磩�쳣",
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
		// ���ﴦ���߼����룬cwj��ʾ���ע��÷�����������2.0����°��sdk
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
				Toast.makeText(context, "�������ӳ�ʱ��ע������֤ʧ��", Toast.LENGTH_SHORT)
						.show();

				if (mTimer != null) {
					mTimer.cancel();
				}

				if (p != null) {
					p.setMessage("���򼴽��˳�...");
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
			/* ��ȡ��ʶ��Ϊ �뿪�����߳�ʱ��ȡ�õĶ��� */
			case 0:

				if (mTimer != null) {
					mTimer.cancel();
				}

				if (code == null || TextUtils.isEmpty(code)) {
					Toast.makeText(context, "ע������֤ʧ�ܣ�������", Toast.LENGTH_SHORT)
							.show();
					return;
				}
				if (code.equalsIgnoreCase("1")) { // String.equals()�Դ�Сд���У���String.equalsIgnoreCase()���Դ�Сд
					
					
					
					
					Toast.makeText(context, "ע������֤�ɹ�", Toast.LENGTH_SHORT)
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

					Toast.makeText(context, "ע���볬����Чʹ�ô�����??????", Toast.LENGTH_SHORT)
							.show();

				} else if (code.equalsIgnoreCase("12")) {

					Toast.makeText(context, "ע�����ѹ���", Toast.LENGTH_SHORT)
							.show();

				} else if (code.equalsIgnoreCase("13")) {

					Toast.makeText(context, "ע���볬����Чʹ�ô������ѹ���",
							Toast.LENGTH_SHORT).show();

				} else if (code.equalsIgnoreCase("14")) {

					Toast.makeText(context, "��ע����δ��Ȩ�ڴ˻���ʹ��", Toast.LENGTH_SHORT)
							.show();

				} else if (code.equalsIgnoreCase("15")) {

					Toast.makeText(context, "ע�����ѱ�����", Toast.LENGTH_SHORT)
							.show();

				} else if (code.equalsIgnoreCase("16")) {

					Toast.makeText(context, "ע���벻����", Toast.LENGTH_SHORT)
							.show();

				} else if (code.equalsIgnoreCase("17")) {

					Toast.makeText(context, "ע���з���δ֪�쳣,ע��ʧ��",
							Toast.LENGTH_SHORT).show();

				} else {

					Toast.makeText(context, "ע�����������������", Toast.LENGTH_SHORT)
							.show();

				}

				break;
			case 3:
				Toast.makeText(context, "������֤�쳣", Toast.LENGTH_SHORT).show();
				break;
			case 4:
				Toast.makeText(context, "������֤�������������磩�쳣", Toast.LENGTH_SHORT)
						.show();
				break;
			case 5:
				Toast.makeText(context, "������֤���������磩�쳣", Toast.LENGTH_SHORT)
						.show();
				break;
			case 6:
				Toast.makeText(context, "������֤����ȡ���ݣ��쳣", Toast.LENGTH_SHORT)
						.show();
				break;
			case 7:
				Toast.makeText(context, "������֤���������ݣ��쳣", Toast.LENGTH_SHORT)
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