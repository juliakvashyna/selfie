package com.bigdropinc.selfieking.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import android.util.Log;

import com.bigdropinc.selfieking.controller.loaders.ApiException;
import com.bigdropinc.selfieking.controller.managers.login.LoginManagerImpl;
import com.bigdropinc.selfieking.model.Password;
import com.bigdropinc.selfieking.model.User;
import com.bigdropinc.selfieking.model.responce.ResponceError;
import com.bigdropinc.selfieking.model.responce.StatusCode;
import com.bigdropinc.selfieking.model.selfie.Comment;
import com.bigdropinc.selfieking.model.selfie.CommentSelfieImage;
import com.bigdropinc.selfieking.model.selfie.Contest;
import com.bigdropinc.selfieking.model.selfie.Like;
import com.bigdropinc.selfieking.model.selfie.SelfieImage;

public class HttpClientHelper {

    private static final String ALL = "all";
    public static final String EMAIL = "email";
    public static final String USER_ID = "user_id";
    public static final String PASSWORD = "password";
    public static final String USER_NAME = "username";
    public static final String PROFESSION = "profession";
    public static final String PHONE = "phone ";
    public static final String WEBSITE = "website ";
    public static final String GENDER = "gender ";
    public static final String TOKEN = "token";
    public static final String FIELDS = "fields";
    public static final String WITH = "with";
    public static final String OFFSET = "offset";
    public static final String LIMIT = "limit";
    public static final String EXTENSION = "extension";
    public static final String IMAGE = "image";
    public static final String DATA = "data";
    public static final String COMMENT = "comment";
    public static final String POST_ID = "postId";
    public static final String DESCRIPTION = "description";
    public static final String CURRENT_PASSWORD = "currentPassword";
    public static final String NEW_PASSWORD = "newPassword";
    public static final String YEAR = "year";
    public static final String MONTH = "month";
    public static final String IMAGEEXT = "imageExt";
    public static final String PNG = "png";
    private static final String TAG = "tag";
    private JsonHelper jsonHelper = new JsonHelper();
    private String token = LoginManagerImpl.getInstance().getToken();
    private static SharedPreferences sharedpreferences;

    public static void init(Context context) {
        sharedpreferences = context.getSharedPreferences(SharedPreferenceKeys.MyPREFERENCES, Context.MODE_PRIVATE);
    }

    public User getUser(String token) throws ApiException {
        List<NameValuePair> params = new ArrayList<NameValuePair>(1);
        params.add(new BasicNameValuePair(TOKEN, token));
        InputStream inputStream = postData(UrlRequest.GET_USER, params);
        String content = convertStreamToString(inputStream);
        Log.d(TAG, "responce from server  " + content);
        User user = jsonHelper.parseUser(content);
        return user;
    }

    public User getUser(String email, String password) throws ApiException {
        ArrayList<NameValuePair> params = new ArrayList<NameValuePair>(5);
        params.add(new BasicNameValuePair(EMAIL, email));
        params.add(new BasicNameValuePair(PASSWORD, password));
        InputStream inputStream = postData(UrlRequest.LOGIN, params);
        String content = convertStreamToString(inputStream);
        Log.d(TAG, "responce from server  " + content);
        User user = jsonHelper.parseUser(content);
        token = user.getToken();
        return user;
    }

    public User registr(User user) throws ApiException {
        List<NameValuePair> params = new ArrayList<NameValuePair>(5);
        params.add(new BasicNameValuePair(USER_NAME, user.getUserName()));
        params.add(new BasicNameValuePair(EMAIL, user.getEmail()));
        params.add(new BasicNameValuePair(PASSWORD, user.getPassword()));
        InputStream inputStream = postData(UrlRequest.REGISTR_USER, params);
        String content = convertStreamToString(inputStream);
        Log.d(TAG, "responce from server  " + content);
        StatusCode code = jsonHelper.parseMessage(content);
        if (code.isSuccess()) {
            user = getUser(user.getEmail(), user.getPassword());
        }
        return user;
    }

