package com.bigdropinc.selfieking.activities.profile;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.Loader;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bigdrop.selfieking.db.DatabaseManager;
import com.bigdropinc.selfieking.R;
import com.bigdropinc.selfieking.controller.loaders.Command;
import com.bigdropinc.selfieking.controller.loaders.CommandLoader;
import com.bigdropinc.selfieking.controller.managers.login.LoginManagerImpl;
import com.bigdropinc.selfieking.model.Password;
import com.bigdropinc.selfieking.model.User;
import com.bigdropinc.selfieking.model.responce.StatusCode;

public class ChangePasswordActivity extends Activity implements LoaderManager.LoaderCallbacks<StatusCode> {
    private TextView oldTextView;
    private TextView newPass;
    private TextView repeatNewPass;
    private Button edit;
    private Button back;
    private CommandLoader loader;
    private int LOADER_ID = 25;

    @Override
    public Loader<StatusCode> onCreateLoader(int id, Bundle args) {
        loader = new CommandLoader(this, args);
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<StatusCode> arg0, StatusCode statusCode) {
        if (statusCode.isSuccess()) {
            User user = ((CommandLoader) loader).getUser();
            if (user != null) {
                user.setToken(LoginManagerImpl.getInstance().getToken());
                DatabaseManager.getInstance().updateUser(user);
                LoginManagerImpl.getInstance().setToken(user.getToken());
            }
            finish();
            Toast.makeText(this, "Password was changed", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, statusCode.getError().get(0).errorMessage, Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onLoaderReset(Loader<StatusCode> arg0) {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        initViews();
        initListeners();
    }

    private void initViews() {
        oldTextView = (TextView) findViewById(R.id.changePasswordCurrent);
        newPass = (TextView) findViewById(R.id.changePasswordNew);
        repeatNewPass = (TextView) findViewById(R.id.changePasswordRepeat);
        edit = (Button) findViewById(R.id.changePasswordButton);
        back = (Button) findViewById(R.id.changePasswordBack);
    }

    private void initListeners() {
        edit.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                changePassword();
            }
        });
        back.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void startLoader(String old, String newPass) {
        Bundle bundle = new Bundle();
        Password password = new Password();
        password.setNewPassword(newPass);
        password.setOld(old);
        Command command = new Command(Command.CHANGE_PASSWORD);
        command.setPassword(password);
        bundle.putParcelable(Command.BUNDLE_NAME, command);
        getLoaderManager().initLoader(LOADER_ID, bundle, this).forceLoad();
    }

    private void changePassword() {
        String pass = newPass.getText().toString();
        String old = oldTextView.getText().toString();
        if (pass.length() >= 6 && pass.equals(repeatNewPass.getText().toString())) {
            startLoader(old, pass);
        } else {
            Toast.makeText(this, "Password is too short. Minimum 6 characters.", Toast.LENGTH_SHORT).show();
        }
    }
}
