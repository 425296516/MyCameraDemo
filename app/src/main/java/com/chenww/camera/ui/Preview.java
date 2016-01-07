package com.chenww.camera.ui;

/**
 * @author Jose Davis Nidhin
 */

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;

import java.io.IOException;
import java.util.List;

class Preview extends ViewGroup implements SurfaceHolder.Callback {
    private final String TAG = "Preview";

    SurfaceView mSurfaceView;
    SurfaceHolder mHolder;
    Size mPreviewSize;
    List<Size> mSupportedPreviewSizes;
    Camera mCamera;

    Preview(Context context, SurfaceView sv) {
        super(context);

        mSurfaceView = sv;
//        addView(mSurfaceView);
       
        mHolder = mSurfaceView.getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    public void setCamera(Camera camera,int degree) {
    	mCamera = camera;
    	if (mCamera != null) {
    		mSupportedPreviewSizes = mCamera.getParameters().getSupportedPreviewSizes();

    		requestLayout();

    		// get Camera parameters
    		Camera.Parameters params = mCamera.getParameters();

    		List<String> focusModes = params.getSupportedFocusModes();
    		if (focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
    			// set the focus mode
    			params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
    			// set Camera parameters
    			mCamera.setParameters(params);
    		}
    	}
        mCamera.setDisplayOrientation(degree);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // We purposely disregard child measurements because act as a
        // wrapper to a SurfaceView that centers the camera preview instead
        // of stretching it.
        final int width = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        final int height = resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        setMeasuredDimension(width, height);

        if (mSupportedPreviewSizes != null) {
            if(android.os.Build.MODEL.equals("MK16i")){
                mPreviewSize = mSupportedPreviewSizes.get(0);
            }else{
                mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes, width, height);
            }
        }

        Log.d(TAG,"onMeasure mPreviewSize " + mPreviewSize.width + " " + mPreviewSize.height);
        Log.d(TAG, "onMeasure " + width + " " + height);

    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        /*Log.d(TAG,"onLayout"+changed+""+getChildCount());
        if (changed && getChildCount() > 0) {
            Log.d(TAG,"onLayout2");
            final View child = getChildAt(0);

            final int width = r - l;
            final int height = b - t;

            int previewWidth = width;
            int previewHeight = height;
            if (mPreviewSize != null) {
                previewWidth = mPreviewSize.width;
                previewHeight = mPreviewSize.height;
            }
            Log.d(TAG,previewWidth+" : "+previewHeight+"height");

            // Center the child SurfaceView within the parent.
            if (width * previewHeight > height * previewWidth) {
                final int scaledChildWidth = previewWidth * height / previewHeight;
                child.layout((width - scaledChildWidth) / 2, 0,
                        (width + scaledChildWidth) / 2, height);

                Log.d(TAG, "aaaa");
            } else {
                final int scaledChildHeight = previewHeight * width / previewWidth;
                child.layout(0, (height - scaledChildHeight) / 2,
                        width, (height + scaledChildHeight) / 2);

                Log.d(TAG, "bbbb");
            }
        }*/
    }

    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, acquire the camera and tell it where
        // to draw.
        try {
            if (mCamera != null) {
                mCamera.setPreviewDisplay(holder);
            }
        } catch (IOException exception) {
            Log.e(TAG, "IOException caused by setPreviewDisplay()", exception);
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        // Surface will be destroyed when we return, so stop the preview.
        if (mCamera != null) {
            mCamera.stopPreview();
        }
    }


    private Size getOptimalPreviewSize(List<Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) w / h;
        if (sizes == null) return null;

        Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;
        // Try to find an size match aspect ratio and size
        for (Size size : sizes) {
            Log.d(TAG,"size=="+size.width+" : "+size.height);
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        // Cannot find the one match the aspect ratio, ignore the requirement
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
    	if(mCamera != null) {
            Log.d(TAG, "format=" + format + " w=" + w + " h=" + h);
    		Camera.Parameters parameters = mCamera.getParameters();
            Log.d(TAG, "surfaceChanged " + mPreviewSize.width + "" + mPreviewSize.height);
    		parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
            parameters.setPictureSize(mPreviewSize.width, mPreviewSize.height);
    		//requestLayout();

    		mCamera.setParameters(parameters);
    		mCamera.startPreview();
    	}
    }

}