    public StatusCode resetPassword(User user) throws ApiException {
        List<NameValuePair> params = new ArrayList<NameValuePair>(5);
        params.add(new BasicNameValuePair(EMAIL, user.getEmail()));
        InputStream inputStream = postData(UrlRequest.RESET_PASSWORD, params);
        String responce = convertStreamToString(inputStream);
        Log.d(TAG, "responce from server  " + responce);
        StatusCode str = jsonHelper.parseMessage(responce);
        return str;
    }

    public User editProfile(User user) throws ApiException {
        List<NameValuePair> params = new ArrayList<NameValuePair>(5);
        params.add(new BasicNameValuePair(EMAIL, user.getEmail()));
        params.add(new BasicNameValuePair(USER_NAME, user.getUserName()));
        params.add(new BasicNameValuePair(PROFESSION, user.getJob()));
        params.add(new BasicNameValuePair(PHONE, user.getPhone()));
        params.add(new BasicNameValuePair(GENDER, String.valueOf(user.getGender())));
        params.add(new BasicNameValuePair(TOKEN, user.getToken()));
        InputStream inputStream = postData(UrlRequest.EDIT_PROFILE, params);
        String responce = convertStreamToString(inputStream);
        Log.d(TAG, "responce from server  " + responce);
        StatusCode str = jsonHelper.parseMessage(responce);
        return getUser(user.getToken());
    } 

    public StatusCode postSelfie(SelfieImage selfieImage) throws ApiException {
        List<NameValuePair> params = new ArrayList<NameValuePair>(5);
        params.add(new BasicNameValuePair(TOKEN, selfieImage.getToken()));
        params.add(new BasicNameValuePair(DESCRIPTION, selfieImage.getDescription()));
        params.add(new BasicNameValuePair(IMAGEEXT, PNG));
        params.add(new BasicNameValuePair(IMAGE, Base64.encodeToString(selfieImage.getBytesImage(), 0)));
        InputStream inputStream = postData(UrlRequest.POST_SELFIE, params);
        String content = convertStreamToString(inputStream);
        StatusCode statusCode = jsonHelper.parseMessage(content);
        return statusCode;
    }

    public SelfieImage commentSelfie(Comment comment) throws ApiException {
        List<NameValuePair> params = new ArrayList<NameValuePair>(5);
        params.add(new BasicNameValuePair(TOKEN, token));
        params.add(new BasicNameValuePair(COMMENT, comment.getText()));
        params.add(new BasicNameValuePair(POST_ID, String.valueOf(comment.getPostId())));
        InputStream inputStream = postData(UrlRequest.ADD_COMMENT, params);
        String responce = convertStreamToString(inputStream);
        Log.d(TAG, "responce from server  " + responce);
        SelfieImage image = new SelfieImage();
        image.setToken(token);
        image.setId(comment.getPostId());
        image = getSelfie(image);
        return image;
    }

    public SelfieImage likeSelfie(Like like, String url) throws ApiException {
        List<NameValuePair> params = new ArrayList<NameValuePair>(5);
        params.add(new BasicNameValuePair(TOKEN, like.getToken()));
        params.add(new BasicNameValuePair(POST_ID, String.valueOf(like.getPostId())));
        InputStream inputStream = postData(url, params);
        String content = convertStreamToString(inputStream);
        StatusCode code = jsonHelper.parseMessage(content);
        SelfieImage image = new SelfieImage();
        image.setToken(like.getToken());
        image.setId(like.getPostId());
        image = getSelfie(image);
        if (!code.isSuccess()) {
            Log.d("like", "likeSelfie  " + code.getError().get(0).errorMessage);
        }
        return image;
    }

    public List<SelfieImage> getSelfies(int offset) throws ApiException {
        return getSelfies(UrlRequest.GET_SELFIES, offset);
    }

    public List<SelfieImage> getLikedSelfies(int offset) throws ApiException {
        return getSelfies(UrlRequest.GET_LIKED, offset);
    }

    public StatusCode deleteSelfie(SelfieImage selfieImage) throws ApiException {
        List<NameValuePair> params = new ArrayList<NameValuePair>(5);
        params.add(new BasicNameValuePair(TOKEN, token));
        if (selfieImage == null) {
            params.add(new BasicNameValuePair(POST_ID, ALL));
        } else {
            params.add(new BasicNameValuePair(POST_ID, String.valueOf(selfieImage.getId())));
        }
        InputStream inputStream = postData(UrlRequest.DELETE_SELFIE, params);
        String content = convertStreamToString(inputStream);
        Log.d(TAG, "responce from server  " + content);
        StatusCode statusCode = jsonHelper.parseMessage(content);
        Log.d(TAG, "status code from server  " + statusCode.getCode());
        return statusCode;
    }

