package com.chenww.camera.ui.application;

import com.chenww.camera.ui.R;
import com.chenww.camera.ui.db.SPManager;

import org.acra.ReportField;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

/**
 * Created by qinqi on 15/3/24.
 */

@ReportsCrashes(formKey = "",
        mailTo = "qinqi@jiuweist.com",
        mode = ReportingInteractionMode.TOAST,
        resToastText = R.string.crash_toast_text,
        customReportContent = {
                ReportField.DEVICE_ID,
                ReportField.APP_VERSION_NAME,
                ReportField.ANDROID_VERSION,
                ReportField.PHONE_MODEL,
                ReportField.CUSTOM_DATA,
                ReportField.STACK_TRACE,
                ReportField.LOGCAT},
        logcatArguments = {"-t", "200", "-v", "time"})
public class MyApplication extends android.app.Application {
    @Override
    public void onCreate() {
        super.onCreate();
        //ACRA.init(this);

        //UserModule.getInstance().init(this);

        SPManager.getInstance().init(this);
        //Tools.init(this);

    }

}
