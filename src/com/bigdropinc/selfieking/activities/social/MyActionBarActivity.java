package com.bigdropinc.selfieking.activities.social;

import java.util.UUID;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.bigdrop.selfieking.gcm.DeviceInfo;
import com.bigdropinc.selfieking.GCMHelper;
import com.bigdropinc.selfieking.R;
import com.bigdropinc.selfieking.activities.editimages.SelectImageActivity;
import com.bigdropinc.selfieking.activities.login.LogoActivity;
import com.bigdropinc.selfieking.activities.login.RegistrationActivity;
import com.bigdropinc.selfieking.activities.profile.ProfileFragment;
import com.bigdropinc.selfieking.controller.loaders.Command;
import com.bigdropinc.selfieking.controller.loaders.CommandLoader;
import com.bigdropinc.selfieking.controller.loaders.Constants;
import com.bigdropinc.selfieking.controller.managers.login.LoginManagerImpl;
import com.bigdropinc.selfieking.model.responce.StatusCode;
import com.bigdropinc.selfieking.model.selfie.Comment;
import com.bigdropinc.selfieking.model.selfie.Like;
import com.bigdropinc.selfieking.model.selfie.SelfieImage;
import com.google.android.gcm.GCMRegistrar;

public class MyActionBarActivity extends Activity implements LoaderManager.LoaderCallbacks<StatusCode> {

    public static final String M_CURRENT_TAB = "M_CURRENT_TAB";
    private static final String TAB_HOME = "TAB_HOME";
    private static final String TAB_LIKED = "TAB_LIKED";
    public static final String TAB_CAMERA = "TAB_CAMERA";
    public static final String TAB_CONTEST = "TAB_CONTEST";
    public static final String TAB_PROFILE = "TAB_PROFILE";

    private String mCurrentTab = TAB_PROFILE;
    private TabHost mTabHost;
    private int LOADER_ID_COMMENT = 21;
    private int LOADER_ID_CONTEST = 22;
    private CommandLoader loader;
    private String TAG = "tag";
    private FeedFragment feedFragment = new FeedFragment();
    private int LOADER_ID_LIKE = 10;
    private int LOADER_ID_DISLIKE = 11;
    private int id;
    private int index;
    private boolean fromCamera;
    public boolean fromContest;
    public static String uniqueID = null;
    private static final String PREF_REQ_ID = "PREF_UNIQUE_ID";
    public static String regId = "";
    private int LOADER_ID = 10;
    Fragment contestFragment = new ContestFragment();

    ProfileFragment profileFragment = new ProfileFragment();

    @Override
    public Loader<StatusCode> onCreateLoader(int id, Bundle args) {

        loader = new CommandLoader(this, args);
        Log.d(TAG, "onCreateLoader: " + loader.hashCode());

        return loader;
    }

    @Override
    public void onBackPressed() {
        // mTabHost.removeAllViews();
        // mTabHost = null;
        // Fragment f1=getFragmentManager().findFragmentByTag(TAB_CONTEST);
        // getFragmentManager().beginTransaction().remove(f1);
        // Fragment f2=getFragmentManager().findFragmentByTag(TAB_PROFILE);
        // getFragmentManager().beginTransaction().remove(f2);
        finish();
        // super.onBackPressed();

    }

    @Override
    public void onLoadFinished(Loader<StatusCode> loader, StatusCode code) {
        if (loader.getId() == LOADER_ID) {
            if (code.isSuccess()) {
                Log.d("gcm", "device rigestered on server");
                saveReqId();
            } else {
                Log.d("gcm", "device  not rigestered on server");
            }
        } else if (code.isSuccess()) {
            if (loader.getId() == LOADER_ID_CONTEST) {
                Toast.makeText(this, "Post is added to contest", Toast.LENGTH_SHORT).show();
            }
            updateFeed(loader);

        } else {
            Toast.makeText(this, code.getError().get(0).errorMessage, Toast.LENGTH_SHORT).show();
        }
        getLoaderManager().destroyLoader(loader.getId());
    }

    private void saveReqId() {
        SharedPreferences sharedPrefs = getSharedPreferences(PREF_REQ_ID, Context.MODE_PRIVATE);
        Editor editor = sharedPrefs.edit();
        editor.putString(PREF_REQ_ID, regId);
        editor.commit();
    }

    @Override
    public void onLoaderReset(Loader<StatusCode> arg0) {

    }

