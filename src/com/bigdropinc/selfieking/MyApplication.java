package com.bigdropinc.selfieking;

import java.io.UnsupportedEncodingException;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.provider.Settings;
import android.provider.Settings.Global;
import android.util.Base64;

import com.bigdrop.selfieking.db.DatabaseManager;
import com.bigdropinc.selfieking.controller.CustomPicasso;
import com.bigdropinc.selfieking.controller.HttpClientHelper;
import com.bigdropinc.selfieking.controller.InternetChecker;
import com.bigdropinc.selfieking.controller.SharedPreferenceKeys;
import com.bigdropinc.selfieking.controller.managers.FileManager;
import com.bigdropinc.selfieking.controller.managers.login.LoginManagerImpl;

public class MyApplication extends Application {

    private SharedPreferences sharedpreferences;
    private Editor editor;

    @SuppressLint("NewApi")
	@Override
    public void onCreate() {
        DatabaseManager.init(this);
        LoginManagerImpl.init(getApplicationContext());
        FileManager.init(getApplicationContext());
        InternetChecker.init(getApplicationContext());
        CustomPicasso.init(getApplicationContext());
        HttpClientHelper.init(getApplicationContext());
        initAuthorization();
      //  Settings.Global.putInt(getApplicationContext().getContentResolver(), Global.AIRPLANE_MODE_ON,  1);
        super.onCreate();
    }

    private void initAuthorization() {
        sharedpreferences = getSharedPreferences(SharedPreferenceKeys.MyPREFERENCES, Context.MODE_PRIVATE);
        editor = sharedpreferences.edit();
        editor.putString(SharedPreferenceKeys.APIKEY, getKey());
        editor.commit();
    }

    private String getKey() {
        byte[] bytes = null;
        String user = "bigdrop";
        String password = "Bigdrop01";
        String separator = ":";
        String key = user + separator + password;
        try {
            bytes = key.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }
        String cred = Base64.encodeToString(bytes, Base64.URL_SAFE | Base64.NO_WRAP);
        return "Basic " + cred;
    }

}
