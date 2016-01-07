package com.chenww.camera.ui.util;

import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.util.Log;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CamParaUtil {
	private static final String TAG = "yanzi";
	private CameraSizeComparator sizeComparator = new CameraSizeComparator();
	private static CamParaUtil myCamPara = null;
	private CamParaUtil(){

	}
	public static CamParaUtil getInstance(){
		if(myCamPara == null){
			myCamPara = new CamParaUtil();
			return myCamPara;
		}
		else{
			return myCamPara;
		}
	}

	public Camera.Size getBestPreviewSize(List<Size> sizes,int width, int height)
	{
		//List<Size> sizes = camera.getParameters().getSupportedPreviewSizes();
		if (sizes == null) return null;

		Size optimalSize = null;
		double minDiff = Double.MAX_VALUE;

		int targetHeight = height;
		int targetWidth = width;

		int minWidthDiff = 0;
		if (optimalSize == null) {
			minDiff = Double.MAX_VALUE;
			for (Size size : sizes) {
				if (Math.abs(size.width - targetWidth) < minDiff) {
					if(size.width > width) {
						if (minWidthDiff == 0) {
							minWidthDiff = size.width - width;
							optimalSize = size;
						}
						else if (Math.abs(size.width - targetWidth) < minWidthDiff) {
							minWidthDiff = size.width - width;
							optimalSize = size;

						}
						minDiff = Math.abs(size.width - targetWidth);
					}
				}
			}
		}

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

	public  Size getPropPreviewSize(List<Size> list, float th, int minWidth){
		Collections.sort(list, sizeComparator);

		int i = 0;
		for(Size s:list){
			if((s.width > th) && equalRate(s, 1.33f)){
				Log.d(TAG, "最终设置预览尺寸:w = " + s.width + "h = " + s.height);
				break;
			}
			i++;
		}

		return list.get(i);
	}
	public Size getPropPictureSize(List<Size> list, float th, int minWidth){
		Collections.sort(list, sizeComparator);

		int i = 0;
		for(Size s:list){
			if((s.width > th) && equalRate(s, 1.33f)) {
				Log.d(TAG, "最终设置图片尺寸:w = " + s.width + "h = " + s.height);
				break;
			}
			i++;
		}

		return list.get(i);
	}

	public boolean equalRate(Size s, float rate){
		float r = (float)(s.width)/(float)(s.height);
		if(Math.abs(r - rate) <= 0.3)
		{
			return true;
		}
		else{
			return false;
		}
	}

	public  class CameraSizeComparator implements Comparator<Size>{
		public int compare(Size lhs, Size rhs) {
			// TODO Auto-generated method stub
			if(lhs.width == rhs.width){
				return 0;
			}
			else if(lhs.width > rhs.width){
				return 1;
			}
			else{
				return -1;
			}
		}

	}

	/**打印支持的previewSizes
	 * @param params
	 */
	public  void printSupportPreviewSize(Camera.Parameters params){
		List<Size> previewSizes = params.getSupportedPreviewSizes();
		for(int i=0; i< previewSizes.size(); i++){
			Size size = previewSizes.get(i);
			Log.i(TAG, "previewSizes:width = "+size.width+" height = "+size.height);
		}

	}

	/**打印支持的pictureSizes
	 * @param params
	 */
	public  void printSupportPictureSize(Camera.Parameters params){
		List<Size> pictureSizes = params.getSupportedPictureSizes();
		for(int i=0; i< pictureSizes.size(); i++){
			Size size = pictureSizes.get(i);
			Log.i(TAG, "pictureSizes:width = "+ size.width
					+" height = " + size.height);
		}
	}
	/**打印支持的聚焦模式
	 * @param params
	 */
	public void printSupportFocusMode(Camera.Parameters params){
		List<String> focusModes = params.getSupportedFocusModes();
		for(String mode : focusModes){
			Log.i(TAG, "focusModes--" + mode);
		}
	}
}
