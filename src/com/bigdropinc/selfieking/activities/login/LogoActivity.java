package com.bigdropinc.selfieking.activities.login;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.bigdropinc.selfieking.R;
import com.bigdropinc.selfieking.activities.social.MyActionBarActivity;
import com.bigdropinc.selfieking.controller.managers.login.LoginManagerImpl;

public class LogoActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logo);
        start();

    }

    private void start() {
        if (!LoginManagerImpl.getInstance().check()) {
            startHandler();
        } else {
            startActionBarctivity();
        }
    }

    private void startHandler() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                openActivity();
            }
        }, 1000);
    }

    private void startActionBarctivity() {
        Intent intent = new Intent(getApplicationContext(), MyActionBarActivity.class);
        startActivity(intent);
        LogoActivity.this.finish();
    }

    private void openActivity() {
        Intent intent = null;
        if (LoginManagerImpl.getInstance().check()) {
            intent = new Intent(getApplicationContext(), MyActionBarActivity.class);
        } else {
            intent = new Intent(getApplicationContext(), RegistrationActivity.class);
        }
        startActivity(intent);
        LogoActivity.this.finish();
    }
}
