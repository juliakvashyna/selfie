package com.bigdropinc.selfieking.controller;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

public class InternetChecker {
    private static Context context;

    public static void init(Context context) {
        InternetChecker.context = context;
    }

    public static boolean isNetworkConnected() {
        NetworkInfo info = (NetworkInfo) ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        if (info == null || !info.isConnected()) {
            return false;
        }
        if (info.isRoaming()) {
            // here is the roaming option you can change it if you want to
            // disable internet while roaming, just return false
            return false;
        }
        return true;

    }

    public static void showNotInternetError(final Activity activity) {
        Toast.makeText(activity, "No internet connection", Toast.LENGTH_SHORT).show();
    }

}
