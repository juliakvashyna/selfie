package com.bigdropinc.selfieking.activities.editimages;

import io.fabric.sdk.android.Fabric;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.app.LoaderManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.content.Loader;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore.Images;
import android.text.Layout;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.vending.billing.IabException;
import com.android.vending.billing.IabHelper;
import com.android.vending.billing.IabHelper.OnIabPurchaseFinishedListener;
import com.android.vending.billing.IabResult;
import com.android.vending.billing.Inventory;
import com.android.vending.billing.Purchase;
import com.bigdrop.selfieking.db.DatabaseManager;
import com.bigdropinc.selfieking.R;
import com.bigdropinc.selfieking.activities.social.MyActionBarActivity;
import com.bigdropinc.selfieking.controller.loaders.Command;
import com.bigdropinc.selfieking.controller.loaders.CommandLoader;
import com.bigdropinc.selfieking.controller.loaders.Constants;
import com.bigdropinc.selfieking.controller.managers.login.LoginManagerImpl;
import com.bigdropinc.selfieking.model.responce.StatusCode;
import com.bigdropinc.selfieking.model.selfie.EditImage;
import com.bigdropinc.selfieking.model.selfie.SelfieImage;
import com.facebook.UiLifecycleHelper;
import com.facebook.widget.FacebookDialog;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;

import com.google.android.gms.internal.im;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.PlusShare;
import com.makeramen.roundedimageview.RoundedImageView;
import com.twitter.sdk.android.tweetcomposer.TweetComposer;

public class ShareActivity extends Activity implements LoaderManager.LoaderCallbacks<StatusCode>, ConnectionCallbacks, OnConnectionFailedListener, LocationListener {

    private static final int PURCHASE_REQUEST_CODE = 12;
    private static final String WATERMARK = "watermark";
    private static final String SHARE_TEXT = "My selfie from SelfieKing";
    private static final int LOADER_ID = 3;
    private static final int RC_SIGN_IN = 56;
    private static final int LOADER_ID_CONTEST = 15;
    private Button shareButton;
    private Button back;
    private RoundedImageView imageView;
    private EditText editText;
    private Bitmap image;
    private byte[] byteArray;
    private CommandLoader commandLoader;
    private String TAG = "tag";
    private EditImage editImage;
    private Button fbButton;
    private Button googleButton;
    private Button twitterButton;
    private Button addToContestButton;
    // private Button purshaceButton;
    boolean fbSelected;
    boolean gSelected;
    boolean twSelected;
    private ProgressDialog dialog;
    private Uri myImageUri;
    IabHelper mHelper;
    private GoogleApiClient mGoogleApiClient;
    private UiLifecycleHelper uiHelper;
    private boolean mIntentInProgress;
    private TextView countryTextView;
    PaymentDialog paydialog;
    Location location;
    private boolean addToContest;
    private Bitmap imageOriginal;
    private String countryName;

    @Override
    public void onPause() {
        super.onPause();
        uiHelper.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        uiHelper.onDestroy();
        if (mHelper != null)
            mHelper.dispose();
        mHelper = null;
    }