    public SelfieImage getSelfie(SelfieImage selfieImage) throws ApiException {
        List<NameValuePair> params = new ArrayList<NameValuePair>(5);
        params.add(new BasicNameValuePair(TOKEN, String.valueOf(selfieImage.getToken())));
        params.add(new BasicNameValuePair(POST_ID, String.valueOf(selfieImage.getId())));
        params.add(new BasicNameValuePair(FIELDS, "basic, extended, imageSmall, imageMedium"));
        InputStream inputStream = postData(UrlRequest.GET_POST, params);
        String content = convertStreamToString(inputStream);
        SelfieImage statusCode = jsonHelper.parseSelfie(content);

        return statusCode;
    }

    public CommentSelfieImage getSelfieWithComments(SelfieImage selfieImage) throws ApiException {
        List<NameValuePair> params = new ArrayList<NameValuePair>(5);
        params.add(new BasicNameValuePair(TOKEN, token));
        params.add(new BasicNameValuePair(POST_ID, String.valueOf(selfieImage.getId())));
        params.add(new BasicNameValuePair(FIELDS, "basic, extended, imageSmall, imageMedium"));
        params.add(new BasicNameValuePair(WITH, "comments"));
        InputStream inputStream = postData(UrlRequest.GET_POST, params);
        String content = convertStreamToString(inputStream);
        CommentSelfieImage selfie = jsonHelper.parseCommentSelfie(content);
        return selfie;
    }

    public StatusCode changePassword(Password password) throws ApiException {
        List<NameValuePair> params = new ArrayList<NameValuePair>(5);
        params.add(new BasicNameValuePair(TOKEN, token));
        params.add(new BasicNameValuePair(CURRENT_PASSWORD, password.getOld()));
        params.add(new BasicNameValuePair(NEW_PASSWORD, password.getNewPassword()));
        InputStream inputStream = postData(UrlRequest.CHANGE_PASSWORD, params);
        String content = convertStreamToString(inputStream);
        Log.d(TAG, "responce from server  " + content);
        StatusCode statusCode = jsonHelper.parseMessage(content);
        Log.d(TAG, "status code from server  " + statusCode.getCode());
        if (statusCode.isSuccess()) {
            String tokenString = statusCode.getToken();
            LoginManagerImpl.getInstance().setToken(tokenString);
        }
        return statusCode;
    }

    public StatusCode deleteAccount() throws ApiException {
        List<NameValuePair> params = new ArrayList<NameValuePair>(5);
        params.add(new BasicNameValuePair(TOKEN, token));
        InputStream inputStream = postData(UrlRequest.DELETE_ACCOUNT, params);
        String content = convertStreamToString(inputStream);
        Log.d(TAG, "responce from server  " + content);
        StatusCode statusCode = jsonHelper.parseMessage(content);
        Log.d(TAG, "status code from server  " + statusCode.getCode());
        return statusCode;
    }

    public StatusCode contest(SelfieImage selfieImage) throws ApiException {
        List<NameValuePair> params = new ArrayList<NameValuePair>(5);
        params.add(new BasicNameValuePair(TOKEN, token));
        params.add(new BasicNameValuePair(POST_ID, String.valueOf(selfieImage.getId())));
        InputStream inputStream = postData(UrlRequest.ADD_CONTEST, params);
        String content = convertStreamToString(inputStream);
        StatusCode statusCode = jsonHelper.parseMessage(content);
        return statusCode;
    }

    public List<SelfieImage> getContest(Contest contest) throws ApiException {
        List<NameValuePair> params = new ArrayList<NameValuePair>(5);
        params.add(new BasicNameValuePair(TOKEN, token));
        params.add(new BasicNameValuePair(YEAR, contest.getYear()));
        params.add(new BasicNameValuePair(MONTH, String.valueOf(contest.getMonth())));
        InputStream inputStream = postData(UrlRequest.GET_CONTEST, params);
        String content = convertStreamToString(inputStream);
        List<SelfieImage> list = jsonHelper.parseSelfies(content);
        return list;
    }

