package com.bigdropinc.selfieking.activities.profile;

import java.util.ArrayList;
import java.util.List;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
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
import com.bigdropinc.selfieking.controller.loaders.Command;
import com.bigdropinc.selfieking.controller.loaders.CommandLoader;
import com.bigdropinc.selfieking.controller.managers.login.LoginManagerImpl;
import com.bigdropinc.selfieking.model.User;
import com.bigdropinc.selfieking.model.responce.StatusCode;
import com.bigdropinc.selfieking.model.selfie.SelfieImage;
import com.bigdropinc.selfieking.views.RoundedImageView;

public class ProfileFragment extends Fragment implements LoaderManager.LoaderCallbacks<StatusCode> {
    private static final int REQUEST_EDIT = 35;
    private static final int LOADER_ID = 1;

    private RoundedImageView avatar;
    private int IMAGE_SIZE = 200;
    private View rootView;
    private GridView gridView;
    private ImageButton editProfileButton;
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

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_profile, container, false);
        initViews();
        initUser();
        initFeed();
        initListeners();
        return rootView;
    }

    @Override
    public android.content.Loader<StatusCode> onCreateLoader(int id, Bundle args) {
        loader = new CommandLoader(getActivity(), args);
        Log.d(LOG_TAG, "onCreateLoader: " + loader.hashCode());
        return loader;
    }

    @Override
    public void onLoadFinished(android.content.Loader<StatusCode> arg0, StatusCode statusCode) {
        updateGridview(statusCode);
        getLoaderManager().destroyLoader(loader.getId());
    }

    @Override
    public void onLoaderReset(android.content.Loader<StatusCode> arg0) {
    }

    private void updateGridview(StatusCode statusCode) {
        if (statusCode.isSuccess()) {
            more = (ArrayList<SelfieImage>) ((CommandLoader) loader).getSelfies();
            if (more != null) {
                images.addAll(more);
                countTextView.setText(String.valueOf(images.size()));
            }
            adapter.notifyDataSetChanged();
        } else {
            Toast.makeText(getActivity(), statusCode.getError().get(0).errorMessage, Toast.LENGTH_SHORT).show();
        }
    }

    private void initFeed() {
        if (InternetChecker.isNetworkConnected()) {
            initGridview();
            startGetSelfieLoader();
        } else {
            InternetChecker.showNotInternetError(getActivity());
        }
    }

    private void initUser() {
        user = DatabaseManager.getInstance().findUser(LoginManagerImpl.getInstance().getToken());
        nameTextView.setText(user.getUserName());
        emailTextView.setText(user.getEmail());
    }

    private void initViews() {
        avatar = (RoundedImageView) rootView.findViewById(R.id.avatar);
        CustomPicasso.getImageLoader(getActivity()).load("http://i.dailymail.co.uk/i/pix/2014/03/10/article-0-1C2B325500000578-458_634x699.jpg").into(avatar);
        gridView = (GridView) rootView.findViewById(R.id.profileGridView);

        editProfileButton = (ImageButton) rootView.findViewById(R.id.profileEditButton);
        nameTextView = (TextView) rootView.findViewById(R.id.profileUserNameTextView);
        emailTextView = (TextView) rootView.findViewById(R.id.profileEmailTextView);
        countTextView = (TextView) rootView.findViewById(R.id.profileCountTextView);
        backButton = (ImageButton) rootView.findViewById(R.id.profileBack);
    }

    private void initListeners() {
        editProfileButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startEditActivity(user.getId());
            }
        });
        backButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getFragmentManager().beginTransaction().remove(ProfileFragment.this).commit();
            }
        });
    }

    private void initGridview() {
        adapter = new ImageAdapter(getActivity(), R.layout.image_item_gridview, images);
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
        getLoaderManager().initLoader(LOADER_ID, bundle, ProfileFragment.this).forceLoad();
    }

    private void startEditActivity(int id) {
        Intent intent = new Intent(getActivity().getApplicationContext(), ProfileEditActivity.class);
        intent.putExtra("userId", id);
        getActivity().startActivityForResult(intent, REQUEST_EDIT);
    }

    private void loadMore(int page) {
        command.setOffset(page);
        bundle.putParcelable(Command.BUNDLE_NAME, command);
        getLoaderManager().initLoader(LOADER_ID + page, bundle, ProfileFragment.this).forceLoad();
    }

    private void startOneSelfieActivity(AdapterView<?> parent, int position) {
        SelfieImage selfie = (SelfieImage) parent.getItemAtPosition(position);
        Intent intent = new Intent(getActivity().getApplicationContext(), OneSelfieActivity.class);

        intent.putExtra(OneSelfieActivity.INTENT_SELFIE_ID, selfie.getId());
        getActivity().startActivity(intent);
    }

}
