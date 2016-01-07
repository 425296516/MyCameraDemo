package example.com.mooccamera;

import android.app.Activity;
import android.content.Context;
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
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity implements SurfaceHolder.Callback {

    private static final String TAG = MainActivity.class.getSimpleName();
    private Camera mCamera;
    private SurfaceView mPreview;
    private SurfaceHolder mHolder;
    private int mScreenHeight, mScreenWidth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        lightScreen();

        mPreview = (SurfaceView) findViewById(R.id.preview);
        mHolder = mPreview.getHolder();
        mHolder.addCallback(this);

        // 得到屏幕的大小
        WindowManager wManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        Display display = wManager.getDefaultDisplay();
        mScreenHeight = display.getHeight();
        mScreenWidth = display.getWidth();

        mPreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCamera.autoFocus(null);
            }
        });

    }

    private PowerManager.WakeLock mWakeLock;

    public void lightScreen() {
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "My Tag");
        mWakeLock.acquire();
    }


    public void capture(View view) {

        mCamera.autoFocus(new Camera.AutoFocusCallback() {
            @Override
            public void onAutoFocus(boolean success, Camera camera) {
                if (success) {

                    Timer timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {

                            try {
                                mCamera.takePicture(null, null, mPictureCallback);
                            } catch (Exception e) {
                                setStartPreview(mCamera, mHolder);
                                Log.d(TAG,"takePicture="+e.toString());
                            }

                        }
                    }, 0, 3000);

                }
            }
        });


    }

    private Camera.PictureCallback mPictureCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            File temp = new File("/sdcard/temp.png");

            try {
                FileOutputStream fos = new FileOutputStream(temp);
                fos.write(data);
                fos.close();

                Bitmap bitmap = drawTextToBitmap(getApplicationContext(), temp.toString(), getStringDate());
                saveBitmap(bitmap);

               /*  Intent intent = new Intent(getApplicationContext(), ResultActivity.class);
                intent.putExtra("picPath", temp.getAbsolutePath());
                startActivity(intent);

                finish();*/

                setStartPreview(mCamera, mHolder);

            } catch (Exception e) {
               Log.d(TAG,"onPictureTaken="+e.toString());
                e.printStackTrace();
            }

        }
    };

    /**
     * 获取现在时间
     *
     * @return返回字符串格式 yyyy-MM-dd HH:mm:ss
     */
    public static String getStringDate() {
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateString = formatter.format(currentTime);
        return dateString;
    }

    public static void saveBitmap(Bitmap b){

        File temp = new File("/sdcard/temp.png");
        long dataTake = System.currentTimeMillis();
        String jpegName = temp.toString();
        Log.i(TAG, "saveBitmap:jpegName = " + jpegName);
        try {
            FileOutputStream fout = new FileOutputStream(jpegName);
            BufferedOutputStream bos = new BufferedOutputStream(fout);
            b.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            bos.flush();
            bos.close();
            Log.i(TAG, "saveBitmap成功");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            Log.i(TAG, "saveBitmap:失败");
            e.printStackTrace();
        }

    }

    /**
     * 获取camera对象
     */
    private Camera getCamera() {
        Camera camera;
        camera = Camera.open();
        return camera;
    }


    @Override
    protected void onResume() {
        super.onResume();

        if (mCamera == null) {
            mCamera = getCamera();
            if (mHolder != null) {
                setStartPreview(mCamera, mHolder);
            }
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        releaseCamera();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mWakeLock != null) {
            mWakeLock.release();
        }

    }

    /**
     * 开始预览相机内容
     */
    private void setStartPreview(Camera camera, SurfaceHolder holder) {

        try {
            camera.setPreviewDisplay(holder);
            //将系统预览角度进行调整
            //camera.setDisplayOrientation(90);
            camera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 释放相机资源
     */
    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }

    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        setStartPreview(mCamera, mHolder);

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.d(TAG, "format= " + format + " width=" + width + " height=" + height);
        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setPictureFormat(ImageFormat.JPEG);

        List<Camera.Size> list = parameters.getSupportedPictureSizes();
        for (int i = 0; i < list.size(); i++) {
            Log.d(TAG, "list=" + list.size() + " width=" + list.get(i).width + " height=" + list.get(i).height);
        }
        Camera.Size size = list.get(8);

        Log.d(TAG, "screen:width=" + mScreenWidth + " height=" + mScreenHeight);

        //设置预览大小
        // parameters.setPreviewSize(size.width, size.height);
        //设置图片大小
        //parameters.setPictureSize(size.width, size.height);
        //设置自动对焦功能
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);

        mCamera.setParameters(parameters);

        mCamera.stopPreview();
        setStartPreview(mCamera, mHolder);

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        releaseCamera();
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
                                   String gResId,
                                   String gText) {
        Resources resources = gContext.getResources();
        float scale = resources.getDisplayMetrics().density;
        Bitmap bitmap = BitmapFactory.decodeFile(gResId);

        //bitmap = scaleWithWH(bitmap, 300*scale, 300*scale);
        Log.d(TAG,"bitmap="+" scale="+scale+" : "+bitmap.getWidth()+" : "+bitmap.getHeight());
        android.graphics.Bitmap.Config bitmapConfig =
                bitmap.getConfig();

        // set default bitmap config if none
        if(bitmapConfig == null) {
            bitmapConfig = android.graphics.Bitmap.Config.ARGB_8888;
        }
        // resource bitmaps are imutable,
        // so we need to convert it to mutable one
        bitmap = bitmap.copy(bitmapConfig, true);
        Log.d(TAG,"bitmap="+bitmap.getWidth()+" : "+bitmap.getHeight());
        Canvas canvas = new Canvas(bitmap);
        // new antialised Paint
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        // text color - #3D3D3D
        paint.setColor(Color.RED);
        paint.setTextSize((int) (18 * scale));
        paint.setDither(true); //获取跟清晰的图像采样
        paint.setFilterBitmap(true);//过滤一些
        Rect bounds = new Rect();
        paint.getTextBounds(gText, 0, gText.length(), bounds);
        int x = 30;
        int y = 30;
        canvas.drawText(gText, x * scale, y * scale, paint);
        Log.d(TAG, "bitmap=" + bitmap.getWidth() + " : " + bitmap.getHeight());
        return bitmap;
    }


}