    @SuppressLint("NewApi")
    public void like(Like like, boolean liked, int index) {
        this.index = index;
        this.id = like.getPostId();
        Bundle bundle = getLiketBundle(like, liked);
        if (liked) {
            if (getLoaderManager().getLoader(LOADER_ID_LIKE + like.getPostId()) != null)
                getLoaderManager().getLoader(LOADER_ID_LIKE + like.getPostId()).reset();
            getLoaderManager().initLoader(LOADER_ID_DISLIKE + like.getPostId(), bundle, MyActionBarActivity.this).forceLoad();
        } else {
            if (getLoaderManager().getLoader(LOADER_ID_DISLIKE + like.getPostId()) != null)
                getLoaderManager().getLoader(LOADER_ID_DISLIKE + like.getPostId()).reset();
            getLoaderManager().initLoader(LOADER_ID_LIKE + like.getPostId(), bundle, MyActionBarActivity.this).forceLoad();
        }
        Log.d("listview", " MyActionBarActivity like selfie id =" + like.getPostId());
    }

    public void comment(Comment comment, int index) {
        this.id = comment.getPostId();
        this.index = index;
        Bundle bundle = getCommentBundle(comment);
        getLoaderManager().initLoader(LOADER_ID_COMMENT, bundle, MyActionBarActivity.this).forceLoad();
    }

    public void contest(SelfieImage selfieImage, int position) {
        this.index = position;
        this.id = selfieImage.getId();
        Bundle bundle = getContestBundle(selfieImage);
        getLoaderManager().initLoader(LOADER_ID_CONTEST, bundle, MyActionBarActivity.this).forceLoad();

    }

    /*
     * Example of starting nested fragment from another fragment:
     * 
     * Fragment newFragment = ManagerTagFragment.newInstance(tag.getMac());
     * TagsActivity tAct = (TagsActivity)getActivity();
     * tAct.pushFragments(newFragment, true, true, null);
     */
    public void pushFragments(android.app.Fragment fragment, boolean shouldAnimate, boolean shouldAdd, String tag) {
        android.app.FragmentManager manager = getFragmentManager();
        android.app.FragmentTransaction ft = manager.beginTransaction();
        Fragment find = manager.findFragmentByTag(tag);
        if (find == null) {
            ft.replace(R.id.realtabcontent, fragment, tag);
            ft.addToBackStack(tag);

        } else {
            if (!fragment.isAdded())
                ft.add(fragment, tag);
            if (tag == TAB_CONTEST) {

                ft.hide(profileFragment);
            } else {

                ft.hide(contestFragment);
            }
            ft.show(fragment);
        }
        ft.commit();
        // } else {
        // if (!fragment.isAdded())
        // ft.add(find, tag);
        // ft.show(find);
        // if (tag == TAB_CONTEST) {

        // ft.hide(profileFragment);
        // } else {

        // ft.hide(contestFragment);
        // }

        // if(fragment instanceof ContestFragment){
        // if((ProfileFragment)fragment).list.size()==0))
        // ft.replace(R.id.realtabcontent, fragment, tag);
        // }
        // if (fragment != null && !fragment.isAdded()) {
        // ft.replace(R.id.realtabcontent, fragment, tag);

        if (shouldAdd) {
            /*
             * here you can create named backstack for realize another logic.
             * ft.addToBackStack("name of your backstack");
             */

        } else {
            /*
             * and remove named backstack:
             * manager.popBackStack("name of your backstack",
             * FragmentManager.POP_BACK_STACK_INCLUSIVE); or remove whole:
             * manager.popBackStack(null,
             * FragmentManager.POP_BACK_STACK_INCLUSIVE);
             */
            // manager.popBackStack(null,
            // FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }

        // }
    }

    /*
     * create 3 tabs with name and image and add it to TabHost
     */
    public void initializeTabs() {

        TabHost.TabSpec spec;
        spec = mTabHost.newTabSpec(TAB_CONTEST);
        spec.setContent(new TabHost.TabContentFactory() {
            public View createTabContent(String tag) {
                return findViewById(R.id.realtabcontent);
            }
        });
        spec.setIndicator(createTabView(R.drawable.contest_selector, "contest"));
        mTabHost.addTab(spec);

        spec = mTabHost.newTabSpec(TAB_CAMERA);
        spec.setContent(new TabHost.TabContentFactory() {
            public View createTabContent(String tag) {
                return findViewById(R.id.realtabcontent);
            }
        });
        spec.setIndicator(createTabView(R.drawable.make_shot, "camera"));
        mTabHost.addTab(spec);

        spec = mTabHost.newTabSpec(TAB_PROFILE);
        spec.setContent(new TabHost.TabContentFactory() {
            public View createTabContent(String tag) {
                return findViewById(R.id.realtabcontent);
            }
        });
        spec.setIndicator(createTabView(R.drawable.profile_selector, "profile"));
        mTabHost.addTab(spec);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 33) {
            mTabHost.setCurrentTabByTag(mCurrentTab);
            mTabHost.setOnTabChangedListener(listener);
            initializeTabs();
        } else if (requestCode == 66)
            mTabHost.setCurrentTab(0);
        if (data != null) {
            SelfieImage selfieImage = data.getExtras().getParcelable("selfie");
            index = data.getExtras().getInt("index");
            feedFragment.updateGridView(index, selfieImage);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // if (savedInstanceState != null)
        // return;
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_actionbar);
        if (mTabHost == null) {
            initTab(savedInstanceState);
        }
        initGCM();
        checkFromPush();
    }

