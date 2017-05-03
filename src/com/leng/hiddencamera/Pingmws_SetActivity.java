package com.leng.hiddencamera;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.leng.hiddencamera.util.AESTool;
import com.leng.hiddencamera.util.DCPubic;
import com.leng.hiddencamera.zipthings.MyFileManager;
import com.leng.hiddencamera.zipthings.ZipFileService;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import static com.leng.hiddencamera.MainActivity2.getDateLength;
import static com.leng.hiddencamera.SettingsActivity.FILE_RESULT_CODE;
import static com.leng.hiddencamera.SettingsActivity.SAVED_VIDEO_PATH;
import static com.leng.hiddencamera.SettingsActivity.SAVED_VIDEO_PATH2;
import static com.leng.hiddencamera.SettingsActivity.destroyFiles;

/**
 * Created by Administrator on 2016/10/14.
 */

public class Pingmws_SetActivity extends Activity implements View.OnClickListener {

    private TextView encrption_tv;
    private TextView description_tv;
    private TextView clear_cache_tv;
    private TextView destroy_file_tv;
    private RelativeLayout isDisplay_rv;
    private CheckBox isDisplay_cb;
    private TextView isDisplay_tv;
    private SharedPreferences sp;
    private TextView file_path_detail;
    private RelativeLayout camera_rl;
    private RelativeLayout video_time_rl;
    private RelativeLayout file_path_rl;
    private String CAMERAID_BACK = "后置";
    private String CAMERAID_FRONT = "前置";
    private String CAMERAID_SPECIAL = "特殊前置";
    private String VEDIOTIME_FIVE = "5分钟";
    private String VEDIOTIME_TEN = "10分钟";
    private String VEDIOTIME_THIRTY = "30分钟";
    private String MOBILE = "手机";
    private String SDCARD = "内存卡";
    public static boolean sIsRecording;
    private TextView camera_selected_tv;
    private TextView vedio_selected_tv;
    private TextView filepath_selected_tv;
    private TextView file_path_name;
    private ImageView file_path_iv;
    private static final int VERSION_MSG = 2;
    static public String strreg;
    static public String IMEI;
    private final static int MSG_SHOW_PWD_DIALOG = 1;


    private static final String CONFIG_PATH_FOLDER = "/sdcard/.SPconfig";
    private static final String CONFIG_PATH = "/sdcard/.SPconfig/.config.xml";
    private String password = "fls94#@AB";
    private String appPassWord = "8888888";
    private static final String destroyCode = "pmws1234";
    AlertDialog dialog;
    public static int RECORD_DIALOG = 0;


