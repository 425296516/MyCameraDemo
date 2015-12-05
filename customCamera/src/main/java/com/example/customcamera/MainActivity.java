package com.example.customcamera;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ToggleButton;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class MainActivity extends Activity {

	/**
	 * 拍照预览
	 */
	private SurfaceView surfaceView;
	/**
	 * 拍照按钮
	 */
	private ImageView playImageView;
	/**
	 * 闪光灯
	 */
	private ToggleButton lightToggleButton;
	/**
	 * 前后置摄像头切换
	 */
	private ImageView switchImageView;
	/**
	 * 取消按钮
	 */
	private ImageView cancelImageView;
	/**
	 * 保存按钮
	 */
	private ImageView saveImageView;
	/**
	 * 浮层实例图片
	 */
	// private ImageView exampleImageView;
	/**
	 * 拍完预览图片
	 */
	private ImageView previewImageView;
	/**
	 * 头部的操作布局
	 */
	private RelativeLayout opeRelativeLayout;
	/**
	 *
	 */
	private RelativeLayout imageRelativeLayout;

	private SurfaceHolder surfaceHolder;
	private EasyCamera mEasyCamera;
	private VerticalTextView titleTextView;
	EasyCamera.CameraActions mCameraActions;
	private Bitmap previewBitmap;
	/**
	 * 判断是否在预览状态
	 */
	private boolean previewIsRunning = false;
	/**
	 * 0表示后置，1表示前置
	 */
	private int cameraPosition = 0;
	private Parameters parameter;
	private boolean isOpenForLight;
	private byte[] imageData;
	/**
	 * 0:本方碰撞部位特写 1:车辆碰撞部位(单车) 2:车辆碰撞部位(双车) 3:车架号 4:右后方(含车牌) 5:右前方(含车牌)
	 * 6:左后方(含车牌) 7:左前方(含车牌) 8:司机汽车合影 9:侧前方全景(含车牌.道路线标) 10: 其他
	 */
	private int index;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		surfaceView = (SurfaceView) findViewById(R.id.camera_claims_sv);
		playImageView = (ImageView) findViewById(R.id.play_claims_iv);
		lightToggleButton = (ToggleButton) findViewById(R.id.light_claims_tbtn);
		switchImageView = (ImageView) findViewById(R.id.switch_claims_iv);
		// exampleImageView = (ImageView) findViewById(R.id.example_claims_iv);
		cancelImageView = (ImageView) findViewById(R.id.cancel_claims_iv);
		saveImageView = (ImageView) findViewById(R.id.save_claims_iv);
		previewImageView = (ImageView) findViewById(R.id.preview_claims_iv);
		titleTextView = (VerticalTextView) findViewById(R.id.title_claims_tv);
		opeRelativeLayout = (RelativeLayout) findViewById(R.id.ope_claims_rl);
		imageRelativeLayout = (RelativeLayout) findViewById(R.id.image_claims_rl);

		surfaceHolder = surfaceView.getHolder();
		index = getIntent().getIntExtra("index", -1);

		cancelImageView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (saveImageView.getVisibility() == View.INVISIBLE) {
					finish();
				} else if (saveImageView.getVisibility() == View.VISIBLE) {
					saveImageView.setVisibility(View.INVISIBLE);
					playImageView.setVisibility(View.VISIBLE);
					opeRelativeLayout.setVisibility(View.VISIBLE);
					imageRelativeLayout.setVisibility(View.VISIBLE);
					surfaceView.setVisibility(View.VISIBLE);
					previewImageView.setVisibility(View.INVISIBLE);
					previewBitmap = null;

					// 释放资源
					releaseCamera();
					if (cameraPosition == 1) {
						// 前置摄像头
						switchCameraStatus(Camera.CameraInfo.CAMERA_FACING_FRONT);
					} else {
						// 后置摄像头
						switchCameraStatus(Camera.CameraInfo.CAMERA_FACING_BACK);
					}
				}
			}
		});

		switchImageView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				switchCamera();
				playImageView.setVisibility(View.VISIBLE);
				saveImageView.setVisibility(View.INVISIBLE);

			}
		});

		// saveImageView.setOnClickListener(new OnClickListener() {
		//
		// @Override
		// public void onClick(View v) {
		// if (imageData != null) {
		// String filePath = saveFile(imageData);
		// Log.i("MainActivity", "file path = " + filePath);
		// Intent intent = new Intent(MainActivity.this,
		// getIntent().getClass());
		// intent.putExtra("PATH", filePath);
		// setResult(Activity.RESULT_OK, intent);
		// finish();
		// }
		// }
		// });

		surfaceHolder.addCallback(new Callback() {

			@Override
			public void surfaceDestroyed(SurfaceHolder arg0) {
				Log.i("CameraActivity", "surfaceDestroyed");
				// 停止预览并释放
				releaseCamera();
				changeLightStatus(false);
			}

			@Override
			public void surfaceCreated(SurfaceHolder arg0) {
				Log.i("CameraActivity", "surfaceCreated");
				// 　开启后置摄像头
				mEasyCamera = DefaultEasyCamera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
				// 设置相机翻转角度
				// mEasyCamera.setDisplayOrientation(90);
				// 　正常显示相机预览
				WindowManager manager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
				mEasyCamera.alignCameraAndDisplayOrientation(manager);
			}

			@Override
			public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
				Log.i("CameraActivity", "surfaceChanged");

				if (!previewIsRunning && mEasyCamera != null) {
					try {
						// 开启预览
						mCameraActions = mEasyCamera.startPreview(arg0);
					} catch (IOException e) {
						e.printStackTrace();
					}
					previewIsRunning = true;
				}

				mEasyCamera.autoFocus(null);
			}
		});

		playImageView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				mEasyCamera.autoFocus(null);
				setPicturePix();
				changeLightStatus(isOpenForLight);

				EasyCamera.PictureCallback mPictureCallback = new EasyCamera.PictureCallback() {

					@Override
					public void onPictureTaken(byte[] data, EasyCamera.CameraActions actions) {
						imageData = data;
						changeLightStatus(false);
						// 停止预览
						// mEasyCamera.stopPreview();

						// try {
						// // 延时1秒钟，看的更加清楚
						// Thread.sleep(1000);
						// } catch (InterruptedException e) {
						// e.printStackTrace();
						// }
						// String imagePath = saveFile(data);
						// LogUtil.i(TAG, "image uri = " + imagePath);
						// previewBitmap = BitmapFactory.decodeFile(imagePath);

						playImageView.setVisibility(View.INVISIBLE);
						saveImageView.setVisibility(View.VISIBLE);
						opeRelativeLayout.setVisibility(View.INVISIBLE);
						imageRelativeLayout.setVisibility(View.INVISIBLE);
						surfaceView.setVisibility(View.INVISIBLE);
						previewImageView.setVisibility(View.VISIBLE);

						previewBitmap = imageRotate(data, 90);
						if (previewBitmap != null) {
							previewImageView.setImageBitmap(previewBitmap);
						}

						// 开启预览
						EasyCamera.Callbacks.create().withRestartPreviewAfterCallbacks(true);
					}
				};

				// 拍摄照片的操作
				mCameraActions.takePicture(EasyCamera.Callbacks.create().withJpegCallback(mPictureCallback));

			}
		});

		lightToggleButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (isOpenForLight) {
					isOpenForLight = false;
				} else {
					isOpenForLight = true;
				}
			}
		});
	}

	/**
	 * 设置拍照分辨率
	 */
	private void setPicturePix() {
		// Parameters parameters = mEasyCamera.getParameters();// 获取相机参数集
		// List<Size> SupportedPreviewSizes =
		// parameters.getSupportedPreviewSizes();// 获取支持预览照片的尺寸
		// Size previewSize = SupportedPreviewSizes.get(0);// 从List取出Size
		// parameters.setPreviewSize(previewSize.width, previewSize.height);//
		// 设置预览照片的大小
		// List<Size> supportedPictureSizes =
		// parameters.getSupportedPictureSizes();// 获取支持保存图片的尺寸
		// Size pictureSize = supportedPictureSizes.get(0);// 从List取出Size
		// parameters.setPictureSize(previewSize.width, previewSize.height);//
		// 设置照片的大小
		// mEasyCamera.setParameters(parameters);

		// 获取照相机的参数
		Parameters params = mEasyCamera.getParameters();
		// 获取照相机支持的分辨率列表
		List<Size> list = params.getSupportedPictureSizes();
		// 从列表中获取最接近手机分辨率的下标
		int index = getPictureSize(list);

		int w, h;
		w = list.get(index).width;
		h = list.get(index).height;

		// 设置参数
		params.setPictureSize(w, h);
		mEasyCamera.setParameters(params);
	}

	/**
	 * 获取拍照之后的尺寸
	 */
	private int getPictureSize(List<Size> sizes) {

		// 屏幕的宽度
		int screenWidth = getResources().getDisplayMetrics().widthPixels;
		int index = -1;

		for (int i = 0; i < sizes.size(); i++) {
			if (Math.abs(screenWidth - sizes.get(i).width) == 0) {
				index = i;
				break;
			}
		}
		// 当未找到与手机分辨率相等的数值,取列表中间的分辨率
		if (index == -1) {
			index = sizes.size() / 2;
		}

		return index;
	}

	// @Override
	// public boolean onTouchEvent(MotionEvent event) {
	// if (event.getAction() == MotionEvent.ACTION_DOWN) {// 按下时自动对焦
	// mEasyCamera.autoFocus(null);
	// }
	//
	// return true;
	// }

	/**
	 * 保存图片到本地
	 *
	 * @param data
	 */
	private String saveFile(byte[] data) {
		// 初始化存储路径
		File imageFile = initDirPath();
		String fileName = System.currentTimeMillis() + ".jpg";
		// 存储图片的操作
		try {
			File file = new File(imageFile, fileName);

			if (!file.exists()) {
				file.createNewFile();
			}

			FileOutputStream fos = null;
			fos = new FileOutputStream(file);
			fos.write(data);
			// previewBitmap.compress(Bitmap.CompressFormat.JPEG,
			// Constant.UPLOAD_PHOTO_QUALITY, fos);
			fos.flush();
			fos.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// Log.i(TAG, "image path = " + imageFile + File.separator + fileName);
		return imageFile + File.separator + fileName;
	}

	/**
	 * 初始化路径 检查路径是否存在 进行校验
	 *
	 * @return
	 */
	private File initDirPath() {
		// 图片的存储路径
		File imagePath = null;
		// 初始化图片保存路径
		// String photo_dir =
		// AbFileUtil.getImageDownloadDir(MyApplication.getInstance().getApplicationContext());
		// // status = Environment.getExternalStorageState();
		// if (AbStrUtil.isEmpty(photo_dir)) {
		// ToastUtil.showBottomtoast(this, "存储卡不存在");
		// imagePath = new File(getCacheDir().getAbsolutePath() + File.separator
		// + "download" + File.separator
		// + "cache_files" + File.separator);
		// } else {
		// imagePath = new File(photo_dir);
		// }
		if (!imagePath.exists()) {
			imagePath.mkdirs();
		}
		return imagePath;
	}

	/**
	 * 打开关闭闪光灯
	 *
	 * @param isOpen
	 */
	private void changeLightStatus(boolean isOpen) {
		if (isOpen) {
			// 打开闪光灯
			if (mEasyCamera != null) {
				parameter = mEasyCamera.getParameters();
				parameter.setFlashMode(Parameters.FLASH_MODE_TORCH);
				mEasyCamera.setParameters(parameter);
			}
		} else {
			// 关闭闪关灯
			if (mEasyCamera != null) {
				parameter = mEasyCamera.getParameters();
				parameter.setFlashMode(Parameters.FLASH_MODE_OFF);
				mEasyCamera.setParameters(parameter);
			}

		}
	}

	/**
	 * 切换前后摄像头
	 */
	@SuppressLint("NewApi")
	private void switchCamera() {
		// 切换前后摄像头
		CameraInfo cameraInfo = new CameraInfo();
		for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
			Camera.getCameraInfo(i, cameraInfo);// 得到每一个摄像头的信息
			if (cameraPosition == 1) {
				// 现在是前置，变更为后置
				if (cameraPosition == Camera.CameraInfo.CAMERA_FACING_FRONT) {
					lightToggleButton.setClickable(true);
					releaseCamera();
					switchCameraStatus(Camera.CameraInfo.CAMERA_FACING_BACK);
					cameraPosition = 0;
					break;
				}
			} else {
				// 现在是后置， 变更为前置
				if (cameraPosition == Camera.CameraInfo.CAMERA_FACING_BACK) {
					lightToggleButton.setClickable(false);
					releaseCamera();
					switchCameraStatus(Camera.CameraInfo.CAMERA_FACING_FRONT);
					cameraPosition = 1;
				}
			}
			break;
		}
	}

	private void switchCameraStatus(int cameraStatus) {
		// 打开当前选中的摄像头
		mEasyCamera = DefaultEasyCamera.open(cameraStatus);
		EasyCamera.Callbacks.create().withRestartPreviewAfterCallbacks(true);
		try {
			// 开启预览
			mEasyCamera.startPreview(surfaceHolder);
			mCameraActions = mEasyCamera.startPreview(surfaceHolder);
			// 　正常显示相机预览
			WindowManager manager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
			mEasyCamera.alignCameraAndDisplayOrientation(manager);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		releaseCamera();
	}

	public Bitmap Bytes2Bimap(byte[] b) {
		if (b.length != 0) {
			return BitmapFactory.decodeByteArray(b, 0, b.length);
		} else {
			return null;
		}
	}

	/**
	 * 记得释放camera，方便其他应用调用
	 */
	private void releaseCamera() {
		if (mEasyCamera != null) {
			mEasyCamera.stopPreview();
			mEasyCamera.release();
			mEasyCamera = null;
			mCameraActions = null;
			previewIsRunning = false;
		}
	}

	/**
	 * 图片旋转
	 *
	 * @param b
	 * @return
	 */
	private Bitmap imageRotate(byte[] b, int angle) {
		Bitmap bitmap = BitmapFactory.decodeByteArray(b, 0, b.length);
		Matrix matrix = new Matrix();
		if (cameraPosition == 1) {
			matrix.postScale(-1, 1); // 镜像水平翻转
		}
		matrix.postRotate(angle);
		bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
		// bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
		return bitmap;
	}

}
