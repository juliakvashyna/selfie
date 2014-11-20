package com.bigdropinc.selfieking.controller;

import java.io.IOException;
import java.net.HttpURLConnection;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;

import com.squareup.picasso.LruCache;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.UrlConnectionDownloader;

public class CustomPicasso {
    private static final int MAX_STALE = 30 * 24 * 60 * 60; // 30 days
    protected static SharedPreferences sharedpreferences;
    static String value;
    static Picasso picasso;

    public static void init(Context context) {
        sharedpreferences = context.getSharedPreferences(SharedPreferenceKeys.MyPREFERENCES, Context.MODE_PRIVATE);
        value = sharedpreferences.getString(SharedPreferenceKeys.APIKEY, "");
        Picasso.Builder builder = new Picasso.Builder(context);
        builder.downloader(new UrlConnectionDownloader(context) {
            @Override
            protected HttpURLConnection openConnection(Uri uri) throws IOException {
                HttpURLConnection connection = super.openConnection(uri);
                connection.setRequestProperty("Authorization", value);
                connection.setRequestProperty("Cache-Control", "max-stale=" + MAX_STALE);
                return connection;
            }
        });
        
        
        LruCache c = new LruCache(12 * 1024 * 1024);
        builder.memoryCache(c);
        picasso = builder.build();

    }

    public static Picasso getImageLoader(final Context ctx) {
        return picasso;

    }
}