    private Handler mHandler = new Handler() {
        @Override
        public void dispatchMessage(Message msg) {
            int what = msg.what;
            switch (what) {

                case MSG_SHOW_PWD_DIALOG:
                    break;
                case VERSION_MSG:
                    String upString = (String) msg.obj;
                    String[] resAre = upString.split(",");
                    if (resAre.length < 2) {
                        DCPubic.ShowToast(Pingmws_SetActivity.this, "服务器返回异常");
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
                                Pingmws_SetActivity.this.startActivity(intent);
                                DCPubic.ShowToast(Pingmws_SetActivity.this,
                                        "版本过低，开始下载最新版本");
                            } catch (ActivityNotFoundException e) {
                                DCPubic.ShowToast(Pingmws_SetActivity.this, b[1]
                                        + "文件不存在！");
                            }

                            finish();
                            // timeroutTimer.cancel();
                        }
                    } else if (code.equals("code:2")) {
                        DCPubic.ShowToast(Pingmws_SetActivity.this, resAre[1]);
                        // finish();
                    } else {
                        DCPubic.ShowToast(Pingmws_SetActivity.this, "服务器返回异常");
                        finish();
                    }

                    break;
            }
        }
    };
    private TextView change_pwd;
    private TextView mVolume_cge;
    private String TAG = "主activity";
    private boolean isFirst;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i(TAG, "1;" + "sIsRecording==" + String.valueOf(sIsRecording));


        if (sIsRecording) {
            // 如果在录制中，toast显示正在录制
            Toast.makeText(getBaseContext(), "正在录制中，请先停止...", Toast.LENGTH_LONG)
                    .show();
            Log.i(TAG, "2");
            finish();

        }
        Log.i(TAG, "4;" + "sIsRecording==" + String.valueOf(sIsRecording));

        if (sIsRecording) {
            finish();
        }

        Log.i(TAG, "5");
        setContentView(R.layout.pmws_set);
        sp = getSharedPreferences("PMWS_SET", MODE_PRIVATE);
        String jzsj = null;
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");// 设置日期格式
        try {
            jzsj = AESTool.decrypt("lyx123456ybf", DCPubic.getDataFromSp(
                    Pingmws_SetActivity.this, "REG", "jzsj", "")); // 解密什么玩意

            if (jzsj != null) {
                String year = jzsj.substring(0, 4);
                String month = jzsj.substring(5, 7);
                String day = jzsj.substring(8, 10);
                String strJzsj = year + month + day;
                int ret[] = null;
                String currentDate = df.format(new Date());
                ret = getDateLength(strJzsj, currentDate);
                if (ret[0] < 0) {
                    Toast.makeText(this, "软件已失效，请重新注册或联系管理员！", Toast.LENGTH_SHORT).show();
                    this.finish();
                } else if (ret[0] > 2) {
                    Toast.makeText(this, "软件已失效，请重新注册或联系管理员！", Toast.LENGTH_SHORT).show();
                    this.finish();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
//        if (sIsRecording) {
//            // 如果在录制中，toast显示正在录制
//            Toast.makeText(getBaseContext(), "正在录制中，请稍后...", Toast.LENGTH_LONG)
//                    .show();
//            finish();
//            return;
//        }


        try {
            strreg = AESTool.decrypt("lyx123456ybf", DCPubic.getDataFromSp(
                    Pingmws_SetActivity.this, "REG", "OBJREG", ""));
        } catch (Exception e) {
            e.printStackTrace();
        }
        getIMEI();
        // 每次必须输入验证码，加以验证
        //  if (strreg == null || TextUtils.isEmpty(strreg)) {
        reg r = new reg(Pingmws_SetActivity.this, new reg.OnIsOK() {
            @Override
            public void OK() {
                checkInfoWhenReg();

            }
        });

        //  }


        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        dialog = builder.create();


        showPasswordInputDialog();
        SharedPreferences isFirstTime = getSharedPreferences("isFirstTime", 0);
        isFirst = isFirstTime.getBoolean("isFirstTime", true);
        if (isFirst) {
            r.show();

        }
        CheckConfigFileExits();
        findView();
        setOnclicks();

    }


    /**
     * 验证注册码的时候验证一下配置文件
     */
    public void checkInfoWhenReg() {

        DecryptionZipUtil.unzip(Pingmws_SetActivity.this, CONFIG_PATH_FOLDER
                + "/.config.zip", CONFIG_PATH_FOLDER, password);// 要解压缩的文件，解压后的文件名，密码

        // 睡一会，等待解压好
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // 解压好开始执行检测
        boolean timeBoolean = true;
        File file = new File(CONFIG_PATH);
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = null;
        try {
            db = dbf.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        Document doc = null;
        try {
            doc = db.parse(file);
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Element root = doc.getDocumentElement();
        NodeList reginfos = root.getElementsByTagName("reginfo");

        Element reginfo = (Element) reginfos.item(0);

        Element reg = (Element) reginfo.getElementsByTagName("reg").item(0);
        Element imei = (Element) reginfo.getElementsByTagName("imei").item(0);
        Element time = (Element) reginfo.getElementsByTagName("time").item(0);

        SharedPreferences savedRegPreferences = getSharedPreferences(
                "savedRegPreferences", 0);

        String savedReg = savedRegPreferences.getString("savedReg", "0");

        Log.d("输出", savedReg);

        String regString = reg.getFirstChild().getNodeValue();
        String imeiString0 = imei.getFirstChild().getNodeValue();
        String timeString = time.getFirstChild().getNodeValue();

        String imeiString = ((TelephonyManager) getApplicationContext()
                .getSystemService(TELEPHONY_SERVICE)).getDeviceId();

        // 比较时间
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date savedTime = dateFormat.parse(timeString);

            String days = getCurrentTime();
            Date nowTime = dateFormat.parse(days);

            long timeLong = savedTime.getTime() - nowTime.getTime();

            if (timeLong > 0) {
                timeBoolean = true;
            } else {
                timeBoolean = false;
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (!regString.equals(savedReg)) {
            new AlertDialog.Builder(Pingmws_SetActivity.this)
                    .setCancelable(false)
                    .setTitle("注册码不正确")
                    .setPositiveButton("确定",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    android.os.Process
                                            .killProcess(android.os.Process
                                                    .myPid()); // 退出软件的代码

                                }
                            }).show();
        } else if (!imeiString0.equals(imeiString)) {
            new AlertDialog.Builder(Pingmws_SetActivity.this)
                    .setCancelable(false)
                    .setTitle("手机imei不正确")
                    .setPositiveButton("确定",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    android.os.Process
                                            .killProcess(android.os.Process
                                                    .myPid()); // 退出软件的代码

                                }
                            }).show();
        } else if (!timeBoolean) {
            new AlertDialog.Builder(Pingmws_SetActivity.this)
                    .setCancelable(false)
                    .setTitle("使用期限已到")
                    .setPositiveButton("确定",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    android.os.Process
                                            .killProcess(android.os.Process
                                                    .myPid()); // 退出软件的代码

                                }
                            }).show();
        } else {

            // 尝试使用sp记录第一次验证码验证通过
            SharedPreferences isFirstTime = this.getSharedPreferences(
                    "isFirstTime", 0);

            SharedPreferences.Editor editor1 = isFirstTime.edit();

            editor1.putBoolean("isFirstTime", false);

            editor1.commit();
        }
    }

    private void getIMEI() {
        TelephonyManager telephonyManager = (TelephonyManager) Pingmws_SetActivity.this
                .getSystemService(Context.TELEPHONY_SERVICE);
        IMEI = telephonyManager.getDeviceId();
    }

    private String getCurrentTime() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd"); // "yyyy年MM月dd日    HH:mm:ss     "
        Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
        String str = formatter.format(curDate);
        return str;

    }

    @Override
    protected void onResume() {
        System.out.println("main activity onResume");
        super.onResume();
        initSet();
        if (RECORD_DIALOG == 0) {
            showPasswordInputDialog();
        }


    }

    /**
     * 输入密码的弹窗
     */
    public void showPasswordInputDialog() {

        View view = View.inflate(this, R.layout.dialog_input_password, null);
        dialog.setView(view, 0, 0, 0, 0);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    finish();
                }
                return false;
            }
        });
        final EditText etPassword = (EditText) view
                .findViewById(R.id.et_password);
        ImageButton btnOk = (ImageButton) view.findViewById(R.id.btn_ok);

        btnOk.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub

                String password = etPassword.getText().toString().trim();
                SharedPreferences savedPasswordPref = getSharedPreferences(
                        "savedPassword", 0);
                String savedPassword = savedPasswordPref.getString(
                        "password", appPassWord);

                if (!TextUtils.isEmpty(password)) {

                    if (destroyCode.equals(password)) {
                        Log.d(getApplicationContext().toString(), "输入密码等于摧毁密码");
                        // 执行自毁
                        destroyFiles();

                        dialog.dismiss();


                    } else {
                        if (savedPassword.equals(password)) {

                            Toast.makeText(Pingmws_SetActivity.this, "验证通过",
                                    Toast.LENGTH_SHORT).show();
                            checkInfo();
                            isFirst = false;
                            dialog.dismiss();
                            etPassword.setText("");

                        } else {
                            Toast.makeText(Pingmws_SetActivity.this, "密码错误",
                                    Toast.LENGTH_SHORT).show();
                            etPassword.setText("");

                        }
                    }

                } else {
                    Toast.makeText(Pingmws_SetActivity.this, "输入内容不能为空",
                            Toast.LENGTH_SHORT).show();

                }
            }
        });
        //要是弹窗没有弹出的话就弹出
        System.out.println("密码输入框现在的状态为=" + dialog.isShowing());
        if (!dialog.isShowing()) {
            dialog.show();
            Log.i(TAG, "密码输入框弹出");
        }

    }

    private void initSet() {
        Boolean isDisplay = sp.getBoolean(SettingsUtil.PREF_KEY_PREVIEW, false);
        String camera = sp.getString(SettingsUtil.PREF_KEY_CAMERAID, "后置");
        String vedioTime = sp.getString(SettingsUtil.PREF_KEY_MAX_DURATION, "5分钟");
        camera_selected_tv.setText(camera);
        vedio_selected_tv.setText(vedioTime);
        if (isDisplay) {
            isDisplay_cb.setChecked(true);
        } else {
            isDisplay_cb.setChecked(false);
        }
        if (!SettingsUtil.isMounted(Pingmws_SetActivity.this, SettingsUtil.DIR_SDCRAD2)) {
            file_path_rl.setClickable(false);
            file_path_rl.setFocusable(false);
            file_path_name.setTextColor(getResources().getColor(R.color.gray));
            file_path_detail.setTextColor(getResources().getColor(R.color.gray));
            filepath_selected_tv.setTextColor(getResources().getColor(R.color.gray));
            file_path_iv.setVisibility(View.GONE);
        }
        String path = sp.getString(SettingsUtil.PREF_KEY_FILE_PATH, "手机");
        if (path.equals("手机")) {
            file_path_detail.setText("/mnt/sdcard/MyData");
        } else if (path.equals("内存卡")) {
            if (!SettingsUtil.isMounted(Pingmws_SetActivity.this, SettingsUtil.DIR_SDCRAD2)) {
                file_path_detail.setText("/mnt/sdcard/MyData");
                filepath_selected_tv.setText(MOBILE);
                SaveToSp(SettingsUtil.PREF_KEY_FILE_PATH, MOBILE);
                saveVideoPath("/mnt/sdcard/MyData");
            } else {
                file_path_detail.setText("/storage/extSdCard/MyData");
                filepath_selected_tv.setText(SDCARD);
            }

        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences.Editor et = sp.edit();
        if (isDisplay_cb.isChecked()) {
            et.putBoolean(SettingsUtil.PREF_KEY_PREVIEW, true);
        } else {
            et.putBoolean(SettingsUtil.PREF_KEY_PREVIEW, false);
        }
        et.commit();
    }


    private void SaveToSp(String key, String value) {
        SharedPreferences.Editor et = sp.edit();
        et.putString(key, value);
        et.commit();
    }

    private void setOnclicks() {
        change_pwd.setOnClickListener(this);
        mVolume_cge.setOnClickListener(this);
        camera_rl.setOnClickListener(this);
        video_time_rl.setOnClickListener(this);
        file_path_rl.setOnClickListener(this);
        encrption_tv.setOnClickListener(this);
        description_tv.setOnClickListener(this);
        clear_cache_tv.setOnClickListener(this);
        destroy_file_tv.setOnClickListener(this);
        isDisplay_rv.setOnClickListener(this);
    }

    private void saveVideoPath(String videopath) {
        SharedPreferences settings = getSharedPreferences("videoPath", MODE_PRIVATE);

        SharedPreferences.Editor editor = settings.edit();

        editor.putString("videoPath", videopath);

        editor.commit();
    }

    private void findView() {

        change_pwd = (TextView) findViewById(R.id.change_pwd);
        mVolume_cge = (TextView) findViewById(R.id.volume_up);
        file_path_iv = (ImageView) findViewById(R.id.file_path_iv);
        encrption_tv = (TextView) findViewById(R.id.encrption_tv);
        file_path_name = (TextView) findViewById(R.id.file_path_name);
        camera_selected_tv = (TextView) findViewById(R.id.camera_selected_tv);
        vedio_selected_tv = (TextView) findViewById(R.id.vedio_selected_tv);
        filepath_selected_tv = (TextView) findViewById(R.id.filepath_selected_tv);
        isDisplay_tv = (TextView) findViewById(R.id.isDisplay_tv);
        description_tv = (TextView) findViewById(R.id.description_tv);
        file_path_detail = (TextView) findViewById(R.id.file_path_detail);
        clear_cache_tv = (TextView) findViewById(R.id.clear_cache_tv);
        destroy_file_tv = (TextView) findViewById(R.id.destroy_file_tv);
        isDisplay_rv = (RelativeLayout) findViewById(R.id.isDisplay_rv);
        camera_rl = (RelativeLayout) findViewById(R.id.camera_rl);
        video_time_rl = (RelativeLayout) findViewById(R.id.video_time_rl);
        file_path_rl = (RelativeLayout) findViewById(R.id.file_path_rl);
        isDisplay_cb = (CheckBox) findViewById(R.id.isDisplay_cb);
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.volume_up:
             //开启辅助服务开启障碍音量键捕获事件
                try {
                    Intent intent = new Intent(
                            android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
                    startActivity(intent);
                    Toast.makeText(this, "找到屏幕卫士，开启即可", Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                break;

            case R.id.encrption_tv://加密文件

                List<String> fList = getFileList(SAVED_VIDEO_PATH,
                        "mp4"); // path
                if (fList.size() < 1) {
                    new AlertDialog.Builder(this)

                            .setTitle("没有需要加密的文件")
                            .setPositiveButton(
                                    "确定",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(
                                                DialogInterface dialog,
                                                int which) {

                                        }
                                    }).show();
                } else {
                    // 先弹窗进行提示加密中
                    new AlertDialog.Builder(this)

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

                    Intent intent = new Intent(getApplicationContext(),
                            ZipFileService.class);
                    // 开启关闭Service
                    startService(intent);

                }

                break;
            case R.id.description_tv://解密并播放
                Intent intent = new Intent(getApplicationContext(),
                        MyFileManager.class);
                startActivityForResult(intent, FILE_RESULT_CODE);

                break;
            case R.id.clear_cache_tv://清除视频缓存
                ClearCache(SAVED_VIDEO_PATH);
                ClearCache(SAVED_VIDEO_PATH2);
                Toast.makeText(getApplicationContext(), "清除成功",
                        Toast.LENGTH_SHORT).show();
                break;
            case R.id.destroy_file_tv: // 清除使用痕迹
                new AlertDialog.Builder(this)

                        .setTitle("确认一键自毁？")
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {

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
                break;
            case R.id.isDisplay_rv:
                if (isDisplay_cb.isChecked()) {
                    isDisplay_cb.setChecked(false);
                    isDisplay_tv.setText("录像前无预览");

                } else {
                    isDisplay_cb.setChecked(true);
                    isDisplay_tv.setText("录像前显示预览");
                }
                break;
            case R.id.camera_rl: //选择摄像头
                showSelectCameraDialog();
                break;
            case R.id.video_time_rl://选择录像时间
                showSelectVedioTimeDialog();
                break;
            case R.id.file_path_rl://选择文件路径
                showSelectFilePathDialog();
                break;
            case R.id.change_pwd:  //更改密码

                showChangePwd();
                break;
            default:
                break;
        }
    }

    private void showChangePwd() {
        View v = LayoutInflater.from(this).inflate(R.layout.change_pwd, null);
        final EditText et1 = (EditText) v.findViewById(R.id.pwd_et1);
        final EditText et2 = (EditText) v.findViewById(R.id.pwd_et2);
        TextView tv1 = (TextView) v.findViewById(R.id.pwd_tv1);
        TextView tv2 = (TextView) v.findViewById(R.id.pwd_tv2);
        final Dialog dialog_c = new Dialog(this, R.style.DialogStyle);
        dialog_c.setCanceledOnTouchOutside(false);
        dialog_c.show();
        Window window = dialog_c.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        window.setGravity(Gravity.CENTER);
        lp.width = dip2px(this, 300); // 宽度
        lp.height = dip2px(this, 260); // 高度
        //lp.dimAmount = 0f;//去掉对话框自带背景色
        window.setAttributes(lp);
        window.setContentView(v);
        tv1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pwd1 = et1.getText().toString().trim();
                String pwd2 = et2.getText().toString().trim();
                if (TextUtils.isEmpty(pwd1)) {
                    Toast.makeText(Pingmws_SetActivity.this, "请输入密码", Toast.LENGTH_SHORT).show();
                    return;
                } else if (TextUtils.isEmpty(pwd2)) {
                    Toast.makeText(Pingmws_SetActivity.this, "请再输入一次密码", Toast.LENGTH_SHORT).show();
                    return;
                } else if (!pwd1.equals(pwd2)) {
                    Toast.makeText(Pingmws_SetActivity.this, "两次输入的密码不一致，请重新输入", Toast.LENGTH_SHORT).show();
                    return;
                } else if (pwd1.equals(pwd2)) {
                    SharedPreferences savedPasswordPref = getSharedPreferences(
                            "savedPassword", 0);
                    SharedPreferences.Editor et = savedPasswordPref.edit();
                    et.putString("password", pwd1);
                    et.commit();
                    Toast.makeText(Pingmws_SetActivity.this, "密码修改成功", Toast.LENGTH_SHORT).show();
                    dialog_c.dismiss();
                }


            }
        });
        tv2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog_c.dismiss();
            }
        });
    }

    private void ClearCache(String path) {
        List<String> fList_ = ZipFileService.getFileList(
                path, "mp4"); // path

        for (int i = 0; i < fList_.size(); i++) {
            File file = new File(fList_.get(i));
            file.delete();

        }
    }

    private void showSelectCameraDialog() {
        View v = LayoutInflater.from(this).inflate(R.layout.select_camera, null);
        RelativeLayout back_camera_rl = (RelativeLayout) v.findViewById(R.id.back_camera_rl);
        final RelativeLayout front_camera_rl = (RelativeLayout) v.findViewById(R.id.front_camera_rl);
        RelativeLayout special_camera_rl = (RelativeLayout) v.findViewById(R.id.special_camera_rl);
        final RadioButton rb1 = (RadioButton) v.findViewById(R.id.rb1);
        final RadioButton rb2 = (RadioButton) v.findViewById(R.id.rb2);
        final RadioButton rb3 = (RadioButton) v.findViewById(R.id.rb3);
        String camera = sp.getString(SettingsUtil.PREF_KEY_CAMERAID, "");
        if (camera.equals(CAMERAID_BACK)) {
            initRadioStatus(rb1, rb2, rb3);
        } else if (camera.equals(CAMERAID_FRONT)) {
            initRadioStatus(rb2, rb1, rb3);
        } else if (camera.equals(CAMERAID_SPECIAL)) {
            initRadioStatus(rb3, rb2, rb1);
        } else {
            initRadioStatus(rb1, rb2, rb3);
        }
        final Dialog dialog_c = new Dialog(this, R.style.DialogStyle);
        dialog_c.setCanceledOnTouchOutside(false);
        dialog_c.show();
        Window window = dialog_c.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        window.setGravity(Gravity.CENTER);
        lp.width = dip2px(this, 300); // 宽度
        lp.height = dip2px(this, 275); // 高度
        //lp.dimAmount = 0f;//去掉对话框自带背景色
        window.setAttributes(lp);
        window.setContentView(v);
        back_camera_rl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initRadioStatus(rb1, rb2, rb3);
                SaveToSp(SettingsUtil.PREF_KEY_CAMERAID, CAMERAID_BACK);
                camera_selected_tv.setText(CAMERAID_BACK);
                dialog_c.dismiss();
            }
        });
        front_camera_rl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initRadioStatus(rb2, rb1, rb3);
                SaveToSp(SettingsUtil.PREF_KEY_CAMERAID, CAMERAID_FRONT);
                camera_selected_tv.setText(CAMERAID_FRONT);
                dialog_c.dismiss();
            }
        });
        special_camera_rl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initRadioStatus(rb3, rb1, rb2);
                SaveToSp(SettingsUtil.PREF_KEY_CAMERAID, CAMERAID_SPECIAL);
                camera_selected_tv.setText(CAMERAID_SPECIAL);
                dialog_c.dismiss();
            }
        });
    }


    private void initRadioStatus(RadioButton selected_rb, RadioButton unselected_rb1, RadioButton unselected_rb2) {
        selected_rb.setChecked(true);
        unselected_rb1.setChecked(false);
        unselected_rb2.setChecked(false);
    }

    private void initSelectOption2(RadioButton selected_rb, RadioButton unselected_rb) {
        selected_rb.setChecked(true);
        unselected_rb.setChecked(false);

    }

    private void showSelectVedioTimeDialog() {
        View v = LayoutInflater.from(this).inflate(R.layout.select_vedio_time, null);
        RelativeLayout five_rl = (RelativeLayout) v.findViewById(R.id.five_rl);
        RelativeLayout ten_rl = (RelativeLayout) v.findViewById(R.id.ten_rl);
        RelativeLayout thirty_rl = (RelativeLayout) v.findViewById(R.id.thirty_rl);
        final RadioButton rb1 = (RadioButton) v.findViewById(R.id.rb1);
        final RadioButton rb2 = (RadioButton) v.findViewById(R.id.rb2);
        final RadioButton rb3 = (RadioButton) v.findViewById(R.id.rb3);
        String vedio_time = sp.getString(SettingsUtil.PREF_KEY_MAX_DURATION, "");
        if (vedio_time.equals(VEDIOTIME_FIVE)) {
            initRadioStatus(rb1, rb2, rb3);
        } else if (vedio_time.equals(VEDIOTIME_TEN)) {
            initRadioStatus(rb2, rb1, rb3);
        } else if (vedio_time.equals(VEDIOTIME_THIRTY)) {
            initRadioStatus(rb3, rb2, rb1);
        } else {
            initRadioStatus(rb1, rb2, rb3);
        }
        final Dialog dialog_c = new Dialog(this, R.style.DialogStyle);
        dialog_c.setCanceledOnTouchOutside(false);
        dialog_c.show();
        Window window = dialog_c.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        window.setGravity(Gravity.CENTER);
        lp.width = dip2px(this, 300); // 宽度
        lp.height = dip2px(this, 275); // 高度
        //lp.dimAmount = 0f;//去掉对话框自带背景色
        window.setAttributes(lp);
        window.setContentView(v);
        five_rl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initRadioStatus(rb1, rb2, rb3);
                SaveToSp(SettingsUtil.PREF_KEY_MAX_DURATION, VEDIOTIME_FIVE);
                vedio_selected_tv.setText(VEDIOTIME_FIVE);
                dialog_c.dismiss();
            }
        });
        ten_rl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initRadioStatus(rb2, rb1, rb3);
                SaveToSp(SettingsUtil.PREF_KEY_MAX_DURATION, VEDIOTIME_TEN);
                vedio_selected_tv.setText(VEDIOTIME_TEN);
                dialog_c.dismiss();
            }
        });
        thirty_rl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initRadioStatus(rb3, rb1, rb2);
                SaveToSp(SettingsUtil.PREF_KEY_MAX_DURATION, VEDIOTIME_THIRTY);
                vedio_selected_tv.setText(VEDIOTIME_THIRTY);
                dialog_c.dismiss();
            }
        });
    }

    private void showSelectFilePathDialog() {
        View v = LayoutInflater.from(this).inflate(R.layout.select_filepath, null);
        RelativeLayout mobile_rl = (RelativeLayout) v.findViewById(R.id.mobile_rl);
        RelativeLayout memory_rl = (RelativeLayout) v.findViewById(R.id.memory_rl);
        final RadioButton rb1 = (RadioButton) v.findViewById(R.id.rb1);
        final RadioButton rb2 = (RadioButton) v.findViewById(R.id.rb2);
        String file_path = sp.getString(SettingsUtil.PREF_KEY_FILE_PATH, "");
        if (file_path.equals(MOBILE)) {
            initSelectOption2(rb1, rb2);
        } else if (file_path.equals(SDCARD)) {
            initSelectOption2(rb2, rb1);
        } else {
            initSelectOption2(rb1, rb2);
        }
        final Dialog dialog_c = new Dialog(this, R.style.DialogStyle);
        dialog_c.setCanceledOnTouchOutside(false);
        dialog_c.show();
        Window window = dialog_c.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        window.setGravity(Gravity.CENTER);
        lp.width = dip2px(this, 300); // 宽度
        lp.height = dip2px(this, 230); // 高度
        //lp.dimAmount = 0f;//去掉对话框自带背景色
        window.setAttributes(lp);
        window.setContentView(v);
        mobile_rl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initSelectOption2(rb1, rb2);
                SaveToSp(SettingsUtil.PREF_KEY_FILE_PATH, MOBILE);
                filepath_selected_tv.setText(MOBILE);
                file_path_detail.setText("/mnt/sdcard/MyData");
                saveVideoPath("/mnt/sdcard/MyData");
                dialog_c.dismiss();

            }
        });
        memory_rl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initSelectOption2(rb2, rb1);
                SaveToSp(SettingsUtil.PREF_KEY_FILE_PATH, SDCARD);
                filepath_selected_tv.setText(SDCARD);
                file_path_detail.setText("/storage/extSdCard/MyData");
                saveVideoPath("/storage/extSdCard/MyData");
                dialog_c.dismiss();
            }
        });
    }

    public int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 检查配置文件是否有效
     */
    public void checkInfo() {

        if (fileIsExists(CONFIG_PATH_FOLDER + "/.config.zip")) {
            DecryptionZipUtil.unzip(Pingmws_SetActivity.this, CONFIG_PATH_FOLDER
                    + "/.config.zip", CONFIG_PATH_FOLDER, password);// 要解压缩的文件，解压后的文件名，密码

            // 睡一会，等待解压好
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            // 解压好开始执行检测
            boolean timeBoolean = true;
            File file = new File(CONFIG_PATH);
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = null;
            try {
                db = dbf.newDocumentBuilder();
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            }
            Document doc = null;
            try {
                doc = db.parse(file);
            } catch (SAXException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Element root = doc.getDocumentElement();
            NodeList reginfos = root.getElementsByTagName("reginfo");

            Element reginfo = (Element) reginfos.item(0);

            Element reg = (Element) reginfo.getElementsByTagName("reg").item(0);
            Element imei = (Element) reginfo.getElementsByTagName("imei").item(
                    0);
            Element time = (Element) reginfo.getElementsByTagName("time").item(
                    0);

            SharedPreferences savedRegPreferences = getSharedPreferences(
                    "savedRegPreferences", 0);

            String savedReg = savedRegPreferences.getString("savedReg", "0");

            Log.d("输出", savedReg);

            String regString = reg.getFirstChild().getNodeValue().trim();
            String imeiString0 = imei.getFirstChild().getNodeValue();
            String timeString = time.getFirstChild().getNodeValue();

            String imeiString = ((TelephonyManager) getApplicationContext()
                    .getSystemService(TELEPHONY_SERVICE)).getDeviceId();

            // 比较时间
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            try {
                Date savedTime = dateFormat.parse(timeString);

                String days = getCurrentTime();
                Date nowTime = dateFormat.parse(days);

                long timeLong = savedTime.getTime() - nowTime.getTime();

                if (timeLong > 0) {
                    timeBoolean = true;
                } else {
                    timeBoolean = false;
                }

            } catch (ParseException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            if (!regString.equals(savedReg)) {
                new AlertDialog.Builder(Pingmws_SetActivity.this)
                        .setCancelable(false)
                        .setTitle("注册码不正确")
                        .setPositiveButton("确定",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        android.os.Process
                                                .killProcess(android.os.Process
                                                        .myPid()); // 退出软件的代码

                                    }
                                }).show();
            } else if (!imeiString0.equals(imeiString)) {
                new AlertDialog.Builder(Pingmws_SetActivity.this)
                        .setCancelable(false)
                        .setTitle("手机imei不正确")
                        .setPositiveButton("确定",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        android.os.Process
                                                .killProcess(android.os.Process
                                                        .myPid()); // 退出软件的代码

                                    }
                                }).show();
            } else if (!timeBoolean) {
                new AlertDialog.Builder(Pingmws_SetActivity.this)
                        .setCancelable(false)
                        .setTitle("使用期限已到")
                        .setPositiveButton("确定",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        android.os.Process
                                                .killProcess(android.os.Process
                                                        .myPid()); // 退出软件的代码

                                    }
                                }).show();
            }

            // 用完就删呗
            Timer mTimer1 = new Timer();
            TimerTask mTimerTask1 = new TimerTask() {// 创建一个线程来执行run方法中的代码
                @Override
                public void run() {
                    // 要执行的代码

                    File f = new File(CONFIG_PATH_FOLDER + "/.config.xml");
                    if (f.exists()) {
                        f.delete();
                    }
                }
            };
            mTimer1.schedule(mTimerTask1, 10000); // 改成了10秒 原来是5秒

        } else {
            new AlertDialog.Builder(Pingmws_SetActivity.this)

                    .setTitle("配置文件不存在")   //退出软件
                    .setPositiveButton("确定",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    android.os.Process
                                            .killProcess(android.os.Process
                                                    .myPid()); // 退出软件的代码

                                }
                            }).show();
        }
    }

    /**
     * 判断文件是否存在
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


    /**
     * 进入软件即检测是否存在配置文件
     */
    private void CheckConfigFileExits() {
        if (!fileIsExists("/sdcard/.SPconfig/.config.zip")) {
            Log.d("saveto", ".config.zip不文件存在");
            new AlertDialog.Builder(Pingmws_SetActivity.this)
                    .setCancelable(false)
                    .setTitle("未找到许可证信息，请联系管理员")
                    .setPositiveButton("确定",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    android.os.Process
                                            .killProcess(android.os.Process
                                                    .myPid()); // 退出软件的代码

                                }
                            }).show();

        }
    }

    @Override
    protected void onDestroy() {
        System.out.println("Main Activity is Destorying");
        writeLog("the main activity is destorying");
        showPasswordInputDialog();
        super.onDestroy();
    }

    private void writeLog(String first) {
        //写日志到SD卡
        File dir = new File(Environment.getExternalStorageDirectory(), "PMWSLog");
        if (!dir.exists()) {
            dir.mkdir();
        }

        try {

            SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日  HH:mm:ss ");
            Date curDate = new Date(System.currentTimeMillis());//获取当前时间
            String str = formatter.format(curDate);

            FileWriter writer = new FileWriter(dir + "/log.txt", true);
            writer.write(first + str + ";" + "\r\n");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();

        }
    }


}
