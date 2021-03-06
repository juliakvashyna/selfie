package com.bigdropinc.selfieking.activities.profile;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bigdrop.selfieking.db.DatabaseManager;
import com.bigdropinc.selfieking.R;
import com.bigdropinc.selfieking.activities.login.RegistrationActivity;
import com.bigdropinc.selfieking.controller.loaders.Command;
import com.bigdropinc.selfieking.controller.loaders.CommandLoader;
import com.bigdropinc.selfieking.controller.managers.login.LoginManagerImpl;
import com.bigdropinc.selfieking.model.User;
import com.bigdropinc.selfieking.model.responce.StatusCode;

public class ProfileEditActivity extends Activity implements LoaderManager.LoaderCallbacks<StatusCode>, OnClickListener {
    private static final String FEMALE = "Female";
    private static final String MALE = "Male";
    private EditText userNameEditText;
    private EditText jobEditText;
    private EditText siteEditText;
    private EditText emailEditText;
    private EditText phoneEditText;
    private Button genderSpinner;
    private Button editButton;
    private Button backButton;
    private Button changePassword;;
    private Button deleteAccount;
    private Button deleteAll;
    private Button signOut;
    private User user;

    private CommandLoader commandLoader;
    private int LOADER_ID = 13;
    private int LOADER_ID_EDIT = 14;
    private final int LOADER_ID_DELETE = 15;
    private final int LOADER_ID_DELETE_ALL = 16;

    @Override
    public Loader<StatusCode> onCreateLoader(int id, Bundle args) {
        commandLoader = new CommandLoader(this, args);
        return commandLoader;
    }

    @Override
    public void onLoadFinished(Loader<StatusCode> loader, StatusCode statusCode) {
        checkingCode(loader, statusCode);
        getLoaderManager().destroyLoader(loader.getId());
    }

    @Override
    public void onLoaderReset(Loader<StatusCode> arg0) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.editProfileButton:
            editProfile();
            break;

        case R.id.editProfileBack:
            onBackPressed();
            break;

        case R.id.changePassword:
            goChangePassword();
            break;

        case R.id.deleteAccount:
            deleteAccount();
            break;

        case R.id.signOut:
            signOut();
            break;
        case R.id.editPofileMale:
            changeGender();
            break;

        // case R.id.deleteAll:
        // deleteAll();
        // break;

