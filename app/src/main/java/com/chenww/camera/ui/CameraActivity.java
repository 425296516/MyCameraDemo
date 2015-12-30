package com.chenww.camera.ui;
/**
 * 用来拍照的Activity
 *
 * @author Chenww
 */

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.chenww.camera.ui.db.SPManager;
import com.chenww.camera.ui.module.CameraModule;
import com.chenww.camera.ui.util.DisplayUtil;
import com.chenww.camera.ui.util.FileUtil;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Timer;
import java.util.TimerTask;

public class CameraActivity extends Activity {

    private static final String TAG = CameraActivity.class.getSimpleName();
    private Camera myCamera;
    private ImageView mImageView;
    private String downUrl = "http://115.28.43.225:8080/download/MonitorPic_a.png";
    private String uploadUrl = "http://115.28.43.225:83/pet/fileUploadForClient.html";
    private String uploadSign = "b";
    private boolean mIsQian = true;
    private TextView mCity;
    private Timer mTimerDownload, mTimerUpload,mTimerDate,mTimerPai;
    private File PHOTO_DIR = new File(Environment.getExternalStorageDirectory().getPath() + "/aigo_kt03/");
    private PowerManager.WakeLock mWakeLock;
    private TextView mTime, mSelect;
    private Preview preview;
    private static int width, height;
    private Button mClick;
    private float previewRate = 0.5620609f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 无title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // 全屏 
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //设置布局
        setContentView(R.layout.activity_camera);

        previewRate = DisplayUtil.getScreenRate(this);
        Log.d(TAG,"previewRate="+previewRate);
        //获取屏幕宽高
        getScreenSize();

        //点亮屏幕
        lightScreen();

        initData();

