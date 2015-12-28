package org.yanzi.example;

/**
 * Created by zhangcirui on 15/11/18.
 */
public class ImageObject {

    private Result result;
    private String imgUrl;

    public Result getResult() {
        return result;
    }

    public void setResult(Result result) {
        this.result = result;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    @Override
    public String toString() {
        return "ImageObject{" +
                "result=" + result +
                ", imgUrl='" + imgUrl + '\'' +
                '}';
    }
}
