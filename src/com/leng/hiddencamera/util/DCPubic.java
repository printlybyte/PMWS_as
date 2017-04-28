package com.leng.hiddencamera.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.telephony.TelephonyManager;
import android.widget.Toast;

public class DCPubic {
	public static void ShowToast(Context context, String text) {
		Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
	}

	/**
	 * @param context
	 * @param filename
	 * @param key
	 * @param defaut
	 * @return ���ص��Ǽ��ܵ�ע���룿
	 */
	public static String getDataFromSp(Context context, String filename,
			String key, String defaut) {
		SharedPreferences reg = context.getSharedPreferences(filename, 0);
		String regCode = reg.getString(key, defaut);
		return regCode;
	}

	public static String getIMEI(Context context) {
		String IMEI;
		TelephonyManager telephonyManager = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		IMEI = telephonyManager.getDeviceId();

		return IMEI;
	}
}
