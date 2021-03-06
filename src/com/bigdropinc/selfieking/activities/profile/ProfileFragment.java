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
import com.bigdropinc.selfieking.activities.social.MyActionBarActivity;
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
import com.bigdropinc.selfieking.model.responce.ResponseListSelfie;
import com.bigdropinc.selfieking.model.responce.StatusCode;
import com.bigdropinc.selfieking.model.selfie.SelfieImage;
import com.bigdropinc.selfieking.views.RoundedImageView;

public class ProfileFragment extends Fragment implements LoaderManager.LoaderCallbacks<StatusCode> {
    private static final int OFFSET = 20;
    public static final int REQUEST_CODE_ONE_SELFIE = 77;
    private static final int REQUEST_EDIT = 35;
    private static final int LOADER_ID = 1;
    private static final int LOADER_ID_AVATAR = 2;
    private static final int LOADER_ID_USER = 3;
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
    private ArrayList<SelfieImage> moreContest = new ArrayList<SelfieImage>();;
    private ArrayList<SelfieImage> moreDrafts = new ArrayList<SelfieImage>();;

    private List<SelfieImage> drafts = new ArrayList<SelfieImage>();
    private List<SelfieImage> incontest = new ArrayList<SelfieImage>();
    private Uri outputFileUri;
    private int YOUR_SELECT_PICTURE_REQUEST_CODE = 33;
    private Button sortButton;
    int mypage = 0;
    boolean end;
    private int countInContest;
    private int countDrafts;
    private boolean draft;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_profile, container, false);
        initViews();
        if (user == null)
            startUser();
        initFeed();
        initListeners();
        draft = ((MyActionBarActivity) getActivity()).draft;
        return rootView;
    }

    public void startUser() {
        Command command = new Command(Command.GET_USER);
        Bundle bundle = new Bundle();
        bundle.putParcelable(Command.BUNDLE_NAME, command);
        getLoaderManager().initLoader(LOADER_ID_USER, bundle, this).forceLoad();
    }

    @Override
    public android.content.Loader<StatusCode> onCreateLoader(int id, Bundle args) {
        loader = new CommandLoader(getActivity(), args);
        Log.d(LOG_TAG, "onCreateLoader: " + loader.hashCode());
        return loader;
    }

    @Override
    public void onLoadFinished(android.content.Loader<StatusCode> loader, StatusCode statusCode) {
        ResponseListSelfie responseListSelfie = ((CommandLoader) loader).getResponseListSelfie();
        if (loader.getId() == LOADER_ID_AVATAR) {
            User user = ((CommandLoader) loader).getUser();
            String url = UrlRequest.ADDRESS + user.getAvatar();
            CustomPicasso.getImageLoader(getActivity()).load(url).into(avatar);
            DatabaseManager.getInstance().updateUser(user);
        } else if (loader.getId() == LOADER_ID_USER) {
            user = ((CommandLoader) loader).getUser();
            if (user != null) {
                initUser();
            } else {
                Log.d("tag", "user is null ProfileFragment");
            }
        } else {
            updateGridview(responseListSelfie, statusCode);
        }

        getLoaderManager().destroyLoader(loader.getId());
    }

    @Override
    public void onResume() {
        super.onResume();
        if (user == null)
            startUser();
    }

    @Override
    public void onLoaderReset(android.content.Loader<StatusCode> arg0) {
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == YOUR_SELECT_PICTURE_REQUEST_CODE) {
                final boolean isCamera;
                if (data == null) {
                    isCamera = true;
                } else {
                    final String action = data.getAction();
                    if (action == null) {
                        isCamera = false;
                    } else {
                        isCamera = action.equals(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    }
                }
                Uri selectedImageUri;
                if (isCamera) {
                    selectedImageUri = outputFileUri;
                } else {
                    selectedImageUri = data == null ? null : data.getData();
                }
                if (selectedImageUri != null)
                    loadAvatar(selectedImageUri);
            } else if (requestCode == REQUEST_EDIT) {
                startUser();
            } else if (requestCode == REQUEST_CODE_ONE_SELFIE) {
                initFeed();
            }
        } else if (resultCode == 44) {
            initFeed();
            ((MyActionBarActivity) getActivity()).mTabHost.setCurrentTab(0);
        }
    }

    private void updateGridview(ResponseListSelfie responseListSelfie, StatusCode statusCode) {
        if (statusCode.isSuccess()) {
            images = (ArrayList<SelfieImage>) responseListSelfie.posts.list;
            countInContest = responseListSelfie.posts.inContest;
            countDrafts = responseListSelfie.posts.count - countInContest;
            if (images != null) {
                updateLists();
            } else {
                end = true;
            }

        } else {
            Toast.makeText(getActivity(), statusCode.getError().get(0).errorMessage, Toast.LENGTH_SHORT).show();
        }
    }

    private void updateAdapter() {
        if (sortButton.getText().toString().equals("In contest")) {
            adapter.setImages(incontest);
            countTextView.setText(String.valueOf(countInContest));
        } else {
            adapter.setImages(drafts);
            countTextView.setText(String.valueOf(countDrafts));
        }
        adapter.notifyDataSetChanged();
        logAdapter();
    }

    private void logAdapter() {
        String log = "count = " + adapter.getCount() + " adapter.getImages() ";
        for (SelfieImage selfieImage : adapter.getImages()) {
            log += selfieImage.getId() + ", ";
        }
        Log.d("profile", log);
    }

    private void updateLists() {
        if (images.size() > 0 && incontest.size() <= countInContest && drafts.size() <= countDrafts) {
            for (SelfieImage image : images) {
                if (image.isInContest()) {
                    if (!incontest.contains(image)) {
                        incontest.add(image);
                    }
                } else {
                    if (!drafts.contains(image)) {
                        drafts.add(image);
                    }
                }
            }
            updateAdapter();
        } else {
            end = true;
        }
    }

    public void initFeed() {
        drafts = new ArrayList<SelfieImage>();
        incontest = new ArrayList<SelfieImage>();
        if (InternetChecker.isNetworkConnected()) {
            initGridview();
            startGetSelfieLoader();
        } else {
            InternetChecker.showNotInternetError(getActivity());
        }
    }

    private void initUser() {
        initUserNames();
        String url = "";
        String userAvatar = user.getAvatar();
        if (userAvatar != null && userAvatar != "") {
            url = UrlRequest.ADDRESS + userAvatar;
            if (!url.isEmpty())
                CustomPicasso.getImageLoader(getActivity()).load(url).into(avatar);
        }
    }

    private void initUserNames() {
        nameTextView.setText(user.getUserName());
        emailTextView.setText(user.getEmail());
    }

    private void initViews() {
        avatar = (RoundedImageView) rootView.findViewById(R.id.avatar);
        gridView = (GridView) rootView.findViewById(R.id.profileGridView);
        editProfileButton = (ImageButton) rootView.findViewById(R.id.profileEditButton);
        nameTextView = (TextView) rootView.findViewById(R.id.profileUserNameTextView);
        emailTextView = (TextView) rootView.findViewById(R.id.profileEmailTextView);
        countTextView = (TextView) rootView.findViewById(R.id.profileCountTextView);
        backButton = (ImageButton) rootView.findViewById(R.id.profileBack);
        sortButton = (Button) rootView.findViewById(R.id.sortinContestButton);
    }

    private void initListeners() {
        editProfileButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (user != null)
                    startEditActivity(user.getId());
            }
        });
        backButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getFragmentManager().beginTransaction().remove(ProfileFragment.this).commit();
            }
        });
        avatar.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                openImageIntent();

            }
        });
        sortButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                sort();
            }
        });
    }

    private void initGridview() {
        if (((MyActionBarActivity)getActivity()).draft) {
            ((MyActionBarActivity)getActivity()).draft=false;
            sortButton.setText(R.string.drafts);
            adapter = new ImageAdapter(getActivity(), R.layout.image_item_gridview, drafts);
        } else {
            sortButton.setText(R.string.inContest);
            adapter = new ImageAdapter(getActivity(), R.layout.image_item_gridview, incontest);
        }
        gridView.setAdapter(adapter);
        initOnScrollListener();
        gridView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                startOneSelfieActivity(parent, position);
            }
        });
        adapter.notifyDataSetChanged();
    }

    private void initOnScrollListener() {
        gridView.setOnScrollListener(new EndlessScrollListener() {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                if (!end) {
                    mypage = mypage + OFFSET;
                    loadMore(mypage);
                }
            }
        });
    }

    private void startGetSelfieLoader() {
        bundle = new Bundle();
        command = new Command(Command.GET_SELFIES);
        command.setOffset(0);
        bundle.putParcelable(Command.BUNDLE_NAME, command);
        getLoaderManager().initLoader(LOADER_ID, bundle, ProfileFragment.this).forceLoad();
    }

    private void startEditActivity(int id) {
        Intent intent = new Intent(getActivity().getApplicationContext(), ProfileEditActivity.class);
        intent.putExtra("userId", id);
        startActivityForResult(intent, REQUEST_EDIT);
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
        intent.putExtra(OneSelfieActivity.FROM_PROFILE, !selfie.isInContest());
        startActivityForResult(intent, REQUEST_CODE_ONE_SELFIE);
    }

    private void openImageIntent() {
        // Determine Uri of camera image to save.
        final File root = new File(Environment.getExternalStorageDirectory() + File.separator + "MyDir" + File.separator);
        root.mkdirs();
        final String fname = "test";
        final File sdImageMainDirectory = new File(root, fname);
        outputFileUri = Uri.fromFile(sdImageMainDirectory);

        // Camera.
        final List<Intent> cameraIntents = new ArrayList<Intent>();
        final Intent captureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        final PackageManager packageManager = getActivity().getPackageManager();
        final List<ResolveInfo> listCam = packageManager.queryIntentActivities(captureIntent, 0);
        for (ResolveInfo res : listCam) {
            final String packageName = res.activityInfo.packageName;
            final Intent intent = new Intent(captureIntent);
            intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            intent.setPackage(packageName);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
            cameraIntents.add(intent);
        }

        // Filesystem.
        final Intent galleryIntent = new Intent();
        galleryIntent.setType("image/*");
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

        // Chooser of filesystem options.
        final Intent chooserIntent = Intent.createChooser(galleryIntent, "Select Source");

        // Add the camera options.
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, cameraIntents.toArray(new Parcelable[] {}));

        startActivityForResult(chooserIntent, YOUR_SELECT_PICTURE_REQUEST_CODE);
    }

    private void loadAvatar(Uri selectedImageUri) {
        Bitmap bitmap = getBitmap(selectedImageUri);
        bitmap = Bitmap.createScaledBitmap(bitmap, 300, 300, false);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, stream);
        byte[] inputData = stream.toByteArray();
        Bundle bundle = new Bundle();
        user.setAvatarBytes(inputData);
        command = new Command(Command.AVATAR, user);
        bundle.putParcelable(Command.BUNDLE_NAME, command);
        getLoaderManager().initLoader(LOADER_ID_AVATAR, bundle, ProfileFragment.this).forceLoad();

    }

    private byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    private Bitmap getBitmap(Uri uri) {
        InputStream in = null;
        try {
            final int IMAGE_MAX_SIZE = 1200000; // 1.2MP
            in = getActivity().getContentResolver().openInputStream(uri);

            // Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            o.inPurgeable = true;
            o.inPreferredConfig = Config.RGB_565;
            o.inDither = true;
            BitmapFactory.decodeStream(in, null, o);
            in.close();

            int scale = 1;
            while ((o.outWidth * o.outHeight) * (1 / Math.pow(scale, 2)) > IMAGE_MAX_SIZE) {
                scale++;
            }

            Bitmap b = null;
            in = getActivity().getContentResolver().openInputStream(uri);
            if (scale > 1) {
                scale--;
                // scale to max possible inSampleSize that still yields an image
                // larger than target
                o = new BitmapFactory.Options();
                o.inSampleSize = scale;
                b = BitmapFactory.decodeStream(in, null, o);

                // resize to desired dimensions
                int height = b.getHeight();
                int width = b.getWidth();

                double y = Math.sqrt(IMAGE_MAX_SIZE / (((double) width) / height));
                double x = (y / height) * width;

                Bitmap scaledBitmap = Bitmap.createScaledBitmap(b, (int) x, (int) y, true);
                b.recycle();
                b = scaledBitmap;

                System.gc();
            } else {
                b = BitmapFactory.decodeStream(in);
            }
            in.close();

            return b;
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch (NullPointerException e) {

            e.printStackTrace();
            return null;
        }
    }

    private void sort() {
        if (sortButton.getText().toString().equals("In contest")) {
            sortButton.setText(R.string.drafts);
        } else {
            sortButton.setText(R.string.inContest);
        }
        updateAdapter();
        logAdapter();
    }
}
