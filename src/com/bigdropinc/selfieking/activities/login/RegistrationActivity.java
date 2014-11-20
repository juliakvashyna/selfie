package com.bigdropinc.selfieking.activities.login;

import java.util.Arrays;

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
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.bigdrop.selfieking.db.DatabaseManager;
import com.bigdropinc.selfieking.R;
import com.bigdropinc.selfieking.activities.editimages.SelectImageActivity;
import com.bigdropinc.selfieking.controller.InternetChecker;
import com.bigdropinc.selfieking.controller.loaders.Command;
import com.bigdropinc.selfieking.controller.loaders.CommandLoader;
import com.bigdropinc.selfieking.controller.managers.login.LoginManager;
import com.bigdropinc.selfieking.controller.managers.login.LoginManagerImpl;
import com.bigdropinc.selfieking.model.User;
import com.bigdropinc.selfieking.model.responce.StatusCode;
import com.facebook.Request;
import com.facebook.Request.GraphUserCallback;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;

public class RegistrationActivity extends Activity implements OnClickListener, LoaderManager.LoaderCallbacks<StatusCode> {
    private static final String TAG = null;
    private static final int LOADER_ID = 0;
    private EditText loginEditText;
    private EditText passEditText;
    private Button signInbutton;
    private Button signUpbutton;
    private Button resetPassButton;
    private UiLifecycleHelper uiHelper;
    private LoginManager loginManager;
    private Button signFBButton;
    private CommandLoader loader;
    private User user;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.btnRegSignIn:
            signIn();
            break;
        case R.id.btnRegSignFB:
            performFacebookLogin();
            break;
        case R.id.btnRegResetPass:
            resetPass();
            break;
        case R.id.btnRegSignUp:
            signUp();
            break;
        default:
            break;
        }
    }

    @Override
    public Loader<StatusCode> onCreateLoader(int id, Bundle args) {

        if (id == LOADER_ID) {
            loader = new CommandLoader(this, args);
            Log.d("tag", "onCreateLoader: " + loader.hashCode());
        }
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<StatusCode> loader, StatusCode statusCode) {
        if (statusCode.isSuccess()) {
            this.user = ((CommandLoader) loader).getUser();
            if (user != null && user.getToken() != null) {
                goSelectImage();
            } else {
                Toast.makeText(RegistrationActivity.this, "Login error", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(RegistrationActivity.this, statusCode.getError().get(0).errorMessage, Toast.LENGTH_LONG).show();
        }
        getLoaderManager().destroyLoader(loader.getId());
    }

    @Override
    public void onLoaderReset(Loader<StatusCode> statusCode) {
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        uiHelper.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        setContentView(R.layout.activity_registration);
        initFields();

        initView();
        initListeners();
        uiHelper = new UiLifecycleHelper(this, callback);
        uiHelper.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        if (loginManager.check()) {
            this.finish();
        }
        super.onResume();
    }

    private void goSelectImage() {
        loginManager.signIn(user.getId(), user.getToken());
        DatabaseManager databaseManager = DatabaseManager.getInstance();
        databaseManager.addUser(user);
        Intent intent = new Intent(getApplicationContext(), SelectImageActivity.class);
        startActivity(intent);
        this.finish();
    }

    private void initView() {
        loginEditText = (EditText) findViewById(R.id.editTextRegLogin);
        passEditText = (EditText) findViewById(R.id.editTextRegPass);
        signInbutton = (Button) findViewById(R.id.btnRegSignIn);
        signUpbutton = (Button) findViewById(R.id.btnRegSignUp);
        resetPassButton = (Button) findViewById(R.id.btnRegResetPass);
        signFBButton = (Button) findViewById(R.id.btnRegSignFB);

    }

    private void initFields() {
        loginManager = LoginManagerImpl.getInstance();
    }

    private void initListeners() {
        signInbutton.setOnClickListener(this);
        signFBButton.setOnClickListener(this);
        signUpbutton.setOnClickListener(this);
        resetPassButton.setOnClickListener(this);
    }

    private void signUp() {
        Intent intent = new Intent(getApplicationContext(), SignUpActivity.class);
        startActivity(intent);
    }

    private static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }

    private void signIn() {
        // progressDialog = new ProgressDialog(this);
        // progressDialog.show();
        hideSoftKeyboard(this);
        if (InternetChecker.isNetworkConnected()) {
            String email = loginEditText.getText().toString();
            User user = new User(email, passEditText.getText().toString());
            Bundle bundle = new Bundle();
            Command command = new Command(Command.LOGIN, user);

            bundle.putParcelable(Command.BUNDLE_NAME, command);
            getLoaderManager().initLoader(LOADER_ID, bundle, this).forceLoad();
        } else {
            InternetChecker.showNotInternetError(this);
        }
    }

    private void resetPass() {
        Intent intent = new Intent(getApplicationContext(), ResetPassActivity.class);
        startActivity(intent);
        this.finish();

    }

    private void onSessionStateChange(Session session, SessionState state, Exception exception) {
        Log.d("FACEBOOK", "onSessionStateChange session state" + session.getState());
        if (state.isOpened()) {
            Log.i(TAG, "Logged in...");
        } else if (state.isClosed()) {
            Log.i(TAG, "Logged out...");
        }
    }

    private Session.StatusCallback callback = new Session.StatusCallback() {
        @Override
        public void call(Session session, SessionState state, Exception exception) {
            onSessionStateChange(session, state, exception);
        }
    };

    private boolean isFetching;

    private void performFacebookLogin() {
        Log.d("FACEBOOK", "performFacebookLogin");
        final Session.NewPermissionsRequest newPermissionsRequest = new Session.NewPermissionsRequest(this, Arrays.asList("email"));
        Session openActiveSession = Session.openActiveSession(this, true, new Session.StatusCallback() {
            @Override
            public void call(Session session, SessionState state, Exception exception) {
                Log.d("FACEBOOK", "call");
                if (session.isOpened() && !isFetching) {
                    Log.d("FACEBOOK", "if (session.isOpened() && !isFetching)");
                    isFetching = true;
                    session.requestNewReadPermissions(newPermissionsRequest);
                    Request getMe = Request.newMeRequest(session, new GraphUserCallback() {
                        @Override
                        public void onCompleted(GraphUser user, Response response) {
                            Log.d("FACEBOOK", "onCompleted");
                            if (user != null) {
                                Log.d("FACEBOOK", "user != null");
                                org.json.JSONObject graphResponse = response.getGraphObject().getInnerJSONObject();
                                String email = graphResponse.optString("email");
                                String token = graphResponse.optString("token");
                                // String id = graphResponse.optString("id");
                                // String facebookName = user.getUsername();
                                if (email == null || email.length() < 0) {
                                    return;
                                } else {
                                    loginManager.signIn(user.getName(), token);
                                    signIn();
                                }
                            }
                        }
                    });
                    getMe.executeAsync();
                } else {
                    if (!session.isOpened()) {
                        Log.d("FACEBOOK", "!session.isOpened()");
                        Log.d("FACEBOOK", "session state" + session.getState());
                    } else {
                        Log.d("FACEBOOK", "isFetching");
                    }

                }
            }
        });
    }

}
