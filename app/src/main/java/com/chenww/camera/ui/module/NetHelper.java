package com.chenww.camera.ui.module;

import android.util.Log;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import rx.Observable;
import rx.exceptions.Exceptions;
import rx.functions.Func0;

/**
 * Created by gaoyf on 15/6/10.
 */
public class NetHelper {
    /**
     * 获取数据，用于也是用Observable的方式去处理
     *
     * @param url
     * @return
     */
    public static Observable<String> getData(final String url) {
        return Observable.defer(new Func0<Observable<String>>() {
            @Override
            public Observable<String> call() {
                try {
                    return Observable.just(getContent(url));
                } catch (IOException e) {
                    Exceptions.propagate(e);
                }
                return null;
            }
        });
    }

    public static Observable<File> downloadFile(final String url, final File file) {
        return Observable.defer(new Func0<Observable<File>>() {
            @Override
            public Observable<File> call() {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder().url(url)
                        .addHeader("Content-Type", "application/json").build();
                try {
                    Response response = client.newCall(request).execute();

                    InputStream in = response.body().byteStream();
                    FileOutputStream fileOutputStream = new FileOutputStream(file);
                    byte data[] = new byte[1024];
                    int count;
                    while ((count = in.read(data)) != -1) {
                        fileOutputStream.write(data, 0, count);
                    }
                    fileOutputStream.close();
                    in.close();
                    return Observable.just(file);
                } catch (Exception e) {
                    Exceptions.propagate(e);
                }
                return null;
            }
        });
    }

    public static File downloadFileSync(final String url, final File file) {

        Ln.d("DownloadFileSync:" + url + ",File:" + file.getPath());
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url)
                .addHeader("Content-Type", "application/json").build();
        try {
            Response response = client.newCall(request).execute();

            InputStream in = response.body().byteStream();
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            byte data[] = new byte[1024];
            int count;
            while ((count = in.read(data)) != -1) {
                fileOutputStream.write(data, 0, count);
            }
            fileOutputStream.close();
            in.close();
            return file;
        } catch (Exception e) {
            Exceptions.propagate(e);
        }
        return null;
    }

    /**
     * 获取数据，用于也是用Observable的方式去处理
     *
     * @param url
     * @return
     */
    public static Observable<String> postData(final String url, final RequestBody body) {
        return Observable.defer(new Func0<Observable<String>>() {
            @Override
            public Observable<String> call() {
                try {
                    return Observable.just(postContent(url,body));
                } catch (IOException e) {
                    Exceptions.propagate(e);
                }
                return null;
            }
        });
    }

    /**
     * 只有这里没有使用异步，但是OkHttpClient也是非常的简单明了
     * @param urlString
     * @return
     * @throws IOException
     */
    public static String getContent(String urlString) throws IOException {
        Ln.d("Request:getContent:" + urlString);
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(urlString)
                .build();
        Response response = client.newCall(request).execute();
        String result = response.body().string();
        Ln.d("Request:getContent:Response:" + result);
        return result;
    }

    /**
     * 只有这里没有使用异步，但是OkHttpClient也是非常的简单明了
     * @param urlString
     * @return
     * @throws IOException
     */
    public static String postContent(String urlString,RequestBody body) throws IOException {
        Ln.d("Request:postContent:Url:" + urlString);

        OkHttpClient client = new OkHttpClient();
        Ln.d("Request:postContent(1)");
        Request request = new Request.Builder()
                .url(urlString)
                .post(body)
                .build();
        Ln.d("Request:postContent(2):" + request.toString());
        Response response = client.newCall(request).execute();
        Ln.d("Request:postContent(3):" + response.toString());
        String result = response.body().string();
        Ln.d("Request:postContent:Url:" + result);
        return result;
    }

    public static class Ln {
        public static void d(String log) {
            Log.d("NetHelper", log);
        }
    }
}
