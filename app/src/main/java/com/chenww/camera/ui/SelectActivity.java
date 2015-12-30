package com.chenww.camera.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;

import com.chenww.camera.ui.db.SPManager;
import com.jiuwei.upgrade_package_new.lib.UpgradeModule;

public class SelectActivity extends Activity implements View.OnClickListener {

    private Button mBeiJingQian, mNanJingQian, mBeiJingHou, mNanJingHou;

    private String mode_1 = "mode_1",mode_2 = "mode_2",mode_3 = "mode_3",mode_4 = "mode_4";
    //SharedPreferences sharedPreferences;
    private String mode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select);

        mBeiJingQian = (Button) findViewById(R.id.tv_beijing_qian);
        mNanJingQian = (Button) findViewById(R.id.tv_nanjing_qian);
        mBeiJingHou = (Button) findViewById(R.id.tv_beijing_hou);
        mNanJingHou = (Button) findViewById(R.id.tv_nanjing_hou);

        mBeiJingQian.setOnClickListener(this);
        mNanJingQian.setOnClickListener(this);
        mBeiJingHou.setOnClickListener(this);
        mNanJingHou.setOnClickListener(this);

        mode = SPManager.getInstance().getMode();
        //ToastUtil.showToast(getApplicationContext(),mode+"");

        if(!TextUtils.isEmpty(mode)){
            Intent intent = new Intent(getApplicationContext(), CameraActivity.class);
            if(mode.equals(mode_1)){
                intent.putExtra("downUrl", "http://115.28.43.225:8080/download/MonitorPic_a.png");
                intent.putExtra("uploadSign", "b");
                intent.putExtra("isQian", true);
                startActivity(intent);
            }else if(mode.equals(mode_2)){
                intent.putExtra("downUrl", "http://115.28.43.225:8080/download/MonitorPic_b.png");
                intent.putExtra("uploadSign", "a");
                intent.putExtra("isQian", true);
                startActivity(intent);
            }else if(mode.equals(mode_3)){
                intent.putExtra("downUrl", "http://115.28.43.225:8080/download/MonitorPic_a.png");
                intent.putExtra("uploadSign", "b");
                intent.putExtra("isQian", false);
                startActivity(intent);
            }else if(mode.equals(mode_4)){
                intent.putExtra("downUrl", "http://115.28.43.225:8080/download/MonitorPic_b.png");
                intent.putExtra("uploadSign", "a");
                intent.putExtra("isQian", false);
                startActivity(intent);
            }
            finish();
        }

        UpgradeModule.init(this);
        UpgradeHelper.checkUpgrade(
                this,
                true,
                com.jiuwei.upgrade_package_new.lib.Constant.DIALOG_STYLE_ELDERLY_ASSISTANT);
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(getApplicationContext(), CameraActivity.class);
        switch (v.getId()) {

            case R.id.tv_beijing_qian:
                intent.putExtra("downUrl", "http://115.28.43.225:8080/download/MonitorPic_a.png");
                intent.putExtra("uploadSign", "b");
                intent.putExtra("isQian", true);
                SPManager.getInstance().setMode(mode_1);
                break;

            case R.id.tv_nanjing_qian:
                intent.putExtra("downUrl", "http://115.28.43.225:8080/download/MonitorPic_b.png");
                intent.putExtra("uploadSign", "a");
                intent.putExtra("isQian", true);
                SPManager.getInstance().setMode(mode_2);
                break;

            case R.id.tv_beijing_hou:
                intent.putExtra("downUrl", "http://115.28.43.225:8080/download/MonitorPic_a.png");
                intent.putExtra("uploadSign", "b");
                intent.putExtra("isQian", false);
                SPManager.getInstance().setMode(mode_3);
                break;

            case R.id.tv_nanjing_hou:
                intent.putExtra("downUrl", "http://115.28.43.225:8080/download/MonitorPic_b.png");
                intent.putExtra("uploadSign", "a");
                intent.putExtra("isQian", false);
                SPManager.getInstance().setMode(mode_4);
                break;
        }
        startActivity(intent);
        finish();

    }
}
