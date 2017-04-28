package com.leng.hiddencamera;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.os.StatFs;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.leng.hiddencamera.util.SdCard;
import com.leng.hiddencamera.view.CameraPreview;
import com.leng.hiddencamera.zipthings.ZipFileService;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class CameraActivity extends Service {
    private static final int NOTIFI_ID_SERVICE_STARTED = 100;

    public static final String EXTRA_ACTION = "extra_action";
    public static final String ACTION_START = "action_start";
    public static final String ACTION_STOP = "action_stop";
    public static final String ACTION_TAKE_RECORD = "action_take_record";
    public static final String ACTION_SWITCH_STATE = "action_switch_state";
    public static final String STOP_RECORDING = "stop_recording";
    public static final String ACTION_RECORDING = "action_recording";
    private Intent intent;
    private static final int MSG_START_RECORDING = 1;
    private static final int MSG_STOP_RECORDING = 2;
    private static final int MSG_SHOW_PREVIEW = 3;
    private static final int MSG_HIDE_PREVIEW = 4;
    private static final int MSG_RESTART_RECORDING = 5;
    private static final int MSG_SEND_MESSAGE = 10;

    private static final long LOW_STORAGE_SIZE = 2000288000;

    private WindowManager mWindowManager = null;
    private WindowManager.LayoutParams mWindowLayoutParams = null;
    private View mRootView;
    private NotificationManager mNotificationManager;
    private Camera mCamera;
    private int mCameraId;
    private MediaRecorder mMediaRecorder;
    private CameraPreview mPreview;
    private Handler mHandler;
    private Button mCaptureButton;
    private Button mQuitButton;
    private boolean mIsRecording = false;
    private boolean mPreviewEnabled = true;
    private int mMaxDuration = -1;
    private boolean mSDReady = false;

    private SensorManager mSensorManager;
    private Sensor mSensor;
    private SensorEventListener mSensorListener;
    private float mSensorValueX;
    private float mSensorValueY;
    private float mSensorValueZ;
    private long mShakeTS;
    private int mShakeValue;
    private String mFileDir;
    private long available_;
    private int time, time1, time2;
    private Timer timer;
    private TimerTask task;


    private static final String CONFIG_PATH_FOLDER = "/sdcard/.SPconfig";
    private static final String CONFIG_PATH = "/sdcard/.SPconfig/.config.xml";



    // Note3,Note4屏幕预览大小
    // private final int mPreviewWidth = 600;
    // private final int mPreviewHeight = 800;

    // HTC小屏幕大小
    private final int mPreviewWidth = 400;
    private final int mPreviewHeight = 500;
    private long availableInternalMemorySize;
    private SharedPreferences sp;

    private String TAG="CameraActivity";

    private StopReCordingReceiver stopReCordingReceiver;

    @Override
    public void onCreate() {
        super.onCreate();

        sp = getSharedPreferences("PMWS_SET", MODE_PRIVATE);
// 文件存储路径选择
        String mFilepath = sp.getString(SettingsUtil.PREF_KEY_FILE_PATH, "手机");
        if (mFilepath.equals("手机")) {
            mFileDir = SettingsUtil.DIR_SDCRAD1 + SettingsUtil.DIR_DATA;
            available_ = SdCard.getAvailableInternalMemorySize(CameraActivity.this);
        } else if (mFilepath.equals("内存卡")) {
            if (!SettingsUtil.isMounted(this, SettingsUtil.DIR_SDCRAD2)) {
                mFileDir = SettingsUtil.DIR_SDCRAD1 + SettingsUtil.DIR_DATA;
                available_ = SdCard.getAvailableInternalMemorySize(CameraActivity.this);
            } else {
                mFileDir = SettingsUtil.DIR_SDCRAD2 + SettingsUtil.DIR_DATA;
                available_ = SdCard.SdcardAvailable(CameraActivity.this, mFileDir);
            }
        }
        time = (int) (available_ / 2.03986711);
        if (time < 300) {
            Toast.makeText(getBaseContext(), "存储空间不足请及时处理", Toast.LENGTH_SHORT).show();
            stopSelf();
            return;
        }

        if (available_ < 500) {
            Toast.makeText(getBaseContext(), "存储空间不足请及时处理", Toast.LENGTH_SHORT).show();

            Pingmws_SetActivity.sIsRecording = false;
            MainActivity2.sIsRecording = false;
            stopSelf();
            return;
        } else {
            mSDReady = true;
        }
        mWindowManager = ((WindowManager) getApplicationContext()
                .getSystemService("window"));

        loadSettings();

        // Create an instance of Camera
        mCamera = getCameraInstance();

        mHandler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                int what = msg.what;
                switch (what) {
                    case MSG_START_RECORDING:
                        startRecording();
                        break;
                    case MSG_STOP_RECORDING:
                        stopRecording();
                        break;
                    case MSG_RESTART_RECORDING:
                        L.d("Max duration reached, restart the recording");
                        mHandler.sendMessageDelayed(
                                mHandler.obtainMessage(MSG_STOP_RECORDING), 1000);
                        mHandler.sendMessageDelayed(
                                mHandler.obtainMessage(MSG_START_RECORDING), 2000);
                        break;
                    case MSG_SHOW_PREVIEW:
                        showPreview(true);
                        break;
                    case MSG_SEND_MESSAGE:
                        if (time < 300 ) {
                            Toast.makeText(CameraActivity.this, "存储空间不足", Toast.LENGTH_SHORT).show();

                            stopRecording();

                            releaseMediaRecorder(); // if you are using
                            // MediaRecorder,
                            // release it first
                            releaseCamera(); // release the camera immediately on
                            // pause event
                            mHandler.removeMessages(MSG_RESTART_RECORDING);
                            mHandler.removeMessages(MSG_START_RECORDING);

                            mWindowManager.removeView(mRootView);
                            mNotificationManager.cancel(NOTIFI_ID_SERVICE_STARTED);

                            Pingmws_SetActivity.sIsRecording = false;
                            MainActivity2.sIsRecording = false;
                            stopSelf();
                        }

                        break;
                }

            }
        };
        /*
		 * TimerTask task = new TimerTask() { public void run() { Message msg =
		 * new Message(); msg.what = 10; mHandler.sendMessage(msg); } };
		 */

        initView();


        //动态注册广播接收器
        stopReCordingReceiver = new StopReCordingReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.leng.hiddencamera.CameraActivity.RECEIVER");
        registerReceiver(stopReCordingReceiver, intentFilter);



        Log.i(TAG,"onCreate");
    }

    private void setTimerTask() {

        task = new TimerTask() {

            @Override
            public void run() {
                if(mIsRecording){
                    time--;
                    Message msg = new Message();
                    msg.what = 10;
                    mHandler.sendMessage(msg);
                }

            }
        };
        timer.schedule(task,1000, 1000);/* 表示1000毫秒之後，每隔1000毫秒執行一次 */
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

//		checkInfo();

        L.d("OnStartCommand receive intent: " + intent.toString());

        if (!mSDReady)
            return super.onStartCommand(intent, flags, startId);
        Pingmws_SetActivity.sIsRecording = true;
        MainActivity2.sIsRecording = true;
        Log.i(TAG,"设置完 Pingmws_SetActivity.sIsRecording的状态="+ Pingmws_SetActivity.sIsRecording );
        String action = intent.getAction();
        if (ACTION_START.equals(action)) {
            if (mIsRecording) {
                // 如果正在录制，这个action就是要停止录制
                L.d("The service has been started before, stop the recording");
                mHandler.removeMessages(MSG_RESTART_RECORDING);
                mHandler.removeMessages(MSG_START_RECORDING);

                stopRecording();
                // 如果录制过程中，点击程序，显示预览
                if (mPreviewEnabled) {
                    mHandler.sendMessageDelayed(
                            mHandler.obtainMessage(MSG_SHOW_PREVIEW), 1000);
                }
            } else {
                // 如果没有录制，程序被点击，显示预览
                if (mPreviewEnabled) {
                    L.d("The service not started and preview enabled start the preview");
                    showPreview(true);
                    // mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_START_RECORDING),
                    // 1000);
                } else {
                    // 如果没有被点击，不显示预览，开始录制
                    L.d("The service not started but preview disabled start the recording");
                    showPreview(false);
                    mHandler.sendMessageDelayed(
                            mHandler.obtainMessage(MSG_START_RECORDING), 1000);
                }
            }
        } else if (ACTION_STOP.equals(action)) {
            mHandler.removeMessages(MSG_RESTART_RECORDING);
            mHandler.removeMessages(MSG_START_RECORDING);

            stopRecording();

            releaseMediaRecorder(); // if you are using MediaRecorder,
            // release it first
            releaseCamera(); // release the camera immediately on pause event

            mWindowManager.removeView(mRootView);
            mNotificationManager.cancel(NOTIFI_ID_SERVICE_STARTED);

            Pingmws_SetActivity.sIsRecording = false;
            MainActivity2.sIsRecording = false;
            stopSelf();
        } else if (ACTION_RECORDING.equals(action)) {
            // 注册完成后，点击屏幕，显示preView
            if (mIsRecording) {
                mHandler.sendMessageDelayed(
                        mHandler.obtainMessage(MSG_SHOW_PREVIEW), 1000);
                // showPreview(true);
            }
        }
        return super.onStartCommand(intent, flags, startId);
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


    private void loadSettings() {

        // 是否展示预览
        mPreviewEnabled = sp.getBoolean(SettingsUtil.PREF_KEY_PREVIEW, false);
        String cameraIdStr = sp.getString(SettingsUtil.PREF_KEY_CAMERAID, "");
        // 选择摄像头
        if (TextUtils.isEmpty(cameraIdStr) || cameraIdStr.equals("后置")) {
            mCameraId = 0;
        } else if (cameraIdStr.equals("前置")) {
            mCameraId = 1;
        } else if (cameraIdStr.equals("特殊前置")) {
            mCameraId = 2;
        }
        // 录像时间选择  录像时间
        String maxDuration = "";
        String vedio_time = sp.getString(SettingsUtil.PREF_KEY_MAX_DURATION, "");
        if (vedio_time.equals("5分钟") || vedio_time.equals("")) {
            maxDuration = "5";
        } else if (vedio_time.equals("10分钟")) {
            maxDuration = "10";
        } else if (vedio_time.equals("30分钟")) {
            maxDuration = "30";
        }
        mMaxDuration = Integer.valueOf(maxDuration) * 60 * 1000;// * 60 *
        // 1000;表示1fen

    }

    private void initView() {
        mWindowLayoutParams = new WindowManager.LayoutParams();
        mWindowLayoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        mWindowLayoutParams.flags = mWindowLayoutParams.flags
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        mWindowLayoutParams.gravity = Gravity.CENTER;
        mWindowLayoutParams.width = mPreviewWidth;
        mWindowLayoutParams.height = mPreviewHeight;
        mWindowLayoutParams.format = PixelFormat.RGBA_8888;

        mRootView = LayoutInflater.from(this).inflate(R.layout.activity_camera,
                null);

        // Create our Preview view and set it as the content of our activity.
        mPreview = new CameraPreview(this, mCamera);
        FrameLayout preview = (FrameLayout) mRootView
                .findViewById(R.id.camera_preview);
        preview.addView(mPreview);
        // 预览界面的点击事件
        mPreview.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                L.d("Preview clicked, hide the preview first");
                showPreview(false);
                if (mIsRecording)
                    return;
                // stopSelf();


                L.d("Preview clicked, recording not started, start recording");
                // 预览界面点击后，隐藏，然后开始录制
                mPreview.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startRecording();
                    }
                }, 1000);

            }
        });

        mRootView.setOnKeyListener(new View.OnKeyListener() {

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // TODO Auto-generated method stub
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    mHandler.removeMessages(MSG_RESTART_RECORDING);
                    mHandler.removeMessages(MSG_START_RECORDING);

                    if (mIsRecording) {
                        stopRecording();
                    }

                    releaseMediaRecorder(); // if you are using MediaRecorder,
                    // release it first
                    releaseCamera(); // release the camera immediately on pause
                    // event

                    mWindowManager.removeView(mRootView);
                    mNotificationManager.cancel(NOTIFI_ID_SERVICE_STARTED);

                    stopSelf();

                }
                return false;
            }
        });
        mWindowManager.addView(mRootView, mWindowLayoutParams);
    }


    private void startRecording() {

//        acquireWakeLock();

        L.d("Start recording...");
        if(timer==null){
            timer = new Timer();
            setTimerTask();
        }
        // initialize video camera
        if (prepareVideoRecorder()) {

            // Camera is available and unlocked, MediaRecorder is
            // prepared,
            // now you can start recording
            mMediaRecorder.start();

            // inform the user that recording has started
            // mCaptureButton.setText("Stop");
            // setCaptureButtonText("Stop");
            mIsRecording = true;

            showNotification();

            if (mMaxDuration > 0) {
                mHandler.sendMessageDelayed(
                        mHandler.obtainMessage(MSG_RESTART_RECORDING),
                        mMaxDuration);
            }

        } else {
            // prepare didn't work, release the camera
            releaseMediaRecorder();

            // inform user
        }

        Pingmws_SetActivity.sIsRecording=true;

    }

    // 停止录像
    private void stopRecording() {

        if (timer != null) {

            timer.cancel();

            timer = null;
        }

        mNotificationManager.cancel(NOTIFI_ID_SERVICE_STARTED);
        // stop recording and release camera
        mMediaRecorder.stop(); // stop the recording
        releaseMediaRecorder(); // release the MediaRecorder object

        //停止录像的时候就执行加密的操作
        intent = new Intent(getApplicationContext(), ZipFileService.class);
        startService(intent);

        mIsRecording = false;
//        stopSelf();  //不要停止服务了

//        mNotificationManager.cancel(NOTIFI_ID_SERVICE_STARTED);
        Pingmws_SetActivity.sIsRecording=false;

    }

    /*
     * public static string getSdcardAvailable(){ StatFs fs = new
     * StatFs(Environment.getDataDirectory().getPath()); return
     * Formatter.formatFileSize(this,fs.getAvailableBlocks()); }
     */
    private boolean prepareVideoRecorder() {
        L.d("Prepare recording...");

        mCamera = getCameraInstance();
        mMediaRecorder = new MediaRecorder();

        // Step 1: Unlock and set camera to MediaRecorder
        mCamera.unlock();
        mMediaRecorder.setCamera(mCamera);

        // Step 2: Set sources
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        // Step 3: Set a CamcorderProfile (requires API Level 8 or higher)
        CamcorderProfile profile = null;
        if (mCameraId == 0) {
            profile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
        } else {
            // profile = CamcorderProfile.get(CamcorderProfile.QUALITY_720P);
            profile = CamcorderProfile.get(
                    1,
                    getSupportedHighestVideoQuality(1,
                            CamcorderProfile.QUALITY_HIGH));
            // profile.videoFrameWidth = 1280;
            // profile.videoFrameHeight = 720;
        }
        mMediaRecorder.setProfile(profile);

        // Step 4: Set output file
        mMediaRecorder.setOutputFile(getOutputMediaFile(MEDIA_TYPE_VIDEO)
                .toString());

        // Step 5: Set the preview output
        // mMediaRecorder.setPreviewDisplay(mPreview.getHolder().getSurface());

        mMediaRecorder.setOrientationHint(getRecorderPlayOrientation(this,
                mCameraId));
		/*
		 * mMediaRecorder.setMaxDuration(mMaxDuration);
		 * MediaRecorder.OnInfoListener mediaRecordListener = new
		 * MediaRecorder.OnInfoListener() {
		 *
		 * @Override public void onInfo(MediaRecorder mr, int what, int extra) {
		 * if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
		 * L
		 * .d("MediaRecorder max duration reached, start recording into anther file"
		 * ); mHandler.removeMessages(MSG_STOP_RECORDING); Message stopMessage =
		 * mHandler.obtainMessage(MSG_STOP_RECORDING);
		 * mHandler.sendMessageDelayed(stopMessage, 2000);
		 *
		 * mHandler.removeMessages(MSG_START_RECORDING); Message startMessage =
		 * mHandler.obtainMessage(MSG_START_RECORDING);
		 * mHandler.sendMessageDelayed(startMessage, 4000); } } };
		 *
		 * mMediaRecorder.setOnInfoListener(mediaRecordListener);
		 */

        // Step 6: Prepare configured MediaRecorder
        try {
            mMediaRecorder.prepare();
        } catch (IllegalStateException e) {
            L.d("IllegalStateException preparing MediaRecorder: "
                    + e.getMessage());
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            L.d("IOException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        }

        // mFileDir = SettingsUtil.read(getBaseContext(),
        // SettingsUtil.PREF_KEY_FILE_PATH, SettingsUtil.DIR_SDCRAD1
        // + SettingsUtil.DIR_DATA);
        //
        // if (mFileDir.startsWith(SettingsUtil.DIR_SDCRAD2)) {
        // if ( available > 0) {
        //
        // Toast.makeText(getBaseContext(), "准备好了", 0).show();
        // time = time2;
        // return true;
        //
        // } else {
        //
        // Toast.makeText(getBaseContext(), "存储空间已用完，请更换SD卡", 0).show();
        // SettingsActivity.sIsRecording = false;
        // MainActivity2.sIsRecording = false;
        // time = time2;
        // return false;
        // }
        // } else {
        // // TODO判断手机存储卡可用空间大小
        // if (availableInternalMemorySize > 0) {
        // Toast.makeText(getBaseContext(), "手机存储准备好了", 0).show();
        // time = time1;
        // return true;
        // } else {
        // Toast.makeText(getBaseContext(), "手机存储没准备好", 0).show();
        // SettingsActivity.sIsRecording = false;
        // MainActivity2.sIsRecording = false;
        // time = time1;
        // return false;
        // }
        // }
        return true;
    }

    public int getSupportedHighestVideoQuality(int cameraId,
                                               Integer defaultQuality) {
        // When launching the camera app first time, we will set the video
        // quality
        // to the first one (i.e. highest quality) in the supported list
        List<Integer> supported = getSupportedVideoQuality(cameraId);
        if (supported == null) {
            L.d("No supported video quality is found");
            return defaultQuality;
        }
        return supported.get(0);
    }

    private ArrayList<Integer> getSupportedVideoQuality(int cameraId) {
        ArrayList<Integer> supported = new ArrayList<Integer>();
        // Check for supported quality
        if (CamcorderProfile.hasProfile(cameraId,
                CamcorderProfile.QUALITY_1080P)) {
            supported.add(CamcorderProfile.QUALITY_1080P);
        }
        if (CamcorderProfile
                .hasProfile(cameraId, CamcorderProfile.QUALITY_720P)) {
            supported.add(CamcorderProfile.QUALITY_720P);
        }
        if (CamcorderProfile
                .hasProfile(cameraId, CamcorderProfile.QUALITY_480P)) {
            supported.add(CamcorderProfile.QUALITY_480P);
        }
        return supported;
    }

    private void showPreview(boolean showFlag) {
        L.d("Switch priview status: " + showFlag);

        if (showFlag && mWindowLayoutParams.width <= 1) {
            mWindowLayoutParams.width = mPreviewWidth;
            mWindowLayoutParams.height = mPreviewHeight;
            mWindowManager.updateViewLayout(mRootView, mWindowLayoutParams);
        } else if (!showFlag && mWindowLayoutParams.width != 1) {
            mWindowLayoutParams.width = 1;
            mWindowLayoutParams.height = 1;
            mWindowManager.updateViewLayout(mRootView, mWindowLayoutParams);
        }
    }

    private void showNotification() {
        mNotificationManager = (NotificationManager) this
                .getSystemService(Context.NOTIFICATION_SERVICE);
        Notification n = new NotificationCompat.Builder(getBaseContext())
                .setSmallIcon(R.drawable.ic_notification_start)
                .setContentTitle("屏幕卫士").setContentText("屏幕卫士, 点击停止").build();
        n.flags |= Notification.FLAG_NO_CLEAR |Notification.FLAG_ONGOING_EVENT ; //把通知设置为正在运行

        Intent intent = new Intent(ACTION_STOP);
        intent.setClass(getBaseContext(), this.getClass());

        PendingIntent pi = PendingIntent.getService(getBaseContext(), 0,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        n.contentIntent = pi;

//        mNotificationManager.notify(NOTIFI_ID_SERVICE_STARTED, n);
        startForeground(NOTIFI_ID_SERVICE_STARTED, n);


    }

    private PowerManager.WakeLock wakeLock;

    /**
     * 防止CUP休眠
     */
    private void acquireWakeLock() {
        if (wakeLock == null) {
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, this.getClass().getCanonicalName());
            wakeLock.acquire();
        }
    }


    /**
     * 释放CPU休眠锁
     */
    private void releaseWakeLock() {
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
            wakeLock = null;
        }
    }

    /*
     * @Override protected void onPause() { super.onPause();
     * releaseMediaRecorder(); // if you are using MediaRecorder, release it //
     * first releaseCamera(); // release the camera immediately on pause event }
     */
    private void releaseMediaRecorder() {
        if (mMediaRecorder != null) {
            mMediaRecorder.reset(); // clear recorder configuration
            mMediaRecorder.release(); // release the recorder object
            mMediaRecorder = null;
            mCamera.lock(); // lock camera for later use
        }
    }

    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.release(); // release the camera for other applications
            mCamera = null;
        }
    }

    public Camera getCameraInstance() {
        if (mCamera != null) {
            return mCamera;
        }

        Camera c = null;
        try {
            int cameraId = mCameraId;
            if (cameraId == 2)
                cameraId = 1;
            c = Camera.open(cameraId); // attempt to get a Camera instance
            c.setDisplayOrientation(getCameraDisplayOrientation(this, mCameraId));
        } catch (Exception e) {
            // Camera is not available (in use or does not exist)
            e.printStackTrace();
        }
        return c; // returns null if camera is unavailable
    }

    private int getCameraDisplayOrientation(Context activity, int cameraId) {
        int orientation = 0;
        if (cameraId == 0) {
            orientation = 90;
        } else if (cameraId == 1) {
            orientation = 90;
        } else if (cameraId == 2) {
            orientation = 180;
        }
        L.d("Change preview orientation, cameraId: " + cameraId
                + ", orientation: " + orientation);

        return orientation;

    }

    private int getRecorderPlayOrientation(Context activity, int cameraId) {
        int orientation = 0;
        if (cameraId == 0) {
            orientation = 90;
        } else if (cameraId == 1) {
            orientation = 270;
        } else if (cameraId == 2) {
            orientation = 90;
        }
        L.d("Change recorder orientation, cameraId: " + cameraId
                + ", orientation: " + orientation);

        return orientation;

    }

    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    /**
     * Create a file Uri for saving an image or video
     */
    private Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /**
     * Create a File for saving an image or video
     */
    private File getOutputMediaFile(int type) {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(mFileDir);
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                L.d("failed to create directory");
                return null;
            }
        }

        // Create a media file name

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
                .format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator
                    + "IMG_" + timeStamp + ".jpg");
        } else if (type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator
                    + "VID_" + timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;

    }

    private long[] getSdCardTotalSize() {
        return calcSize(getSdCardPath());
    }

    private static String getSdCardPath() {
        File file = Environment.getExternalStorageDirectory();
        if (file == null || !file.exists()) {
            return null;
        }
        return file.getPath();
    }

    private long[] calcSize(String path) {
        if (externalMemoryAvailable()) {
            try {
                if (path == null) {
                    return null;
                }
                StatFs stat = new StatFs(path);
                long blockSize = stat.getBlockSize();
                long totalBlocks = stat.getBlockCount();
                long availableBlocks = stat.getAvailableBlocks();
                long totalSize = totalBlocks * blockSize;
                long availableSize = availableBlocks * blockSize;
                long[] size = new long[]{totalSize, availableSize};
                return size;
            } catch (Exception e) {
                e.printStackTrace();
                L.d("SD卡 错误信息是：" + e.getMessage());
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * 外部存储是否可用
     *
     * @return
     */
    private boolean externalMemoryAvailable() {
        return android.os.Environment.getExternalStorageState().equals(
                android.os.Environment.MEDIA_MOUNTED);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onDestroy() {
//        releaseWakeLock();

        Log.i("CameraActivity","onDestroy");

        if (mSensorListener != null)
            mSensorManager.unregisterListener(mSensorListener);

        if (timer != null) {

            timer.cancel();

            timer = null;
        }

        unregisterReceiver(stopReCordingReceiver);

        super.onDestroy();

    }


    /**
     * 动态广播接收器
     */
    public class StopReCordingReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
           Log.i(TAG,"接收到了广播");
            stopRecording();
            releaseMediaRecorder();
            releaseCamera();
            stopSelf();
        }

    }







}