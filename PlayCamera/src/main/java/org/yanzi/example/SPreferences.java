package org.yanzi.example;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by goyourfly on 14-5-23.
 */
public class SPreferences {
    public static String sPName = "com.chenww.camera.ui";

    public static void putStringAppend(Context context,String key,String value){
        SharedPreferences sharedPreferences = context.getSharedPreferences(sPName, Context.MODE_APPEND);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key,value);
        editor.commit();
    }

    public static void putString(Context context,String key,String value){
        SharedPreferences sharedPreferences = context.getSharedPreferences(sPName,0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key,value);
        editor.commit();
    }

    public static boolean putStr(Context context,String key,String value){
        SharedPreferences sharedPreferences = context.getSharedPreferences(sPName,0);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        return editor.putString(key,value).commit();
    }

    public static String getString(Context context,String key,String defValue){
        SharedPreferences sharedPreferences = context.getSharedPreferences(sPName,0);
        return sharedPreferences.getString(key,defValue);
    }

    public static void clear(Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences(sPName,0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.commit();
    }

    public static void remove(Context context,String key){
        SharedPreferences sharedPreferences = context.getSharedPreferences(sPName,0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(key);
        editor.commit();
    }

}