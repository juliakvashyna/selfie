package com.bigdropinc.selfieking.activities.editimages;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bigdrop.selfieking.db.DatabaseManager;
import com.bigdropinc.selfieking.R;
import com.bigdropinc.selfieking.activities.social.MyActionBarActivity;
import com.bigdropinc.selfieking.controller.loaders.Command;
import com.bigdropinc.selfieking.controller.loaders.CommandLoader;
import com.bigdropinc.selfieking.controller.managers.login.LoginManagerImpl;
import com.bigdropinc.selfieking.model.responce.StatusCode;
import com.bigdropinc.selfieking.model.selfie.SelfieImage;

public class ShareActivity extends Activity implements LoaderManager.LoaderCallbacks<StatusCode> {

    private static final int LOADER_ID = 3;
    private Button shareButton;
    private ImageView imageView;
    private EditText editText;
    private Bitmap image;
    private byte[] byteArray;
    private CommandLoader commandLoader;
    private String TAG = "tag";

    // private MyProgressDialog dialog;

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
        // if (dialog != null)
        // dialog.cancel();
        checkCodeAndShare(code);
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

    @Override
    public void onLoaderReset(Loader<StatusCode> arg0) {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);
        initView();

        initImage();
        initListeners();
    }

    private void initView() {
        shareButton = (Button) findViewById(R.id.shareButton);
        imageView = (ImageView) findViewById(R.id.shareImage);
        editText = (EditText) findViewById(R.id.shareComment);

    }

    private void initImage() {
        // byteArray = getIntent().getByteArrayExtra("image");
        // image = BitmapFactory.decodeByteArray(byteArray, 0,
        // byteArray.length);
        byteArray = DatabaseManager.getInstance().findEditImage(getIntent().getIntExtra("id", 0)).getResult();

        image = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
        imageView.setImageBitmap(image);

    }

    private void initListeners() {
        shareButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // dialog = new MyProgressDialog(ShareActivity.this);
                // MyProgressDialog.show(ShareActivity.this,"","");
                startCommandLoader();
            }
        });
    }

    private void share() {
        Intent intent = new Intent(getApplicationContext(), MyActionBarActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        DatabaseManager.getInstance().deleteSelfie(getIntent().getIntExtra("id", 0));
   //     intent.putExtra("id", byteArray);
     //   intent.putExtra("comment", editText.getText().toString());
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
}
