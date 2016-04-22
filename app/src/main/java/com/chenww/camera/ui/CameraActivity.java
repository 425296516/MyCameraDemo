package com.chenww.camera.ui;
/**
 * 用来拍照的Activity
 *
 * @author Chenww
 */

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.chenww.camera.ui.db.SPManager;
import com.chenww.camera.ui.module.CameraModule;
import com.chenww.camera.ui.util.FileUtil;
import com.jiuwei.upgrade_package_new.lib.UpgradeModule;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class CameraActivity extends Activity implements SurfaceHolder.Callback {

    private static final String TAG = CameraActivity.class.getSimpleName();
    private Camera myCamera;
    private ImageView mImageView;
    private String downUrl = "http://115.28.43.225:8080/download/MonitorPic_a.png";
    private String uploadUrl = "http://115.28.43.225:83/pet/fileUploadForClient.html";
    private String uploadSign = "b";
    private boolean mIsQian = true;
    private TextView mCity;
    private Timer mTimerDownload, mTimerUpload, mTimerDate, mTimerPai;
    private File PHOTO_DIR = new File(Environment.getExternalStorageDirectory().getPath() + "/aigo_kt03/");
    private PowerManager.WakeLock mWakeLock;
    private TextView mTime, mSelect;

    private Button mClick;
    private int mHeight;
    private int mWidth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 无title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // 全屏 
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //设置布局
        setContentView(R.layout.activity_camera);

        downUrl = getIntent().getStringExtra("downUrl");
        uploadSign = getIntent().getStringExtra("uploadSign");
        mIsQian = getIntent().getBooleanExtra("isQian", true);

        //获取屏幕宽高
        getScreenSize();

        //点亮屏幕
        lightScreen();

        initData();

        //下载和上传
        initThread();

        UpgradeModule.init(this);
        UpgradeHelper.checkUpgrade(
                this,
                true,
                com.jiuwei.upgrade_package_new.lib.Constant.DIALOG_STYLE_ELDERLY_ASSISTANT);

    }

    public void lightScreen() {
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "My Tag");
        mWakeLock.acquire();
    }

    public void getScreenSize() {

        WindowManager wManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        Display display = wManager.getDefaultDisplay();
        mHeight = display.getHeight();
        mWidth = display.getWidth();

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
        }, 0, 4000);

        mTimerUpload = new Timer();

        mTimerUpload.schedule(new TimerTask() {
            @Override
            public void run() {

                handlerDown.sendEmptyMessage(0);

            }
        }, 0, 4000);
    }

    private SurfaceView mPreview;
    private SurfaceHolder mHolder;

    public void initData() {

        mPreview = (SurfaceView) findViewById(R.id.camera_surfaceview);
        mHolder = mPreview.getHolder();
        mHolder.addCallback(this);

        mTime = (TextView) findViewById(R.id.tv_time);
        mSelect = (TextView) findViewById(R.id.tv_select);

        mCity = (TextView) findViewById(R.id.tv_city);
        mClick = (Button) findViewById(R.id.btn_shutter);

        mImageView = (ImageView) findViewById(R.id.image_view);

        if (uploadSign.equals("b")) {
            mCity.setText("南京");
        } else if (uploadSign.equals("a")) {
            mCity.setText("北京");
        }
        Log.d(TAG, "oncreate");

        if (myCamera == null) {
            myCamera = getCamera();
            if (myCamera != null) {
                setStartPreview(myCamera, mHolder);
            }
        } else {
            setStartPreview(myCamera, mHolder);
        }

        mPreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myCamera.autoFocus(null);
            }
        });

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
                myCamera.autoFocus(new Camera.AutoFocusCallback() {
                    @Override
                    public void onAutoFocus(boolean success, Camera camera) {
                        Log.d(TAG, "success=" + success);
                        //if (success) {
                        mTimerPai = new Timer();
                        mTimerPai.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                Log.d(TAG, "mTimerPai");
                                //对焦后拍照
                                try {
                                    myCamera.takePicture(null, null, myPicCallback);
                                } catch (Exception e) {

                                    mDataHander.sendEmptyMessage(0);

                                    Log.d(TAG, "takePicture" + e.toString());

                                }
                            }
                        }, 0, 4000);
                    }
                    //}
                });
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
                Log.i(TAG, "myJpegCallback:onPictureTaken..." + data.length / 1024);
                Bitmap b = null;
                if (null != data) {
                    b = BitmapFactory.decodeByteArray(data, 0, data.length);//data是字节数据，将其解析成位图

                }
                //保存图片到sdcard
                if (null != b) {
                    Bitmap bitmap = drawTextToBitmap(getApplicationContext(), b, getStringDate());
                    FileUtil.saveBitmap(bitmap);
                    File pictureFile = new File(PHOTO_DIR.getPath(), "camera.jpg");
                    if (!pictureFile.exists()) {
                        pictureFile.createNewFile();
                    }

                    if (data.length / 1024 > 100) {
                        bitmap = CameraModule.getInstance().getimage(pictureFile.getPath(), mWidth, mHeight);

                        FileUtil.saveBitmap(bitmap);
                    }
                }
                myCamera.stopPreview();
                setStartPreview(myCamera, mHolder);

            } catch (Exception error) {

                Toast.makeText(CameraActivity.this, "拍照失败", Toast.LENGTH_SHORT).show();
                Log.d("Demo", "保存照片失败" + error.toString());
                error.printStackTrace();

            }
        }
    };


    public static String getStringDate() {
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("MM-dd HH:mm:ss");
        String dateString = formatter.format(currentTime);
        return dateString;
    }


    public static Bitmap scaleWithWH(Bitmap src, double w, double h) {
        if (w == 0 || h == 0 || src == null) {
            return src;
        } else {
            // 记录src的宽高
            int width = src.getWidth();
            int height = src.getHeight();
            // 创建一个matrix容器
            Matrix matrix = new Matrix();
            // 计算缩放比例
            float scaleWidth = (float) (w / width);
            float scaleHeight = (float) (h / height);
            // 开始缩放
            matrix.postScale(scaleWidth, scaleHeight);
            // 创建缩放后的图片
            return Bitmap.createBitmap(src, 0, 0, width, height, matrix, true);
        }
    }

    public Bitmap drawTextToBitmap(Context gContext,
                                   Bitmap bitmap2,
                                   String gText) {
        Resources resources = gContext.getResources();
        float scale = resources.getDisplayMetrics().density;
        Bitmap bitmap = bitmap2;

        //bitmap = scaleWithWH(bitmap, bitmap.getWidth() / scale, bitmap.getHeight() / scale);
        Log.d(TAG, "bitmap=" + " scale=" + scale + " : " + bitmap.getWidth() + " : " + bitmap.getHeight());
        android.graphics.Bitmap.Config bitmapConfig =
                bitmap.getConfig();

        // set default bitmap config if none
        if (bitmapConfig == null) {
            bitmapConfig = android.graphics.Bitmap.Config.ARGB_8888;
        }
        // resource bitmaps are imutable,
        // so we need to convert it to mutable one
        bitmap = bitmap.copy(bitmapConfig, true);
        Log.d(TAG, "bitmap=" + bitmap.getWidth() + " : " + bitmap.getHeight());
        Canvas canvas = new Canvas(bitmap);
        // new antialised Paint
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        // text color - #3D3D3D
        paint.setColor(Color.RED);
        paint.setTextSize((int) (14 * scale));
        paint.setDither(true); //获取跟清晰的图像采样
        paint.setFilterBitmap(true);//过滤一些
        Rect bounds = new Rect();
        paint.getTextBounds(gText, 0, gText.length(), bounds);
        int x = 30;
        int y = 30;
        canvas.drawText(gText, bitmap.getWidth() / 2, y * scale, paint);
        Log.d(TAG, "bitmap=" + bitmap.getWidth() + " : " + bitmap.getHeight());
        return bitmap;
    }


    private Bitmap bitmap;
    private Handler mShowHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {

            if (mImageView != null && bitmap != null) {
                // ImageUtil.getRotateBitmap(bitmap, 90)
                mImageView.setImageBitmap(bitmap);
            }
        }
    };


    private Handler mDataHander = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            Log.d(TAG, "handleMessage");

            initData();

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

    //private boolean isRelease;

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

        releaseCamera();
    }

    private int mBackKeyClickedNum = 0;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
       /* mBackKeyClickedNum++;
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
        }, 1000 * 1);*/
    }

    /**
     * 获取camera对象
     */
    Camera camera;

    private Camera getCamera() {

        if (camera == null) {
            if (mIsQian) {
                camera = Camera.open(1);
            } else {
                camera = Camera.open(0);
            }
        }

        return camera;
    }


    /**
     * 开始预览相机内容
     */
    private void setStartPreview(Camera camera, SurfaceHolder holder) {

        try {
            if (camera != null && holder != null) {
                camera.setPreviewDisplay(holder);
                //将系统预览角度进行调整
                //camera.setDisplayOrientation(90);
                camera.startPreview();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 释放相机资源
     */
    private void releaseCamera() {
        try{
            if (myCamera != null) {
                myCamera.setPreviewCallback(null);
                myCamera.stopPreview();
                myCamera.release();
                myCamera = null;
            }
        }catch (Exception e){
            Log.d(TAG,e.toString());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        setStartPreview(myCamera, mHolder);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        try{
            Camera.Parameters parameters = myCamera.getParameters();
            parameters.setPictureFormat(ImageFormat.JPEG);

            Log.d(TAG, parameters.getPictureFormat() + "");

            List<Camera.Size> supportedPreviewSizes = parameters.getSupportedPreviewSizes();
            List<Camera.Size> supportedPictureSizes = parameters.getSupportedPictureSizes();
            List<String> focusModes = parameters.getSupportedFocusModes();

            for (int i = 0; i < supportedPreviewSizes.size(); i++) {
                Log.d(TAG, "surfaceChanged supportedPreviewSizes=" + supportedPreviewSizes.size() + " width=" + supportedPreviewSizes.get(i).width + " height=" + supportedPreviewSizes.get(i).height);
            }

            for (int i = 0; i < supportedPictureSizes.size(); i++) {
                Log.d(TAG, "surfaceChanged supportedPictureSizes=" + supportedPictureSizes.size() + " width=" + supportedPictureSizes.get(i).width + " height=" + supportedPictureSizes.get(i).height);
            }

            for (int i = 0; i < focusModes.size(); i++) {
                Log.d(TAG, "surfaceChanged focusModes=" + focusModes.get(i));
            }

            //parameters.setPreviewSize(640, 480);

            //parameters.setPictureSize(640, 480);

       /* float previewRate = DisplayUtil.getScreenRate(this); //默认全屏的比例预览

        Log.d(TAG,"surfaceChanged previewRate="+previewRate+" width="+width+" height="+height);

        Camera.Size pictureSize = CamParaUtil.getInstance().getBestPreviewSize(
                parameters.getSupportedPictureSizes(), mWidth, mHeight);

        Log.d(TAG, "surfaceChanged pictureSize="+pictureSize.width + " : " + pictureSize.height);
        parameters.setPictureSize(pictureSize.width, pictureSize.height);


        Camera.Size previewSize = CamParaUtil.getInstance().getBestPreviewSize(
                parameters.getSupportedPreviewSizes(),mWidth, mHeight);
        parameters.setPreviewSize(previewSize.width, previewSize.height);
        Log.d(TAG, "surfaceChanged previewSize="+previewSize.width + " : " + previewSize.height);*/


            //List<String> focusModes = parameters.getSupportedFocusModes();
            //设置自动对焦功能
            if (focusModes.contains("auto")) {
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            }
            //parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);

            // myCamera.setParameters(parameters);

            setStartPreview(myCamera, mHolder);
        }catch (Exception e){
            finish();
            Log.d(TAG,e.toString());
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        releaseCamera();
    }
}
