package org.yanzi.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageButton;

import org.yanzi.camera.CameraInterface;
import org.yanzi.camera.CameraInterface.CamOpenOverCallback;
import org.yanzi.camera.preview.CameraSurfaceView;
import org.yanzi.playcamera.R;
import org.yanzi.util.DisplayUtil;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

public class CameraActivity extends Activity implements CamOpenOverCallback {
	private static final String TAG = "yanzi";
	CameraSurfaceView surfaceView = null;
	ImageButton shutterBtn;
	float previewRate = -1f;
	private File PHOTO_DIR = new File(Environment.getExternalStorageDirectory().getPath() + "/aigo_kt03/");
	private String uploadUrl = "http://115.28.43.225:83/pet/fileUploadForClient.html";
	private Timer mTimerUpload;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Thread openThread = new Thread(){
			@Override
			public void run() {
				CameraInterface.getInstance().doOpenCamera(CameraActivity.this);
			}
		};
		openThread.start();
		setContentView(R.layout.activity_camera);
		initUI();
		initViewParams();

		shutterBtn.setOnClickListener(new BtnListeners());

		mTimerUpload = new Timer();

		mTimerUpload.schedule(new TimerTask() {
			@Override
			public void run() {

				handlerDown.sendEmptyMessage(0);

			}
		}, 0, 3000);

	}

	private Handler handlerDown = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			//String path = FileUtil.initPath()+"/" +"camera.jpg";
			final File pictureFile = new File(PHOTO_DIR, "camera.jpg");

			Log.d(TAG,pictureFile.getAbsolutePath());
			if (pictureFile.exists()) {
				CameraModule.getInstance().uploadImage(pictureFile, uploadUrl, "b");
			}
			Log.d(TAG, "handlerDown");

		}
	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.camera, menu);
		return true;
	}

	private void initUI(){
		surfaceView = (CameraSurfaceView)findViewById(R.id.camera_surfaceview);
		shutterBtn = (ImageButton)findViewById(R.id.btn_shutter);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(mTimerUpload!=null){
			mTimerUpload.cancel();
		}
	}

	private void initViewParams(){
		//LayoutParams params = surfaceView.getLayoutParams();
		//Point p = DisplayUtil.getScreenMetrics(this);
		//params.width = p.x;
		//params.height = p.y;
		previewRate = DisplayUtil.getScreenRate(this); //默认全屏的比例预览
		//surfaceView.setLayoutParams(params);

		//手动设置拍照ImageButton的大小为120dip×120dip,原图片大小是64×64
		LayoutParams p2 = shutterBtn.getLayoutParams();
		p2.width = DisplayUtil.dip2px(this, 80);
		p2.height = DisplayUtil.dip2px(this, 80);;
		shutterBtn.setLayoutParams(p2);

	}

	@Override
	public void cameraHasOpened() {
		// TODO Auto-generated method stub
		SurfaceHolder holder = surfaceView.getSurfaceHolder();
		CameraInterface.getInstance().doStartPreview(holder, previewRate);
	}
	private class BtnListeners implements OnClickListener{

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch(v.getId()){
				case R.id.btn_shutter:
					Timer timer = new Timer();
					timer.schedule(new TimerTask() {
						@Override
						public void run() {
							CameraInterface.getInstance().doTakePicture();
						}
					}, 0, 2000);

					break;
				default:break;
			}
		}

	}
}
