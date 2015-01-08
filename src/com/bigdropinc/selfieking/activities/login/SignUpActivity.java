package com.bigdropinc.selfieking.activities.login;

import android.app.Activity;
import android.app.LoaderManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.Loader;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bigdrop.selfieking.db.DatabaseManager;
import com.bigdropinc.selfieking.R;
import com.bigdropinc.selfieking.activities.editimages.SelectImageActivity;
import com.bigdropinc.selfieking.controller.loaders.Command;
import com.bigdropinc.selfieking.controller.loaders.CommandLoader;
import com.bigdropinc.selfieking.controller.managers.DialogManager;
import com.bigdropinc.selfieking.controller.managers.login.LoginManagerImpl;
import com.bigdropinc.selfieking.model.User;
import com.bigdropinc.selfieking.model.responce.StatusCode;

public class SignUpActivity extends Activity implements LoaderManager.LoaderCallbacks<StatusCode> {

    private static final int MIN_LENGHT = 2;
    private EditText loginEditText;
    private EditText emailEditText;
    private EditText passEditText;
    private EditText repeatpassEditText;
    private Button signUpButton;
    private Button closeButton;
    private int LOADER_ID = 0;
    private CommandLoader loader;
    private String LOG_TAG = "tag";
    private User user;
    private ProgressDialog dialog;

    @Override
    public Loader<StatusCode> onCreateLoader(int id, Bundle args) {
        if (id == LOADER_ID) {
            loader = new CommandLoader(this, args);
            Log.d(LOG_TAG, "onCreateLoader: " + loader.hashCode());
        }
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<StatusCode> loader, StatusCode code) {
        if (dialog != null) {
            dialog.cancel();
        }
        if (code.isSuccess()) {
            signUp(loader);
        } else {
            Toast.makeText(SignUpActivity.this, code.getError().get(0).errorMessage, Toast.LENGTH_SHORT).show();
        }
        getLoaderManager().destroyLoader(loader.getId());
    }

    @Override
    public void onLoaderReset(Loader<StatusCode> loader) {
        Log.d(LOG_TAG, "onLoaderReset: " + loader.hashCode());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        setContentView(R.layout.activity_sign_up);
        init();
        initListeners();
    }

    private void signUp(Loader<StatusCode> loader) {
        this.user = ((CommandLoader) loader).getUser();
        if (user != null && user.getToken() != null && !user.getToken().isEmpty()) {
            LoginManagerImpl.getInstance().signIn(user.getId(), user.getToken());
            DatabaseManager databaseManager = DatabaseManager.getInstance();
            databaseManager.addUser(user);
            goSelectImage();
        } else {
            Toast.makeText(SignUpActivity.this, "Registration error", Toast.LENGTH_SHORT).show();
        }
    }

    private User createUser() {
        User user = new User();
        user.setEmail(emailEditText.getText().toString());
        user.setUserName(loginEditText.getText().toString());
        user.setPassword(passEditText.getText().toString());
        return user;
    }

    private void init() {
        loginEditText = (EditText) findViewById(R.id.editTextSigUpLogin);
        emailEditText = (EditText) findViewById(R.id.editTextSigUpEmail);
        passEditText = (EditText) findViewById(R.id.editTextSigUpPass);
        repeatpassEditText = (EditText) findViewById(R.id.editTextSigUpRepeatPass);
        signUpButton = (Button) findViewById(R.id.btnSignUp);
        closeButton = (Button) findViewById(R.id.signUpCloseButton);
    }

    private void initListeners() {
        signUpButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (check()) {
                    user = createUser();
                    registr();
                }
            }
        });
        closeButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();

            }
        });
    }

    private boolean check() {
        String email = emailEditText.getText().toString();
        String pass = passEditText.getText().toString();
        String repeat = repeatpassEditText.getText().toString();
        String login = loginEditText.getText().toString();
        if (checkField(login)) {
            if (checkField(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                if (checkField(pass) && checkField(repeat)) {
                    if (pass.equals(repeat)) {
                        return true;
                    } else {
                        Toast.makeText(SignUpActivity.this, R.string.erepeat, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(SignUpActivity.this, R.string.ePass, Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(SignUpActivity.this, R.string.eEmail, Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(SignUpActivity.this, R.string.euserName, Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    private boolean checkField(String field) {
        return field != null && field.length() >= MIN_LENGHT;
    }

    private void goSelectImage() {
        SignUpActivity.this.finish();
        startActivity(new Intent(getApplicationContext(), SelectImageActivity.class));
    }

    private void registr() {
        dialog = ProgressDialog.show(SignUpActivity.this, "", "");
        dialog.setContentView(new ProgressBar(SignUpActivity.this), new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

        Bundle bundle = new Bundle();
        bundle.putParcelable("command", new Command(Command.REGISTR, user));
        getLoaderManager().initLoader(LOADER_ID, bundle, SignUpActivity.this).forceLoad();

    }

}
