package com.chenww.camera.ui;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by zhangcirui on 15/11/20.
 */
class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {

    /**
     * 图片的URL地址
     */
    private String imageUrl;
    private ImageView mImageView;

    public BitmapWorkerTask(ImageView imageView){
        this.mImageView = imageView;
    }

    @Override
    protected Bitmap doInBackground(String... params) {
        imageUrl = params[0];
        // 在后台开始下载图片
        Bitmap bitmap = downloadBitmap(params[0]);
        if (bitmap != null) {
            // 图片下载完成后缓存到LrcCache中
            //addBitmapToMemoryCache(params[0], bitmap);
        }
        return bitmap;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);
        // 根据Tag找到相应的ImageView控件，将下载好的图片显示出来。
        /*ImageView imageView = (ImageView) mPhotoWall.findViewWithTag(imageUrl);
        if (imageView != null && bitmap != null) {
            imageView.setImageBitmap(bitmap);
        }
        taskCollection.remove(this);*/
        if(bitmap!=null){

           // mImageView.setImageBitmap(bitmap);

        }
    }

    /**
     * 建立HTTP请求，并获取Bitmap对象。
     *
     * @param imageUrl
     *            图片的URL地址
     * @return 解析后的Bitmap对象
     */
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

}