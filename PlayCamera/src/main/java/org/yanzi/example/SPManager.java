package org.yanzi.example;

import android.content.Context;

/**
 * Created by zhangcirui on 15/8/24.
 */
public class SPManager {

    public static final String MODE = "MODE";


    private static SPManager spMasterManager;

    private static Context mContext;

    public  void init(Context context) {
        mContext = context;
    }

    public static SPManager getInstance(){
        if(spMasterManager == null){
            spMasterManager = new SPManager();
        }
        return spMasterManager;
    }


    public String getMode() {

        return SPreferences.getString(mContext,MODE, null);
    }

    public boolean setMode(String info) {

        return SPreferences.putStr(mContext, MODE, info);
    }




}