    private void initIabHelper() {
        String base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAppnnGKdUh/6nuk2BL1LZuyN9r60w0q/Zqx3Hkmw6IKZLj/MhpN/+KygSSlje5IlasacAd5r1fw9uQWdL3VlmWHgJ16RAkAyeqGqXT+MH43zGEdiHrKCfUJZGOvbo6jTe/rnzAvpdZl1BCLZ0G2CuY/tr2VDIUcOCTk4AHkj8V13zqekqQBK8TuNP2Eq86B17fwQtqlrLRIQOIyVcHVphGVIaDYy5VqkyH5Erfb5oMh+KLFlCUXz72mHxPINp1yYFJ/Xp4hCuHxpmCK9F+mqnSPA8+7zWaWXbWLneMDXa+AZnnKi5XGEjjHAat0fXZR76Hj6/NDyKUq03ll5V+YvJOwIDAQAB";
        mHelper = new IabHelper(this, base64EncodedPublicKey);
        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                if (!result.isSuccess()) {
                    Log.d(TAG, "Problem setting up In-app Billing: " + result);
                } else {
                    List<String> list = new ArrayList<String>();
                    list.add(WATERMARK);
                    Inventory inventory = null;
                    try {
                        inventory = mHelper.queryInventory(true, list);
                    } catch (IabException e) {
                        e.printStackTrace();
                    }
                    Purchase purchase = inventory.getPurchase(WATERMARK);
                    if (purchase != null) {
                        deleteMark();
                    } else {
                        initPaymentDialog();
                    }
                    Log.d(TAG, "Setting up In-app Billing: SUCCESS DONE");
                }
            }
        });
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
    public void onLocationChanged(Location location) {
        this.location = location;
    }

    @Override
    public void onProviderDisabled(String provider) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onProviderEnabled(String provider) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO Auto-generated method stub

    }

    public void contest(SelfieImage selfieImage) {
        Bundle bundle = getContestBundle(selfieImage);
        getLoaderManager().initLoader(LOADER_ID_CONTEST, bundle, ShareActivity.this).forceLoad();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);
        initView();
        initIabHelper();
        startInitImage();
        initListeners();
        uiHelper = new UiLifecycleHelper(this, null);
        uiHelper.onCreate(savedInstanceState);
        Fabric.with(this, new TweetComposer());
        mGoogleApiClient = new GoogleApiClient.Builder(this).addApi(Plus.API).addScope(Plus.SCOPE_PLUS_LOGIN).build();
        // if not pay
        // initPaymentDialog();
        displayCountry();
    }

    private void initPaymentDialog() {
        OnClickListener listener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                paydialog.dismiss();
                mHelper.launchPurchaseFlow(ShareActivity.this, WATERMARK, PURCHASE_REQUEST_CODE, new OnIabPurchaseFinishedListener() {
                    @Override
                    public void onIabPurchaseFinished(IabResult result, Purchase info) {
                        Toast.makeText(ShareActivity.this, result.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        };
        paydialog = new PaymentDialog(this, image, listener);
        paydialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        paydialog.show();
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
        if (requestCode == PURCHASE_REQUEST_CODE) {

        } else {
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

    }

    private void checkCodeAndShare(StatusCode code) {
        if (code != null) {
            if (code.isSuccess())
                if (addToContest) {
                    shareContestBundle();
                } else {
                    share();
                }
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
        imageView = (RoundedImageView) findViewById(R.id.shareImage);
        editText = (EditText) findViewById(R.id.shareComment);
        back = (Button) findViewById(R.id.shareBack);
        addToContestButton = (Button) findViewById(R.id.contestButton);
        countryTextView = (TextView) findViewById(R.id.sharecountry);

    }

    private void startInitImage() {
        new AsyncTask<Void, Void, Bitmap>() {
            @Override
            protected void onPreExecute() {
                dialog = ProgressDialog.show(ShareActivity.this, "", "");
                dialog.setContentView(new ProgressBar(ShareActivity.this), new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
                super.onPreExecute();
            }

            @Override
            protected Bitmap doInBackground(Void... params) {
                return initImage();
            }

            protected void onPostExecute(Bitmap result) {
                if (result != null) {
                    imageView.setImageBitmap(image);
                    myImageUri = getImageUri(getApplicationContext(), image);
                } else {
                    Toast.makeText(ShareActivity.this, "Sorry, too big image", Toast.LENGTH_LONG).show();
                }
                if (dialog != null)
                    dialog.cancel();
            };
        }.execute();

    }

    public static Bitmap loadImageFromStorage(String path) {

        try {
            File f = new File(path, "profile.png");
            return BitmapFactory.decodeStream(new FileInputStream(f));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;

    }

    private void addWaterMark() {
        if (image != null) {
            Bitmap res = Bitmap.createBitmap(image);
            Bitmap mutableBitmap = res.copy(Bitmap.Config.ARGB_8888, true);
            Bitmap watermark = BitmapFactory.decodeResource(getResources(), R.drawable.watermark);
            watermark = Bitmap.createScaledBitmap(watermark, 800, 600, false);
            Canvas canvas = new Canvas(mutableBitmap);
            int left = mutableBitmap.getWidth() - watermark.getWidth() + 200;
            int top = mutableBitmap.getHeight() - watermark.getHeight() + 160;
            canvas.drawBitmap(watermark, left, top, null);
            image = Bitmap.createBitmap(mutableBitmap);
            // imageView.setImageBitmap(image);
            // myImageUri = getImageUri(getApplicationContext(), image);
        }
    }

    private void deleteMark() {
        image = Bitmap.createBitmap(imageOriginal);
        imageView.setImageBitmap(image);
        myImageUri = getImageUri(getApplicationContext(), image);
    }

    private void initListeners() {
        shareButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog = ProgressDialog.show(ShareActivity.this, "", "");
                dialog.setContentView(new ProgressBar(ShareActivity.this), new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
                startCommandLoader(Command.POST_SELFIE);
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
        addToContestButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog = ProgressDialog.show(ShareActivity.this, "", "");
                dialog.setContentView(new ProgressBar(ShareActivity.this), new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

                startCommandLoader(Command.POST_SELFIE_CONTEST);
                addToContest = true;
            }

        });

    }

    private void shareContestBundle() {
        Intent intent = new Intent(getApplicationContext(), MyActionBarActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        Bundle bundle = new Bundle();
        bundle.putBoolean(Command.ADD_CONTEST, true);
        intent.putExtras(bundle);
        intent.putExtra(Command.ADD_CONTEST, true);
        DatabaseManager.getInstance().deleteSelfie(editImage.getId());
        hideSoftKeyboard(this);
        getApplicationContext().getContentResolver().delete(myImageUri, null, null);
        startActivity(intent);

    }

    private Bundle getContestBundle(SelfieImage selfieImage) {
        Bundle bundle = new Bundle();
        Command command = new Command(Command.ADD_CONTEST);
        selfieImage.setInContest(1);
        command.setSelfieImage(selfieImage);
        bundle.putParcelable(Constants.COMMAND, command);
        return bundle;
    }

    private Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.PNG, 0, bytes);
        String path = Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        Uri uri = Uri.parse(path);
        return uri;
    }

    private void share() {
        Intent intent = new Intent(getApplicationContext(), MyActionBarActivity.class);
        intent.putExtra("draft", true);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        DatabaseManager.getInstance().deleteSelfie(editImage.getId());
        hideSoftKeyboard(this);
        getApplicationContext().getContentResolver().delete(myImageUri, null, null);
        startActivity(intent);
    }

    private static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }

    private void startCommandLoader(String commandName) {
        Bundle bundle = new Bundle();
        SelfieImage selfie = new SelfieImage();
        editImage.createBytesFilter(image, 100);
        byteArray = editImage.getFilterImageBytes();
        selfie.setBytesImage(byteArray);
        selfie.setDescription(editText.getText().toString());
        selfie.setToken(LoginManagerImpl.getInstance().getToken());
        selfie.setLocation(countryTextView.getText().toString());
        Command command = new Command(commandName);
        command.setSelfieImage(selfie);
        bundle.putParcelable("command", command);
        getLoaderManager().initLoader(LOADER_ID, bundle, ShareActivity.this).forceLoad();
    }

    private void shareFb() {
        try {
            if (!fbSelected) {
                fbSelected = true;
                fbButton.setSelected(fbSelected);
                // ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                if (image != null && uiHelper != null) {
                    // image.compress(Bitmap.CompressFormat.PNG, 100, bytes);
                    ArrayList<Bitmap> col = new ArrayList<Bitmap>();
                    col.add(image);
                    FacebookDialog shareDialog = new FacebookDialog.PhotoShareDialogBuilder(this).addPhotos(col).build();
                    uiHelper.trackPendingDialogCall(shareDialog.present());
                } else {
                    Toast.makeText(this, "Sorry, Facebook error!", Toast.LENGTH_LONG).show();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Sorry, Facebook error!", Toast.LENGTH_LONG).show();
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

    private void displayCountry() {
        String locationProvider = LocationManager.NETWORK_PROVIDER;
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(locationProvider, 0, 0, this);
        location = locationManager.getLastKnownLocation(locationProvider);
        if (location != null) {
            setCityCountry();
        }
    }

    private void setCityCountry() {
        Geocoder gcd = new Geocoder(this, Locale.ENGLISH);
        List<Address> addresses = null;
        try {
            addresses = gcd.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (addresses != null) {
            if (addresses.size() > 0) {
                countryName = addresses.get(0).getCountryName();
                countryTextView.setText(countryName + ", " + addresses.get(0).getLocality());
            } else {
                countryTextView.setText("Search...");
            }
        }
    }

    private Bitmap initImage() {
        editImage = DatabaseManager.getInstance().findEditImage(getIntent().getIntExtra("idEditImage", 0));
        if (editImage != null) {
            image = loadImageFromStorage(editImage.getPath());
            imageOriginal = Bitmap.createBitmap(image);
            addWaterMark();
        } else {
            return null;
        }
        return image;
    }

}