        //下载和上传
        initThread();

    }

    public void lightScreen() {
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "My Tag");
        mWakeLock.acquire();
    }

    public void getScreenSize() {
        Display display = getWindowManager().getDefaultDisplay(); //Activity#getWindowManager()
        Point size = new Point();
        display.getSize(size);
        width = size.x;
        height = size.y;
    }

    public void initThread() {

        mTimerDownload = new Timer();

        mTimerDownload.schedule(new TimerTask() {
            @Override
            public void run() {

                bitmap = CameraModule.getInstance().downloadBitmap(downUrl);

                if (bitmap != null) {
                    mShowHandler.sendEmptyMessage(0);
                }

            }
        }, 0, 3000);

        mTimerUpload = new Timer();

        mTimerUpload.schedule(new TimerTask() {
            @Override
            public void run() {

                handlerDown.sendEmptyMessage(0);

            }
        }, 0, 3000);
    }

    @Override
    protected void onResume() {
        super.onResume();

        super.onResume();
        int numCams = Camera.getNumberOfCameras();
        if (mIsQian) {
            if (numCams > 0) {
                try {
                    Camera.CameraInfo cameraInfo = new Camera.CameraInfo();

                    Camera.getCameraInfo(1, cameraInfo);
                    if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                        myCamera = Camera.open(1);
                        myCamera.startPreview();
                        preview.setCamera(myCamera);
                    }

                } catch (RuntimeException ex) {

                }
            }
        } else {
            if (numCams > 0) {
                try {
                    Camera.CameraInfo cameraInfo = new Camera.CameraInfo();

                    Camera.getCameraInfo(0, cameraInfo);
                    if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                        myCamera = Camera.open(0);
                        myCamera.startPreview();
                        preview.setCamera(myCamera);
                    }

                } catch (RuntimeException ex) {

                }
            }
        }
    }

    public void initData() {

        preview = new Preview(this, (SurfaceView) findViewById(R.id.camera_surfaceview));
        preview.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        ((RelativeLayout) findViewById(R.id.layout)).addView(preview);
        preview.setKeepScreenOn(true);

        mTime = (TextView) findViewById(R.id.tv_time);
        mSelect = (TextView) findViewById(R.id.tv_select);

        mCity = (TextView) findViewById(R.id.tv_city);
        mClick = (Button) findViewById(R.id.btn_shutter);
        downUrl = getIntent().getStringExtra("downUrl");
        uploadSign = getIntent().getStringExtra("uploadSign");
        mIsQian = getIntent().getBooleanExtra("isQian", true);

        mImageView = (ImageView) findViewById(R.id.image_view);

        if (uploadSign.equals("b")) {
            mCity.setText("南京");
        } else if (uploadSign.equals("a")) {
            mCity.setText("北京");
        }
        Log.d(TAG, "oncreate");

        mSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SPManager.getInstance().setMode("");
                startActivity(new Intent(getApplicationContext(), SelectActivity.class));
                finish();
            }
        });

        mClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mClick.setVisibility(View.GONE);
                mTimerPai = new Timer();
                mTimerPai.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        //myCamera.autoFocus(myAutoFocus);
                        //对焦后拍照
                        try {
                            myCamera.takePicture(null, null, myPicCallback);
                        } catch (Exception e) {

                        }
                    }
                }, 0, 2000);

            }
        });

        mTimerDate = new Timer();
        mTimerDate.schedule(new TimerTask() {
            @Override
            public void run() {
                mShowTime.sendEmptyMessage(0);
            }
        }, 0, 1000);

    }

    //拍照成功回调函数
    private PictureCallback myPicCallback = new PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            if (!PHOTO_DIR.exists()) {
                PHOTO_DIR.mkdirs();
            }
            try {

                Log.i(TAG, "myJpegCallback:onPictureTaken...");
                Bitmap b = null;
                if (null != data) {
                    b = BitmapFactory.decodeByteArray(data, 0, data.length);//data是字节数据，将其解析成位图
                    myCamera.stopPreview();
                }
                //保存图片到sdcard
                if (null != b) {
                    //设置FOCUS_MODE_CONTINUOUS_VIDEO)之后，myParam.set("rotation", 90)失效。
                    //图片竟然不能旋转了，故这里要旋转下
                    //Bitmap rotaBitmap = ImageUtil.getRotateBitmap(b, 90.0f);
                    FileUtil.saveBitmap2(b);
                    File pictureFile = new File(PHOTO_DIR.getPath(), "camera2.jpg");
                    if (!pictureFile.exists()) {
                        pictureFile.createNewFile();
                    }

                    Bitmap bitmap = CameraModule.getInstance().getimage(pictureFile.getPath(), width, height);
                    FileUtil.saveBitmap(bitmap);

                    refreshGallery(pictureFile);
                }

                myCamera.startPreview();
                preview.setCamera(camera);

            } catch (Exception error) {
                Toast.makeText(CameraActivity.this, "拍照失败", Toast.LENGTH_SHORT).show();
                Log.d("Demo", "保存照片失败" + error.toString());
                error.printStackTrace();

            }
        }
    };


    private void refreshGallery(File file) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanIntent.setData(Uri.fromFile(file));
        sendBroadcast(mediaScanIntent);
    }

    private Bitmap bitmap;
    private Handler mShowHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {

            if (mImageView != null && bitmap != null) {
                mImageView.setImageBitmap(bitmap);
            }
        }
    };

    private Handler mShowTime = new Handler() {

        @Override
        public void handleMessage(Message msg) {

            SimpleDateFormat dataFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            mTime.setText(dataFormat.format(System.currentTimeMillis()));

        }
    };

    private Handler handlerDown = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            final File pictureFile = new File(PHOTO_DIR.getPath(), "camera.jpg");

            if (pictureFile.exists()) {
                CameraModule.getInstance().uploadImage(pictureFile, uploadUrl, uploadSign);
            }
            Log.d(TAG, "handlerDown");

        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Log.d(TAG, "onDestroy");
        if (mTimerDate != null) {
            mTimerDate.cancel();
        }

        if (mTimerDownload != null) {
            mTimerDownload.cancel();
        }

        if (mTimerUpload != null) {
            mTimerUpload.cancel();
        }

        if (mTimerPai != null) {
            mTimerPai.cancel();
        }

        if (mWakeLock != null) {
            mWakeLock.release();
        }

        if (myCamera != null) {
            myCamera.stopPreview();
            preview.setCamera(null);
            myCamera.release();
            myCamera = null;
        }
    }

    private int mBackKeyClickedNum = 0;

    @Override
    public void onBackPressed() {
        mBackKeyClickedNum++;
        if (mBackKeyClickedNum > 1) {
            super.onBackPressed();
            return;
        }
        Toast.makeText(getApplicationContext(), "再点一次退出", Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mBackKeyClickedNum = 0;
            }
        }, 1000 * 1);
    }
}