    private List<SelfieImage> getSelfies(String command, int offset) throws ApiException {
        List<NameValuePair> params = new ArrayList<NameValuePair>(5);
        params.add(new BasicNameValuePair(TOKEN, token));
        params.add(new BasicNameValuePair(FIELDS, "basic, extended, imageSmall, imageMedium"));
        params.add(new BasicNameValuePair(OFFSET, String.valueOf(offset)));
        params.add(new BasicNameValuePair(LIMIT, String.valueOf(5)));
        InputStream inputStream = postData(command, params);
        String content = convertStreamToString(inputStream);
        List<SelfieImage> list = jsonHelper.parseSelfies(content);
        return list;

    }

    private JSONObject getSelfieJSONImage(SelfieImage selfieImage) {
        JSONObject jo = new JSONObject();
        try {
            jo.put(DATA, Base64.encodeToString(selfieImage.getBytesImage(), 0));
            jo.put(EXTENSION, "jpeg");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jo;
    }

    private JSONObject getSelfieJSON(SelfieImage selfieImage) {
        JSONObject jo = new JSONObject();
        JSONObject jo1 = new JSONObject();

        try {
            jo.put(DATA, Base64.encodeToString(selfieImage.getBytesImage(), 0));
            jo.put(EXTENSION, "jpeg");
            jo1.put(IMAGE, jo);
            jo1.put(TOKEN, String.valueOf(selfieImage.getToken()));
            jo1.put(DESCRIPTION, selfieImage.getDescription());

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jo1;
    }

    private InputStream postData(String url, List<? extends NameValuePair> nameValuePairs) throws ApiException {
        InputStream inputStream = null;
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(url);
        httppost = addHeader(httppost);
        try {
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(nameValuePairs, HTTP.UTF_8);
            httppost.setEntity(entity);
            HttpResponse response = httpclient.execute(httppost);
            inputStream = response.getEntity().getContent();
            return inputStream;
        } catch (ClientProtocolException e) {
            e.printStackTrace();
            List<ResponceError> error = new ArrayList<ResponceError>();
            error.add(new ResponceError("Too slow internet connection", 1));
            throw new ApiException("Failure", error);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return inputStream;
    }

    private HttpPost addHeader(HttpPost httppost) {

        String name = "Authorization";

        httppost.addHeader(name, sharedpreferences.getString(SharedPreferenceKeys.APIKEY, ""));
        return httppost;
    }

    private InputStream postJSON(String url, JSONObject jsonObject) {
        InputStream inputStream = null;
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(url);
        httppost.setHeader("Content-type", "application/json");
        httppost.setHeader("Accept", "application/json");
        try {
            StringEntity entity = new StringEntity(jsonObject.toString(), HTTP.UTF_8);
            entity.setContentType("application/json;charset=UTF-8");
            httppost.setEntity(entity);
            HttpResponse response = httpclient.execute(httppost);
            StatusLine statusLine = response.getStatusLine();
            Log.d(TAG, "Server responded with status code: " + statusLine.getStatusCode());
            inputStream = response.getEntity().getContent();
            return inputStream;
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return inputStream;
    }

    private static String convertStreamToString(InputStream is) {
        if (is != null) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            String line = null;
            try {
                while ((line = reader.readLine()) != null) {
                    sb.append((line + "\n"));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return sb.toString();
        } else {
            Log.d("tag", "InputStream is null");
            return "";
        }
    }

    private InputStream getData(String url) {
        HttpClient httpclient = new DefaultHttpClient();
        HttpGet httpget = new HttpGet(url);
        httpget.setHeader("Content-type", "application/json");
        try {
            HttpResponse response = httpclient.execute(httpget);
            InputStream inputStream = response.getEntity().getContent();
            String testjson = EntityUtils.toString(response.getEntity(), HTTP.UTF_8);
            Log.d(TAG, "json string = " + testjson);
            StatusLine statusLine = response.getStatusLine();
            if (statusLine.getStatusCode() == 200) {
                return inputStream;
            } else {
                Log.e(TAG, "Server responded with status code: " + statusLine.getStatusCode());
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
