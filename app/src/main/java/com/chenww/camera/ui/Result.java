package com.chenww.camera.ui;

/**
 * Created by zhangcirui on 15/11/18.
 */
public class Result {

    private boolean result;
    private String reason;

    public boolean isResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    @Override
    public String toString() {
        return "Result{" +
                "result=" + result +
                ", reason='" + reason + '\'' +
                '}';
    }
}
