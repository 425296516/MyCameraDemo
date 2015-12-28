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
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.util.Log;
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

import com.google.gson.Gson;
import com.jiuwei.upgrade_package_new.lib.UpgradeModule;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
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
    private Timer mTimer, mTimerUpload;
    private File PHOTO_DIR = new File(Environment.getExternalStorageDirectory().getPath() + "/aigo_kt03/");
    private PowerManager.WakeLock mWakeLock;
    private TextView mTime;
    private Preview preview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 无title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // 全屏 
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //设置布局
        setContentView(R.layout.activity_camera);

        preview = new Preview(this, (SurfaceView) findViewById(R.id.camera_surfaceview));
        preview.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        ((RelativeLayout) findViewById(R.id.layout)).addView(preview);
        preview.setKeepScreenOn(true);

        mTime = (TextView) findViewById(R.id.tv_time);

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "My Tag");
        mWakeLock.acquire();

        initData();

        initThread();

        UpgradeModule.init(this);
        UpgradeHelper.checkUpgrade(
                this,
                true,
                com.jiuwei.upgrade_package_new.lib.Constant.DIALOG_STYLE_ELDERLY_ASSISTANT);

    }


    public void initThread() {

        mTimer = new Timer();

        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {

                bitmap = downloadBitmap(downUrl);
                //BitmapWorkerTask task = new BitmapWorkerTask(mImageView);
                //task.execute(downUrl);
                if (bitmap != null) {
                    mShowHandler.sendEmptyMessage(0);
                }

            }
        }, 0, 1000);

        mTimerUpload = new Timer();

        mTimerUpload.schedule(new TimerTask() {
            @Override
            public void run() {

                handlerDown.sendEmptyMessage(0);

            }
        }, 0, 2000);


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
                    //Toast.makeText(ctx, getString(R.string.camera_not_found), Toast.LENGTH_LONG).show();
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
                    //Toast.makeText(ctx, getString(R.string.camera_not_found), Toast.LENGTH_LONG).show();
                }
            }
        }


    }

    @Override
    protected void onPause() {
        if (myCamera != null) {
            myCamera.stopPreview();
            preview.setCamera(null);
            myCamera.release();
            myCamera = null;
        }
        super.onPause();
    }


    private Bitmap bitmap;
    private Handler mShowHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {

            if (mImageView != null && bitmap != null) {
                mImageView.setImageBitmap(bitmap);
            }

            SimpleDateFormat dataFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            mTime.setText(dataFormat.format(System.currentTimeMillis()));

        }
    };

    private Bitmap downloadBitmap(String imageUrl) {
        Bitmap bitmap = null;
        HttpURLConnection con = null;
        try {
            URL url = new URL(imageUrl);
            con = (HttpURLConnection) url.openConnection();
            con.setConnectTimeout(5 * 1000);
            con.setReadTimeout(10 * 1000);
            bitmap = BitmapFactory.decodeStream(con.getInputStream());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (con != null) {
                con.disconnect();
            }
        }
        return bitmap;
    }

    private Button mClick;

    public void initData() {

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
                            myCamera.takePicture(null, rawCallback, myPicCallback);
                        } catch (Exception e) {

                        }

                    }
                }, 0, 2000);

            }
        });

    }

    PictureCallback rawCallback = new PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
            //			 Log.d(TAG, "onPictureTaken - raw");
        }
    };


    private Timer mTimerPai;


    public void uploadImage(File file, String uploadUrl) {

        Log.d(TAG, file.getPath() + " : " + uploadUrl);
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        try {
            params.put("deviceOrder", uploadSign);
            params.put("func", "upload");
            params.put("file", file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        client.post(uploadUrl, params, new AsyncHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody) {
                ImageObject netUploadFile = new Gson().fromJson(new String(responseBody), ImageObject.class);

                if (netUploadFile.getResult().isResult()) {
                    Log.d(TAG, netUploadFile.getImgUrl());

                } else {
                    Log.d(TAG, "errrr1");

                }
            }

            @Override
            public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody, Throwable error) {

            }
        });

    }

    private Handler handlerDown = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            final File pictureFile = new File(PHOTO_DIR.getPath(), "camera.jpg");

            if (pictureFile.exists()) {
                uploadImage(pictureFile, uploadUrl);
            }
            Log.d(TAG, "handlerDown");

        }
    };


    //int degree = 0;
    //拍照成功回调函数
    private PictureCallback myPicCallback = new PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            myCamera.startPreview();
            preview.setCamera(camera);
            //将得到的照片进行270°旋转，使其竖直
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            Matrix matrix = new Matrix();

            matrix.preRotate(0);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            bitmap = comp(bitmap);
            if (!PHOTO_DIR.exists()) {
                PHOTO_DIR.mkdirs();
            }
            try {
                //创建并保存图片文件
                final File pictureFile = new File(PHOTO_DIR.getPath(), "camera.jpg");
                if (!pictureFile.exists()) {
                    pictureFile.createNewFile();
                }

                FileOutputStream fos = new FileOutputStream(pictureFile);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                bitmap.recycle();
                fos.close();

                refreshGallery(pictureFile);
                //handler.sendEmptyMessageDelayed(0, 2000);

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


    private Bitmap comp(Bitmap image) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        if (baos.toByteArray().length / 1024 > 100) {//判断如果图片大于1M,进行压缩避免在生成图片（BitmapFactory.decodeStream）时溢出
            baos.reset();//重置baos即清空baos
            image.compress(Bitmap.CompressFormat.JPEG, 50, baos);//这里压缩50%，把压缩后的数据存放到baos中
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        //开始读入图片，此时把options.inJustDecodeBounds 设回true了
        newOpts.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);
        newOpts.inJustDecodeBounds = false;
        int w = newOpts.outWidth;
        int h = newOpts.outHeight;
        //现在主流手机比较多是800*480分辨率，所以高和宽我们设置为
        float hh = 800f;//这里设置高度为800f
        float ww = 480f;//这里设置宽度为480f
        //缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        int be = 1;//be=1表示不缩放
        if (w > h && w > ww) {//如果宽度大的话根据宽度固定大小缩放
            be = (int) (newOpts.outWidth / ww);
        } else if (w < h && h > hh) {//如果高度高的话根据宽度固定大小缩放
            be = (int) (newOpts.outHeight / hh);
        }
        if (be <= 0)
            be = 1;
        newOpts.inSampleSize = be;//设置缩放比例
        newOpts.inPreferredConfig = Bitmap.Config.RGB_565;//降低图片从ARGB888到RGB565
        //重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
        isBm = new ByteArrayInputStream(baos.toByteArray());
        bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);
        return compressImage(bitmap);//压缩好比例大小后再进行质量压缩
    }

    private Bitmap compressImage(Bitmap image) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);//质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        int options = 100;
        while (baos.toByteArray().length / 1024 > 100) {    //循环判断如果压缩后图片是否大于100kb,大于继续压缩
            baos.reset();//重置baos即清空baos
            options -= 10;//每次都减少10
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);//这里压缩options%，把压缩后的数据存放到baos中

        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());//把压缩后的数据baos存放到ByteArrayInputStream中
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);//把ByteArrayInputStream数据生成图片
        return bitmap;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        Log.d(TAG, "onDestroy");
        if (mTimer != null) {
            mTimer.cancel();
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
            myCamera.release();
            myCamera = null;
        }
    }
}
