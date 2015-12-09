package com.chenww.camera.ui;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.jiuwei.upgrade_package_new.lib.UpgradeModule;
import com.jiuwei.upgrade_package_new.lib.obj.KbAppObject;
import com.jiuwei.upgrade_package_new.lib.obj.KbRomAppObject;

import java.text.SimpleDateFormat;

/**
 * Created by gaoyf on 15/6/13.
 */
public class UpgradeHelper {
    private static boolean downloadFileIsSuccess = false;
    private static String mPackageName = "com.chenww.camera.ui";
    private static String mUrl = "http://api.aigolife.com/device/GetApplicationInfo";
    private static Dialog mDialog;
    private static ProgressDialog mProgressDialog;

    public static void checkUpgrade(final Activity activity, final boolean silence, final int dialogStyle) {
        UpgradeModule.getInstance().autoInstall(true)
                .autoInstall(true)
                .packageName(mPackageName)
                .url(mUrl)
                .checkNewVersion(new UpgradeModule.OnCheckListener() {
                    @Override
                    public void checkStart() {

                    }

                    @Override
                    public void checkError(String s) {
                        if (!silence)
                            Toast.makeText(activity, "检查版本出错", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void newVersion(final KbAppObject kbAppObject) {
                        switch (dialogStyle) {
                            case com.jiuwei.upgrade_package_new.lib.Constant.DIALOG_STYLE_SYSTEM:
                                showUpgradeDialogToSystem(activity, kbAppObject);
                                break;
                            case com.jiuwei.upgrade_package_new.lib.Constant.DIALOG_STYLE_ELDERLY_ASSISTANT:
                                showUpgradeDialogToElderlyAssistant(activity, kbAppObject);
                                break;
                            case com.jiuwei.upgrade_package_new.lib.Constant.DIALOG_STYLE_MOMMY_ASSISTANT:
                                break;
                            case com.jiuwei.upgrade_package_new.lib.Constant.DIALOG_STYLE_TRANSLATION_STUDIES_MUSEUM:
                                break;
                            default:
                                showUpgradeDialogToSystem(activity, kbAppObject);
                                break;
                        }
                    }

                    @Override
                    public void noNewVersion(KbAppObject kbAppObject) {
                        if (!silence)
                            Toast.makeText(activity, "没有新版本", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private static void download(KbAppObject kbAppObject) {
        UpgradeModule.getInstance().download(kbAppObject, true,
                new UpgradeModule.OnDownloadListener() {
                    @Override
                    public void downloadStart() {
//                        Toast.makeText(getClass(), "开始下载", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void haveDownloading() {

                    }

                    @Override
                    public void downloadSuccess(String s) {

                        UpgradeModule.getInstance().installFile();
                    }

                    @Override
                    public void downloadError(String s) {

                    }
                });
    }

    public static void checkUpgradeByWifi(final Activity activity, final boolean silence, final int dialogStyle) {
        if (getNetWorkType(activity) == com.jiuwei.upgrade_package_new.lib.Constant.NETWORKTYPE_WIFI) {

            UpgradeModule.getInstance().autoInstall(true)
                    .autoInstall(true)
                    .packageName(mPackageName)
                    .url(mUrl)
                    .checkNewVersion(new UpgradeModule.OnCheckListener() {
                        @Override
                        public void checkStart() {

                        }

                        @Override
                        public void checkError(String s) {

                            if (!silence)
                                Toast.makeText(activity, "检查版本出错", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void newVersion(final KbAppObject kbAppObject) {
                            Toast.makeText(activity, "有新版本", Toast.LENGTH_SHORT).show();

                            UpgradeModule.getInstance().download(kbAppObject, false,
                                    new UpgradeModule.OnDownloadListener() {
                                        @Override
                                        public void downloadStart() {

                                        }

                                        @Override
                                        public void haveDownloading() {

                                        }

                                        @Override
                                        public void downloadSuccess(String s) {


                                            switch (dialogStyle) {
                                                case com.jiuwei.upgrade_package_new.lib.Constant.DIALOG_STYLE_SYSTEM:
                                                    showUpgradeDialogToSystem(activity, kbAppObject);
                                                    break;
                                                case com.jiuwei.upgrade_package_new.lib.Constant.DIALOG_STYLE_ELDERLY_ASSISTANT:
                                                    showUpgradeDialogToElderlyAssistant(activity, kbAppObject);
                                                    break;
                                                case com.jiuwei.upgrade_package_new.lib.Constant.DIALOG_STYLE_MOMMY_ASSISTANT:
                                                    break;
                                                case com.jiuwei.upgrade_package_new.lib.Constant.DIALOG_STYLE_TRANSLATION_STUDIES_MUSEUM:
                                                    break;
                                                default:
                                                    showUpgradeDialogToSystem(activity, kbAppObject);
                                                    break;
                                            }
                                        }

                                        @Override
                                        public void downloadError(String s) {

                                        }
                                    });
                        }

                        @Override
                        public void noNewVersion(KbAppObject kbAppObject) {
                            if (!silence)
                                Toast.makeText(activity, "没有新版本", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {

        }

    }

    public static void checkRomUpgrade(final Activity activity, final boolean silence, int versionCode) {
        checkRomPackage(activity, "com.aigo.kt03.user1", silence, versionCode);
        checkRomPackage(activity, "com.aigo.kt03.user2", silence, versionCode);
    }

    private static void checkRomPackage(final Activity activity, String packageName, final boolean silence, int versionCode) {
        UpgradeModule.getInstance().autoInstall(true)
                .autoInstall(true)
                .packageName(packageName)
                .url("http://api.aigolife.com/device/GetFirmwareInfo")
                .checkRomNewVersion(new UpgradeModule.OnCheckRomListener() {
                    @Override
                    public void checkStart() {

                    }

                    @Override
                    public void checkError(String s) {

                        if (!silence)
                            Toast.makeText(activity, "检查版本出错", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void newVersion(final KbRomAppObject kbRomAppObject) {

                        if (!silence){
                            // ToastUtil.showToast(activity, "有新版本");
                        }
                        showRomUpgradeDialog(activity);
                    }

                    @Override
                    public void noNewVersion(KbRomAppObject kbRomAppObject) {
                        if (!silence){

                        }
                            //ToastUtil.showToast(activity, "没有新版本");


                    }
                }, versionCode);
    }

    private static void showRomUpgradeDialog(final Activity activity) {

        if (mDialog != null) {
            return;
        }

        mDialog = new AlertDialog.Builder(activity).setTitle("提示")
                .setMessage("您现在不是最新的固件版本，是否确认要升级")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mDialog = null;

                        //showRomProgressDialog(activity,listener);
                        //触发升级接口


                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mDialog = null;
                    }
                }).show();
    }

    private static ProgressDialog dialog;

    /*private static void showRomProgressDialog(final Activity activity,final KT03Module.OnPostListener listener) {

        dialog = new ProgressDialog(activity);
        View view = dialog.getLayoutInflater().inflate(R.layout.dlg_firmware_update, null);
        final RoundProgressBarWidthNumber roundProgressBarWidthNumber = (RoundProgressBarWidthNumber) view.findViewById(R.id.progress_bar);
        dialog.setCancelable(false);
        dialog.show();
        dialog.setContentView(view);

        new CountDownTimer(60 * 1000, 1000) {
            @Override
            public void onTick(long l) {
                roundProgressBarWidthNumber.setProgress((int) ((60 - l / 1000) / 60.0 * 100));
                Log.d("CountDownTimer",""+l/1000);
                if(l/1000<3){
                    KT03Module.getInstance().getKT03Connect();
                }
            }

            @Override
            public void onFinish() {
                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                }
                if(listener != null)
                    listener.onSuccess(true);
            }
        }.start();
    }*/

    private static void showUpgradeDialogToSystem(Activity activity, KbAppObject kbAppObject) {
        final StringBuffer desc = new StringBuffer(
                Html.fromHtml(kbAppObject.getVersionDescription()))
                .append("\n版本：" + kbAppObject.getVersionName())
                .append("\n大小：" + kbAppObject.getFileSize() / 1024 + "KB")
                .append("\n发布时间：" +
                        new SimpleDateFormat("yyyy年MM月dd日 HH:mm")
                                .format(kbAppObject.getModifiedTime() * 1000));

        new AlertDialog.Builder(activity).setTitle("检查升级")
                .setMessage(desc)
                .setPositiveButton("升级", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        UpgradeModule.getInstance().installFile();
                    }
                })
                .setNegativeButton("取消", null).show();
    }

    private static void showUpgradeDialogToElderlyAssistant(Activity activity, final KbAppObject kbAppObject) {
        final AlertDialog dialog = new AlertDialog.Builder(activity).create();
        Window window = dialog.getWindow();
        dialog.show();

        window.clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        window.setContentView(com.jiuwei.upgrade_package_new.lib.R.layout.dlg_upgrade_elderly_assistant);

        TextView newVersionContext = (TextView) window.findViewById(R.id.tv_1_upgrade_version_context);
        TextView upgradeContent = (TextView) window.findViewById(R.id.tv_1_upgrade_message);
        Button ok = (Button) window.findViewById(R.id.btn_dlg_set_time_custom_ok);
        Button cancel = (Button) window.findViewById(R.id.btn_dlg_set_time_custom_cancel);

        final StringBuffer desc = new StringBuffer(
                "版本：v" + kbAppObject.getVersionName())
                .append("\n大小：" + kbAppObject.getFileSize() / 1024 + "KB")
                .append("\n发布时间：" +
                        new SimpleDateFormat("yyyy年MM月dd日 HH:mm")
                                .format(kbAppObject.getModifiedTime() * 1000));
        newVersionContext.setText(desc);
        upgradeContent.setText(Html.fromHtml(kbAppObject.getVersionDescription()));
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (UpgradeModule.getInstance().getFileIsAlreadyExists(kbAppObject.getVersionCode())) {
                    UpgradeModule.getInstance().installFile();
                } else {
                    download(kbAppObject);
                }
                dialog.dismiss();
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
    }


    /**
     * 获取网络状态，wifi,wap,2g,3g.
     *
     * @param context 上下文
     * @return int 网络状态
     */
//    {@link #                            com.jiuwei.upgrade_package_new.lib.Constant .NETWORKTYPE_2G},
//    {@link #NETWORKTYPE_3G},
//    {@link #NETWORKTYPE_INVALID},
//    {@link #NETWORKTYPE_WAP}* <p>
//    {@link #                            com.jiuwei.upgrade_package_new.lib.Constant .NETWORKTYPE_WIFI}
    public static int getNetWorkType(Context context) {
        int mNetWorkType = 0;
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            String type = networkInfo.getTypeName();
            if (type.equalsIgnoreCase("WIFI")) {
                mNetWorkType = com.jiuwei.upgrade_package_new.lib.Constant.NETWORKTYPE_WIFI;
            } else if (type.equalsIgnoreCase("MOBILE")) {
                String proxyHost = android.net.Proxy.getDefaultHost();
                mNetWorkType = TextUtils.isEmpty(proxyHost)
                        ? (isFastMobileNetwork(context) ? com.jiuwei.upgrade_package_new.lib.Constant.NETWORKTYPE_3G : com.jiuwei.upgrade_package_new.lib.Constant.NETWORKTYPE_2G)
                        : com.jiuwei.upgrade_package_new.lib.Constant.NETWORKTYPE_WAP;
            }
        } else {
            mNetWorkType = com.jiuwei.upgrade_package_new.lib.Constant.NETWORKTYPE_INVALID;
        }
        return mNetWorkType;
    }

    private static boolean isFastMobileNetwork(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        switch (telephonyManager.getNetworkType()) {
            case TelephonyManager.NETWORK_TYPE_1xRTT:
                return false; // ~ 50-100 kbps
            case TelephonyManager.NETWORK_TYPE_CDMA:
                return false; // ~ 14-64 kbps
            case TelephonyManager.NETWORK_TYPE_EDGE:
                return false; // ~ 50-100 kbps
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
                return true; // ~ 400-1000 kbps
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
                return true; // ~ 600-1400 kbps
            case TelephonyManager.NETWORK_TYPE_GPRS:
                return false; // ~ 100 kbps
            case TelephonyManager.NETWORK_TYPE_HSDPA:
                return true; // ~ 2-14 Mbps
            case TelephonyManager.NETWORK_TYPE_HSPA:
                return true; // ~ 700-1700 kbps
            case TelephonyManager.NETWORK_TYPE_HSUPA:
                return true; // ~ 1-23 Mbps
            case TelephonyManager.NETWORK_TYPE_UMTS:
                return true; // ~ 400-7000 kbps
            case TelephonyManager.NETWORK_TYPE_EHRPD:
                return true; // ~ 1-2 Mbps
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
                return true; // ~ 5 Mbps
            case TelephonyManager.NETWORK_TYPE_HSPAP:
                return true; // ~ 10-20 Mbps
            case TelephonyManager.NETWORK_TYPE_IDEN:
                return false; // ~25 kbps
            case TelephonyManager.NETWORK_TYPE_LTE:
                return true; // ~ 10+ Mbps
            case TelephonyManager.NETWORK_TYPE_UNKNOWN:
                return false;
            default:
                return false;
        }
    }
}