    private void checkFromPush() {
        if (getIntent() != null && getIntent().getExtras() != null) {
            int id = getIntent().getExtras().getInt(OneSelfieActivity.INTENT_SELFIE_ID);
            if (id != 0) {
                Intent i = new Intent(getApplicationContext(), OneSelfieActivity.class);
                i.putExtra(OneSelfieActivity.INTENT_SELFIE_ID, id);
                startActivityForResult(i, 33);

            }
        }
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        mTabHost = null;
    }

    private void initTab(Bundle savedInstanceState) {
        mTabHost = (TabHost) findViewById(android.R.id.tabhost);
        mTabHost.setup();
        if (savedInstanceState != null) {
            mCurrentTab = savedInstanceState.getString(M_CURRENT_TAB);

            mTabHost.setCurrentTabByTag(mCurrentTab);
            mTabHost.setOnTabChangedListener(listener);
            if (mTabHost.willNotDraw())
                initializeTabs();
        }
        if (getIntent() != null && getIntent().getExtras() != null) {
            Boolean b = getIntent().getExtras().getBoolean(Command.ADD_CONTEST);
            if (b != false) {
                mCurrentTab = TAB_PROFILE;
                fromContest = true;
                mTabHost.setCurrentTabByTag(mCurrentTab);
                mTabHost.setOnTabChangedListener(listener);
                initializeTabs();
            }
        } else {
            mTabHost.setCurrentTabByTag(mCurrentTab);
            mTabHost.setOnTabChangedListener(listener);
            initializeTabs();
        }
        mTabHost.setCurrentTabByTag(TAB_PROFILE);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (fromCamera) {
            mTabHost.setCurrentTab(2);
        }
        if (fromContest) {
            fromContest = false;
            mTabHost.setCurrentTab(0);
        }
        if (!LoginManagerImpl.getInstance().check()) {
            finish();
            Intent intent = new Intent(getApplicationContext(), RegistrationActivity.class);
            startActivity(intent);
        }
    }

    private View createTabView(final int id, final String text) {
        View view = LayoutInflater.from(this).inflate(R.layout.tabs_icon, null);
        ImageView imageView = (ImageView) view.findViewById(R.id.tab_icon);
        imageView.setImageDrawable(getResources().getDrawable(id));
        return view;
    }

    /*
     * first time listener will be trigered immediatelly after first:
     * mTabHost.addTab(spec); for set correct Tab in
     * setmTabHost.setCurrentTabByTag ignore first call of listener
     */
    private TabHost.OnTabChangeListener listener = new TabHost.OnTabChangeListener() {

        public void onTabChanged(String tabId) {
            if (fromContest) {
                tabId = TAB_CONTEST;
                mCurrentTab = TAB_CONTEST;
            } else {
                mCurrentTab = tabId;
            }
            if (tabId.equals(TAB_CONTEST)) {
                pushFragments(contestFragment, false, false, TAB_CONTEST);
            } else if (tabId.equals(TAB_PROFILE)) {
                pushFragments(profileFragment, false, false, TAB_PROFILE);
            } else {
                fromCamera = true;
                startActivity(new Intent(getApplicationContext(), SelectImageActivity.class));
            }

        }
    };

    private void addArguments(Fragment fragment) {
        Bundle data = new Bundle();
        data.putByteArray("image", getIntent().getByteArrayExtra("image"));
        data.putString("comment", getIntent().getStringExtra("comment"));
        fragment.setArguments(data);
    }

