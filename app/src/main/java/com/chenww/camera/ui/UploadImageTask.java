package com.chenww.camera.ui;

import android.os.AsyncTask;
import android.os.Environment;

import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * Created by zhangcirui on 15/11/20.
 */
class UploadImageTask extends AsyncTask<String, Void, Void> {

    private String uploadUrl = "http://115.28.43.225:83/pet/fileUploadForClient.html";

    private File PHOTO_DIR = new File(Environment.getExternalStorageDirectory().getPath() + "/aigo_kt03/");

    private String uploadSign;

    public UploadImageTask(String uploadSign){
        this.uploadSign = uploadSign;
    }

    @Override
    protected Void doInBackground(String... params) {

        final File pictureFile = new File(PHOTO_DIR.getPath(), "camera.jpg");

        if (pictureFile.exists()) {
            uploadImage(pictureFile, uploadUrl);
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void v) {
        super.onPostExecute(v);

    }

    public void uploadImage(File file, String uploadUrl) {

        com.loopj.android.http.AsyncHttpClient client = new com.loopj.android.http.AsyncHttpClient();
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

                } else {

                }
            }

            @Override
            public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody, Throwable error) {

            }
        });

    }

}