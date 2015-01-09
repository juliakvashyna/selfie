package com.bigdropinc.selfieking;

import io.fabric.sdk.android.Fabric;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Typeface;
import android.util.Base64;
import android.util.Log;

import com.bigdrop.selfieking.db.DatabaseManager;
import com.bigdropinc.selfieking.controller.CustomPicasso;
import com.bigdropinc.selfieking.controller.HttpClientHelper;
import com.bigdropinc.selfieking.controller.InternetChecker;
import com.bigdropinc.selfieking.controller.SharedPreferenceKeys;
import com.bigdropinc.selfieking.controller.managers.FileManager;
import com.bigdropinc.selfieking.controller.managers.login.LoginManagerImpl;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;

public class MyApplication extends Application {
    // Note: Your consumer key and secret should be obfuscated in your source code before shipping.
    private static final String TWITTER_KEY = "6PUZMawzdCNwQ2zbuIEvw5Mly";
    private static final String TWITTER_SECRET = "MUchDymINUXFS37w7Lthyb1slyA297KhUs5uh0I7rHnLTtPdAO";
    
    private static final String FONT = "fonts/";
    private static final String DEFAULT_BOLD_FONT_FILENAME = FONT + "Mark Simonson - Proxima Nova Bold.otf";
    private static final String DEFAULT_ITALIC_FONT_FILENAME = FONT + "Mark Simonson - Proxima Nova Light Italic.otf";
    private static final String DEFAULT_BOLD_ITALIC_FONT_FILENAME = FONT + "Mark Simonson - Proxima Nova Semibold Italic.otf";
    private static final String DEFAULT_NORMAL_FONT_FILENAME = FONT + "Mark Simonson - Proxima Nova Light.otf";
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
        // Settings.Global.putInt(getApplicationContext().getContentResolver(),
        // Global.AIRPLANE_MODE_ON, 1);
        setDefaultFont();
        super.onCreate();
        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
        Fabric.with(this, new Twitter(authConfig));
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

    private void setDefaultFont() {
        try {
            final Typeface bold = Typeface.createFromAsset(getAssets(), DEFAULT_BOLD_FONT_FILENAME);
            final Typeface italic = Typeface.createFromAsset(getAssets(), DEFAULT_ITALIC_FONT_FILENAME);
            final Typeface boldItalic = Typeface.createFromAsset(getAssets(), DEFAULT_BOLD_ITALIC_FONT_FILENAME);
            final Typeface regular = Typeface.createFromAsset(getAssets(), DEFAULT_NORMAL_FONT_FILENAME);

            Field DEFAULT = Typeface.class.getDeclaredField("DEFAULT");
            DEFAULT.setAccessible(true);
            DEFAULT.set(null, regular);

            Field DEFAULT_BOLD = Typeface.class.getDeclaredField("DEFAULT_BOLD");
            DEFAULT_BOLD.setAccessible(true);
            DEFAULT_BOLD.set(null, bold);

     

            Field sDefaults = Typeface.class.getDeclaredField("sDefaults");
            sDefaults.setAccessible(true);
            sDefaults.set(null, new Typeface[] { regular, bold, italic, boldItalic });

        } catch (NoSuchFieldException e) {
            logFontError(e);
        } catch (IllegalAccessException e) {
            logFontError(e);
        } catch (Throwable e) {
            // cannot crash app if there is a failure with overriding the
            // default font!
            logFontError(e);
        }
    }

    private void logFontError(Throwable e) {
        Log.d("tag", "font error");

    }
}