    private Bundle getCommentBundle(Comment comment) {
        Bundle bundle = new Bundle();
        Command command = new Command(Command.ADD_COMMENT);
        command.setComment(comment);
        bundle.putParcelable(Constants.COMMAND, command);
        return bundle;
    }

    private Bundle getContestBundle(SelfieImage selfieImage) {
        Bundle bundle = new Bundle();
        Command command = new Command(Command.ADD_CONTEST);
        selfieImage.setInContest(1);
        command.setSelfieImage(selfieImage);
        bundle.putParcelable(Constants.COMMAND, command);
        return bundle;
    }

    /*
     * If you want to start this activity from another
     */
    // public static void startUrself(Activity context) {
    // Intent newActivity = new Intent(context, TagsActivity.class);
    // newActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    // context.startActivity(newActivity);
    // context.finish();
    // }

    private void updateFeed(Loader<StatusCode> loader) {
        SelfieImage selfieImage = ((CommandLoader) loader).getSelfieImage();
        Log.d(TAG, "onLoadFinished: " + loader.hashCode());
        // if (loader.getId() == LOADER_ID_COMMENT || loader.getId() ==
        // LOADER_ID_LIKE + selfieImage.getId() || loader.getId() ==
        // LOADER_ID_DISLIKE + selfieImage.getId()) {
        if (selfieImage.getId() == id) {
            feedFragment.updateGridView(index, selfieImage);

        }
        // }
    }

    private Bundle getLiketBundle(Like like, boolean liked) {
        Bundle bundle = new Bundle();
        String commandName;
        if (liked) {
            commandName = Command.DISLIKE;
        } else {
            commandName = Command.LIKE;
        }
        Command command = new Command(commandName);
        command.setLike(like);
        bundle.putParcelable(Constants.COMMAND, command);
        return bundle;
    }

    private void initGCM() {
        SharedPreferences sharedPrefs = getSharedPreferences(PREF_REQ_ID, Context.MODE_PRIVATE);
        String req = sharedPrefs.getString(PREF_REQ_ID, null);
        if (req == null) {
            GCMHelper gcmHelper = new GCMHelper(getApplicationContext());
            GCMRegistrar.checkDevice(getApplicationContext());
            GCMRegistrar.checkManifest(getApplicationContext());
            GCMRegistrar.register(getApplicationContext(), GCMHelper.SENDER_ID);
            while (regId.equals("")) {
                regId = GCMRegistrar.getRegistrationId(getApplicationContext());
            }
            Log.v("gcm", "regid : " + regId);
            sendInfoToServer();
        }
    }

    private synchronized String initUUID(Context context) {
        if (uniqueID == null) {
            SharedPreferences sharedPrefs = context.getSharedPreferences(PREF_REQ_ID, Context.MODE_PRIVATE);
            uniqueID = sharedPrefs.getString(PREF_REQ_ID, null);
            if (uniqueID == null) {
                uniqueID = generateUUID();
                if (uniqueID.length() > 36) {
                    while (uniqueID.length() > 36) {
                        uniqueID = uniqueID.substring(0, uniqueID.length() - 1);
                    }
                }
                Editor editor = sharedPrefs.edit();
                editor.putString(PREF_REQ_ID, uniqueID);
                editor.commit();
            }
            Log.v("GCM", "uniqueID : " + uniqueID + " unique.lemght " + uniqueID.length());
        }
        return uniqueID;
    }

    private String generateUUID() {
        final TelephonyManager tm = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
        final String tmDevice, androidId;
        tmDevice = "" + tm.getDeviceId();
        androidId = "" + Secure.getString(getApplicationContext().getContentResolver(), Secure.ANDROID_ID);
        UUID deviceUuid = new UUID(androidId.hashCode(), ((long) tmDevice.hashCode() << 32));
        String deviceId = deviceUuid.toString();
        return deviceId;
    }

    private void sendInfoToServer() {

        Bundle bundle = new Bundle();
        DeviceInfo deviceInfo = new DeviceInfo();
        // deviceInfo.setUuid(uniqueID);
        deviceInfo.setToken(regId);
        deviceInfo.setPlatform("android");
        Command command = new Command(Command.REGISTR_DEVICE);
        command.setDeviceInfo(deviceInfo);
        bundle.putParcelable(Command.BUNDLE_NAME, command);
        getLoaderManager().initLoader(LOADER_ID, bundle, this).forceLoad();

    }
}
