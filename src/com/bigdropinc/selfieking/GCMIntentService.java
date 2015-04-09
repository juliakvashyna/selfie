package com.bigdropinc.selfieking;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.bigdropinc.selfieking.activities.login.LogoActivity;
import com.bigdropinc.selfieking.activities.social.MyActionBarActivity;
import com.bigdropinc.selfieking.activities.social.OneSelfieActivity;
import com.bigdropinc.selfieking.controller.CustomPicasso;
import com.bigdropinc.selfieking.controller.UrlRequest;
import com.google.android.gcm.GCMBaseIntentService;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

public class GCMIntentService extends GCMBaseIntentService {

    private String TAG = "gcm";

    public GCMIntentService() {
        super(GCMHelper.SENDER_ID);
    }

    @Override
    protected void onRegistered(Context context, String registrationId) {
        Log.i(TAG, "Device registered");

    }

    @Override
    protected void onUnregistered(Context context, String registrationId) {
        Log.i(TAG, "Device unregistered");
    }

    String nActor = "n_actor";
    String nId = "id";
    String nName = "name";
    String nAvatar = "avatar";
    String nObject = "n_object";
    String nImage = "image";
    String nAuthor = "author";
    String nComment = "comment";
    String nText = "text";
    String nDate = "date";
    String nType = "n_type";

    @Override
    protected void onMessage(Context context, Intent intent) {
        Log.i("gcm1", "Received new message");
        String message = "New Message!";
        String type;
        String obj;
        String actor;

        Bundle extras = intent.getExtras();
        type = extras.getString(nType);
        obj = extras.getString(nObject);

        actor = extras.getString(nActor);
        if (type.equals(nComment)) {
            message = parseComment(obj, actor);
        } else if (type.equals("rating"))
            message = parseRating(actor);

        int selfieId = 0;
        String image = null;
        try {
            JSONObject readerObject = new JSONObject(obj);
            selfieId = readerObject.getInt(nId);
            image = readerObject.getString(nImage);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        generateNotification(context, message, selfieId, image);
    }

    private String parseComment(String obj, String author) {
        String text = "";
        String name = "";
        try {
            JSONObject readerObject = new JSONObject(obj);
            JSONObject readerAuthor = new JSONObject(author);
            JSONObject com = readerObject.getJSONObject(nComment);
            text = com.getString(nText);
            name = readerAuthor.getString(nName);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return name + " wrote comment " + text;
    }

    private String parseRating(String author) {
        String name = "";
        try {
            JSONObject readerAuthor = new JSONObject(author);
            name = readerAuthor.getString(nName);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return name + " votes your Selfie!";
    }

    @Override
    protected void onDeletedMessages(Context context, int total) {
        Log.i(TAG, "Received deleted messages notification");
    }

    @Override
    public void onError(Context context, String errorId) {
        Log.i(TAG, "Received error: " + errorId);
    }

    @Override
    protected boolean onRecoverableError(Context context, String errorId) {
        Log.i(TAG, "Received recoverable error: " + errorId);
        return super.onRecoverableError(context, errorId);
    }

    private static void generateNotification(Context context, String message, int id, String image) {
        Bitmap bitmap = null;
        try {
            bitmap = CustomPicasso.getImageLoader(context).load(UrlRequest.ADDRESS + image).resize(200, 200).get();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context).setSmallIcon(R.drawable.ic_launcher).setContentTitle("Message").setContentText(message).setAutoCancel(true).setLargeIcon(bitmap);
        Intent resultIntent = new Intent(context, MyActionBarActivity.class);
        resultIntent.putExtra(OneSelfieActivity.INTENT_SELFIE_ID, id);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(LogoActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(0, mBuilder.build());
    }
}