        default:
            break;
        }
    }

    private void changeGender() {
        if (genderSpinner.getText().toString().equals(MALE)) {
            genderSpinner.setText(FEMALE);
        } else
            genderSpinner.setText(MALE);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        setContentView(R.layout.activity_profile_edit);
        initVews();
        initListeners();
        initUser();
    }

    // @Override
    // protected void onResume() {
    // super.onResume();
    // // if (!LoginManagerImpl.getInstance().check()) {
    // //
    // // finish();
    // // }
    //
    // }

    private void checkingCode(Loader<StatusCode> loader, StatusCode statusCode) {
        switch (loader.getId()) {
        case LOADER_ID_DELETE: {
            delete(statusCode);
            break;
        }
        case LOADER_ID_DELETE_ALL: {
            deleteAll(statusCode);
            break;
        }
        default: {
            editAndFill(loader);
            break;
        }
        }
    }

    private void deleteAll(StatusCode statusCode) {
        if (statusCode.isSuccess()) {
            Toast.makeText(this, "All posts are deleted", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, statusCode.getError().get(0).errorMessage, Toast.LENGTH_SHORT).show();
        }
    }

    private void delete(StatusCode statusCode) {
        if (statusCode.isSuccess()) {
            Toast.makeText(this, "Account was deleted", Toast.LENGTH_SHORT).show();
            signOut();
        } else {
            Toast.makeText(this, statusCode.getError().get(0).errorMessage, Toast.LENGTH_SHORT).show();
        }
    }

    private void initVews() {
        userNameEditText = (EditText) findViewById(R.id.editPofileUserName);
        jobEditText = (EditText) findViewById(R.id.editPofileProfession);
        siteEditText = (EditText) findViewById(R.id.editPofileSite);
        emailEditText = (EditText) findViewById(R.id.editPofileEmail);
        phoneEditText = (EditText) findViewById(R.id.editPofilePhone);
        genderSpinner = (Button) findViewById(R.id.editPofileMale);

        editButton = (Button) findViewById(R.id.editProfileButton);
        backButton = (Button) findViewById(R.id.editProfileBack);
        changePassword = (Button) findViewById(R.id.changePassword);
        deleteAccount = (Button) findViewById(R.id.deleteAccount);
        // deleteAll = (Button) findViewById(R.id.deleteAll);
        signOut = (Button) findViewById(R.id.signOut);

    }

    private void initListeners() {
        editButton.setOnClickListener(this);
        backButton.setOnClickListener(this);
        changePassword.setOnClickListener(this);
        deleteAccount.setOnClickListener(this);
        // deleteAll.setOnClickListener(this);
        signOut.setOnClickListener(this);
        genderSpinner.setOnClickListener(this);

    }

    private User getEditedUser() {
        user.setToken(LoginManagerImpl.getInstance().getToken());
        user.setEmail(emailEditText.getText().toString());
        user.setUserName(userNameEditText.getText().toString());
        user.setPhone(phoneEditText.getText().toString());
        user.setJob(jobEditText.getText().toString());
        user.setSite(siteEditText.getText().toString());
        int gender;
        if (genderSpinner.getText().toString().equals(MALE)) {
            gender = 2;
        } else
            gender = 1;
        user.setGender(gender);
        return user;
    }

    private void initUser() {
        user = DatabaseManager.getInstance().findUser(LoginManagerImpl.getInstance().getToken());
        Command command = new Command(Command.GET_USER);
        Bundle bundle = new Bundle();
        bundle.putParcelable(Command.BUNDLE_NAME, command);
        getLoaderManager().initLoader(LOADER_ID, bundle, ProfileEditActivity.this).forceLoad();
    }

    private void signOut() {
        LoginManagerImpl.getInstance().signOut();
        DatabaseManager.getInstance().deleteUser(user);
        // this.finishActivity(35);

        moveTaskToBack(true);
        finish();
        Intent intent = new Intent(getApplicationContext(), RegistrationActivity.class);
        startActivity(intent);
    }

    private void editAndFill(Loader<StatusCode> loader) {
        user = ((CommandLoader) loader).getUser();
        if (user != null) {
            user.setToken(LoginManagerImpl.getInstance().getToken());
            DatabaseManager.getInstance().updateUser(user);
            LoginManagerImpl.getInstance().setToken(user.getToken());
            if (loader.getId() == LOADER_ID) {
                fillUser();
            } else if (loader.getId() == LOADER_ID_EDIT) {
                Toast.makeText(this, "Profile was edited", Toast.LENGTH_SHORT).show();
                hideSoftKeyboard(this);
                setResult(Activity.RESULT_OK);
                finish();
            }
        } else {
            Log.d("tag", "user null ProfileEdit");
        }
    }

    private void fillUser() {
        userNameEditText.setText(user.getUserName());
        jobEditText.setText(user.getJob());
        siteEditText.setText(user.getSite());
        emailEditText.setText(user.getEmail());
        phoneEditText.setText(user.getPhone());
        if (user.getGender() == 2) {
            genderSpinner.setText(MALE);
        } else {
            genderSpinner.setText(FEMALE);
        }
    }

    private void deleteAll() {
        Bundle bundle = new Bundle();
        Command command = new Command(Command.DELETE_SELFIE);
        bundle.putParcelable(Command.BUNDLE_NAME, command);
        getLoaderManager().initLoader(LOADER_ID_DELETE_ALL, bundle, ProfileEditActivity.this).forceLoad();

    }

    private void deleteAccount() {
        Bundle bundle = new Bundle();
        Command command = new Command(Command.DELETE_ACCOUNT);
        bundle.putParcelable(Command.BUNDLE_NAME, command);
        getLoaderManager().initLoader(LOADER_ID_DELETE, bundle, ProfileEditActivity.this).forceLoad();
    }

    private void goChangePassword() {
        Intent intent = new Intent(getApplicationContext(), ChangePasswordActivity.class);
        startActivityForResult(intent, 35);
    }

    private void editProfile() {
        Bundle bundle = new Bundle();
        user = getEditedUser();
        Command command = new Command(Command.EDIT_PROFILE, user);
        bundle.putParcelable(Command.BUNDLE_NAME, command);
        getLoaderManager().initLoader(LOADER_ID_EDIT, bundle, ProfileEditActivity.this).forceLoad();
    }

    private static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }
}
