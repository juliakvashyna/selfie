package com.bigdropinc.selfieking.activities.profile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.Config;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bigdrop.selfieking.db.DatabaseManager;
import com.bigdropinc.selfieking.R;
import com.bigdropinc.selfieking.activities.social.OneSelfieActivity;
import com.bigdropinc.selfieking.adapters.EndlessScrollListener;
import com.bigdropinc.selfieking.adapters.ImageAdapter;
import com.bigdropinc.selfieking.controller.CustomPicasso;
import com.bigdropinc.selfieking.controller.InternetChecker;
import com.bigdropinc.selfieking.controller.UrlRequest;
import com.bigdropinc.selfieking.controller.loaders.Command;
import com.bigdropinc.selfieking.controller.loaders.CommandLoader;
import com.bigdropinc.selfieking.controller.managers.login.LoginManagerImpl;
import com.bigdropinc.selfieking.model.User;
import com.bigdropinc.selfieking.model.responce.StatusCode;
import com.bigdropinc.selfieking.model.selfie.SelfieImage;
import com.bigdropinc.selfieking.views.RoundedImageView;

public class ProfileActivity extends Activity implements LoaderManager.LoaderCallbacks<StatusCode> {

    private static final int LOADER_ID = 1;
    private static final int LOADER_ID_USER = 2;
    private RoundedImageView avatar;
    private GridView gridView;

    private ImageButton backButton;
    private TextView nameTextView;
    private TextView emailTextView;
    private TextView countTextView;
    private List<SelfieImage> images = new ArrayList<SelfieImage>();
    private User user;
    private String LOG_TAG = "tag";
    private CommandLoader loader;
    private Bundle bundle;
    private Command command;
    private ImageAdapter adapter;
    private ArrayList<SelfieImage> more;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        initUserById();
        initViews();
        startGetUser();
        initFeed();
        initListeners();

    }

    private void initUserById() {
        user = new User();
        int id = getIntent().getExtras().getInt("userId");
        user.setId(id);
    }

    @Override
    public android.content.Loader<StatusCode> onCreateLoader(int id, Bundle args) {
        loader = new CommandLoader(this, args);
        Log.d(LOG_TAG, "onCreateLoader: " + loader.hashCode());
        return loader;
    }

    @Override
    public void onLoadFinished(android.content.Loader<StatusCode> loader, StatusCode statusCode) {
        if (loader.getId() == LOADER_ID_USER) {
            user = ((CommandLoader) loader).getUser();
            initUser();
        } else {
            updateGridview(statusCode);
        }
        getLoaderManager().destroyLoader(loader.getId());
    }

    @Override
    public void onLoaderReset(android.content.Loader<StatusCode> arg0) {
    }

    private void updateGridview(StatusCode statusCode) {
        if (statusCode.isSuccess()) {
            more = (ArrayList<SelfieImage>) ((CommandLoader) loader).getSelfies();
            if (more != null) {
                if (!images.containsAll(more))
                    images.addAll(more);
                countTextView.setText(String.valueOf(images.size()));
            }
            adapter.notifyDataSetChanged();
        } else {
            Toast.makeText(this, statusCode.getError().get(0).errorMessage, Toast.LENGTH_SHORT).show();
        }
    }

    private void initFeed() {
        if (InternetChecker.isNetworkConnected()) {
            initGridview();
            startGetSelfieLoader();
        } else {
            InternetChecker.showNotInternetError(this);
        }
    }

    private void initUser() {
        // user =
        // DatabaseManager.getInstance().findUser(LoginManagerImpl.getInstance().getToken());
        nameTextView.setText(user.getUserName());
        emailTextView.setText(user.getEmail());
        String url = "http://i.dailymail.co.uk/i/pix/2014/03/10/article-0-1C2B325500000578-458_634x699.jpg";
        String userAvatar = user.getAvatar();
        if (userAvatar != null && userAvatar != "")
            url = UrlRequest.ADDRESS + userAvatar;
        CustomPicasso.getImageLoader(this).load(url).into(avatar);
    }

    private void initViews() {
        avatar = (RoundedImageView) findViewById(R.id.avatar);
        gridView = (GridView) findViewById(R.id.profileGridView);
        nameTextView = (TextView) findViewById(R.id.profileUserNameTextView);
        emailTextView = (TextView) findViewById(R.id.profileEmailTextView);
        countTextView = (TextView) findViewById(R.id.profileCountTextView);
        backButton = (ImageButton) findViewById(R.id.profileBack);
    }

    private void initListeners() {

        backButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void initGridview() {
        adapter = new ImageAdapter(this, R.layout.image_item_gridview, images);
        gridView.setAdapter(adapter);
        gridView.setOnScrollListener(new EndlessScrollListener() {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                loadMore(page);
            }
        });
        gridView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                startOneSelfieActivity(parent, position);
            }
        });
    }

    private void startGetSelfieLoader() {
        bundle = new Bundle();
        command = new Command(Command.GET_SELFIES, user);
        command.setOffset(0);
        bundle.putParcelable(Command.BUNDLE_NAME, command);
        getLoaderManager().initLoader(LOADER_ID, bundle, ProfileActivity.this).forceLoad();
    }

    private void startGetUser() {
        bundle = new Bundle();
        command = new Command(Command.GET_USER, user);
        bundle.putParcelable(Command.BUNDLE_NAME, command);
        getLoaderManager().initLoader(LOADER_ID_USER, bundle, ProfileActivity.this).forceLoad();
    }

    private void loadMore(int page) {
        command.setOffset(page);
        bundle.putParcelable(Command.BUNDLE_NAME, command);
        getLoaderManager().initLoader(LOADER_ID + page, bundle, ProfileActivity.this).forceLoad();
    }

    private void startOneSelfieActivity(AdapterView<?> parent, int position) {
        SelfieImage selfie = (SelfieImage) parent.getItemAtPosition(position);
        Intent intent = new Intent(getApplicationContext(), OneSelfieActivity.class);
        intent.putExtra(OneSelfieActivity.INTENT_SELFIE_ID, selfie.getId());
        intent.putExtra(OneSelfieActivity.FROM_PROFILE, !selfie.isInContest());
        startActivityForResult(intent, 77);
    }

}
