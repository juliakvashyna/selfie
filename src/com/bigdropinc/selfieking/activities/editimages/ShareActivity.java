package com.bigdropinc.selfieking.activities.editimages;

import io.fabric.sdk.android.Fabric;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.LoaderManager;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore.Images;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bigdrop.selfieking.db.DatabaseManager;
import com.bigdropinc.selfieking.R;
import com.bigdropinc.selfieking.activities.social.MyActionBarActivity;
import com.bigdropinc.selfieking.controller.loaders.Command;
import com.bigdropinc.selfieking.controller.loaders.CommandLoader;
import com.bigdropinc.selfieking.controller.managers.login.LoginManagerImpl;
import com.bigdropinc.selfieking.model.responce.StatusCode;
import com.bigdropinc.selfieking.model.selfie.EditImage;
import com.bigdropinc.selfieking.model.selfie.SelfieImage;
import com.bigdropinc.selfieking.views.ImageHelper;
import com.facebook.UiLifecycleHelper;
import com.facebook.widget.FacebookDialog;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.PlusShare;
import com.twitter.sdk.android.tweetcomposer.TweetComposer;

public class ShareActivity extends Activity implements LoaderManager.LoaderCallbacks<StatusCode>, ConnectionCallbacks, OnConnectionFailedListener {

    private static final String SHARE_TEXT = "My selfie from SelfieKing";
    private static final int LOADER_ID = 3;
    private static final int RC_SIGN_IN = 56;
    private Button shareButton;
    private Button back;
    private ImageView imageView;
    private EditText editText;
    private Bitmap image;
    private byte[] byteArray;
    private CommandLoader commandLoader;
    private String TAG = "tag";
    private EditImage editImage;
    private Button fbButton;
    private Button googleButton;
    private Button twitterButton;
    boolean fbSelected;
    boolean gSelected;
    boolean twSelected;
    private ProgressDialog dialog;
    private Uri myImageUri;

    private GoogleApiClient mGoogleApiClient;
    private UiLifecycleHelper uiHelper;
    private boolean mIntentInProgress;

    @Override
    public void onPause() {
        super.onPause();
        uiHelper.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        uiHelper.onDestroy();
    }

    @Override
    public Loader<StatusCode> onCreateLoader(int id, Bundle args) {
        if (id == LOADER_ID) {
            commandLoader = new CommandLoader(this, args);
            Log.d(TAG, "onCreateLoader: " + commandLoader.hashCode());
        }
        return commandLoader;
    }

    @Override
    public void onLoadFinished(Loader<StatusCode> arg0, StatusCode code) {
        checkCodeAndShare(code);
        if (dialog != null)
            dialog.cancel();
    }

    @Override
    public void onLoaderReset(Loader<StatusCode> arg0) {

    }

    @Override
    public void onConnected(Bundle arg0) {
    }

    @Override
    public void onDisconnected() {
    }

    public void onConnectionSuspended(int cause) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        if (!mIntentInProgress && result.hasResolution()) {
            try {
                mIntentInProgress = true;
                startIntentSenderForResult(result.getResolution().getIntentSender(), RC_SIGN_IN, null, 0, 0, 0);
            } catch (SendIntentException e) {
                mIntentInProgress = false;
                mGoogleApiClient.connect();
            }
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);
        initView();
        initImage();
        initListeners();
        uiHelper = new UiLifecycleHelper(this, null);
        uiHelper.onCreate(savedInstanceState);
        Fabric.with(this, new TweetComposer());
        mGoogleApiClient = new GoogleApiClient.Builder(this).addApi(Plus.API)

