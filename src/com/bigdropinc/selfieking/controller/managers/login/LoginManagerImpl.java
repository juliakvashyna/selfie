package com.bigdropinc.selfieking.controller.managers.login;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.bigdropinc.selfieking.model.User;

public final class LoginManagerImpl implements LoginManager {

    public static final String MyPREFERENCES = "MyPrefs";
    public static final String email = "emailKey";
    public static final String pass = "passwordKey";
    public static final String idKey = "idKey";
    public static final String TOKEN = "token";
    private SharedPreferences sharedpreferences;
    private static Context context;
    private static LoginManager loginManager;
    private Editor editor;

    private LoginManagerImpl() {
        super();
        sharedpreferences = context.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        editor = sharedpreferences.edit();
    }

    public static void init(Context context) {
        LoginManagerImpl.context = context;
    }

    public static synchronized LoginManager getInstance() {
        if (loginManager == null) {
            loginManager = new LoginManagerImpl();
        }
        return loginManager;
    }

    private void sign(int id, String t) {
        editor.putInt(idKey, id);
        editor.putString(TOKEN, t);
        editor.commit();
    }

    @Override
    public void signOut() {
        editor.remove(TOKEN);
        editor.remove(idKey);
        editor.commit();
        // moveTaskToBack(true);
        // Welcome.this.finish();
    }

    @Override
    public boolean check() {

        String token = sharedpreferences.getString(TOKEN, "");
        int id = sharedpreferences.getInt(idKey, -1);
        return !token.isEmpty() && id != -1;
    }

    @Override
    public void signIn(int id, String token) {
        sign(id, token);
    }

    public User getUser() {
        if (check()) {
            User user = new User(sharedpreferences.getString(email, ""), sharedpreferences.getString(pass, ""));
            user.setToken(sharedpreferences.getString(TOKEN, ""));
            return user;
        }
        return null;
    }

    @Override
    public void signIn(String id, String token) {
        sign(Integer.valueOf(id), token);// from fb

    }

    @Override
    public String getToken() {
        return sharedpreferences.getString(TOKEN, "");
    }

    @Override
    public void setToken(String token) {
        editor.putString(TOKEN, token);
        editor.commit();
    }

    @Override
    public int getId() {
        // TODO Auto-generated method stub
        return sharedpreferences.getInt(idKey, -1);
    }
}