        .addScope(Plus.SCOPE_PLUS_LOGIN).build();

    }

    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    protected void onStop() {
        super.onStop();

        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        uiHelper.onResume();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        uiHelper.onSaveInstanceState(outState);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            mIntentInProgress = false;

            if (!mGoogleApiClient.isConnecting()) {
                mGoogleApiClient.connect();
            }
        }
        uiHelper.onActivityResult(requestCode, resultCode, data, new FacebookDialog.Callback() {
            @Override
            public void onError(FacebookDialog.PendingCall pendingCall, Exception error, Bundle data) {
                Log.e("share", String.format("Error: %s", error.toString()));
            }

            @Override
            public void onComplete(FacebookDialog.PendingCall pendingCall, Bundle data) {
                Log.i("share", "Success!");
            }
        });

    }

    private void checkCodeAndShare(StatusCode code) {
        if (code != null) {
            if (code.isSuccess())
                share();
            else {
                Toast.makeText(ShareActivity.this, code.getError().get(0).errorMessage, Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(ShareActivity.this, "Share error", Toast.LENGTH_SHORT).show();
        }
    }

    private void initView() {
        shareButton = (Button) findViewById(R.id.shareButton);
        fbButton = (Button) findViewById(R.id.fbButton);
        googleButton = (Button) findViewById(R.id.googleButton);
        twitterButton = (Button) findViewById(R.id.twButton);
        imageView = (ImageView) findViewById(R.id.shareImage);
        editText = (EditText) findViewById(R.id.shareComment);
        back = (Button) findViewById(R.id.shareBack);

    }

    private void initImage() {
        editImage = DatabaseManager.getInstance().findEditImage(getIntent().getIntExtra("id", 0));
        byteArray = editImage.getFilterImageBytes();
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPurgeable = true;
            options.inJustDecodeBounds = false;
            // options.inSampleSize = 2;
            options.inDither = true;
            image = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length, options);
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
        imageView.setImageBitmap(ImageHelper.getRoundedCornerBitmap(image, 50));
        myImageUri = getImageUri(getApplicationContext(), image);

    }

    private void initListeners() {
        shareButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog = ProgressDialog.show(ShareActivity.this, "", "");
                dialog.setContentView(new ProgressBar(ShareActivity.this), new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
                startCommandLoader();
            }
        });
        fbButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                shareFb();
            }

        });
        twitterButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                shareTwitter();
            }
        });
        googleButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                shareGoogle();
            }
        });
        back.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        Uri uri = Uri.parse(path);
        return uri;
    }

    private void share() {
        Intent intent = new Intent(getApplicationContext(), MyActionBarActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        DatabaseManager.getInstance().deleteSelfie(editImage.getId());
        getApplicationContext().getContentResolver().delete(myImageUri, null, null);
        startActivity(intent);
    }

    private void startCommandLoader() {
        Bundle bundle = new Bundle();
        SelfieImage image = new SelfieImage();
        image.setBytesImage(byteArray);
        image.setDescription(editText.getText().toString());
        image.setToken(LoginManagerImpl.getInstance().getToken());
        Command command = new Command(Command.POST_SELFIE);
        command.setSelfieImage(image);
        bundle.putParcelable("command", command);
        getLoaderManager().initLoader(LOADER_ID, bundle, ShareActivity.this).forceLoad();
    }

    private void shareFb() {
        if (!fbSelected) {
            fbSelected = true;
            fbButton.setSelected(fbSelected);
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            image.compress(Bitmap.CompressFormat.PNG, 100, bytes);
            ArrayList<Bitmap> col = new ArrayList<Bitmap>();
            col.add(image);
            FacebookDialog shareDialog = new FacebookDialog.PhotoShareDialogBuilder(this).addPhotos(col).build();
            uiHelper.trackPendingDialogCall(shareDialog.present());
        }

    }

    private void shareTwitter() {
        if (!twSelected) {
            twSelected = true;
            twitterButton.setSelected(twSelected);
            TweetComposer.Builder builder = new TweetComposer.Builder(this).text(SHARE_TEXT).image(myImageUri);
            builder.show();
        }
    }

    private void shareGoogle() {
        if (!gSelected) {
            gSelected = true;
            googleButton.setSelected(gSelected);
            Intent shareIntent = new PlusShare.Builder(this).setType("image/*").setText(SHARE_TEXT).setStream(myImageUri).getIntent();
            startActivityForResult(shareIntent, RC_SIGN_IN);
        }
    }

}
