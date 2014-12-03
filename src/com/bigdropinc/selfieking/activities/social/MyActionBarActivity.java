package com.bigdropinc.selfieking.activities.social;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.bigdropinc.selfieking.R;
import com.bigdropinc.selfieking.activities.editimages.SelectImageActivity;
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

public class MyActionBarActivity extends Activity implements LoaderManager.LoaderCallbacks<StatusCode> {

    public static final String M_CURRENT_TAB = "M_CURRENT_TAB";
    public static final String TAB_HOME = "TAB_HOME";
    public static final String TAB_LIKED = "TAB_LIKED";
    public static final String TAB_CAMERA = "TAB_CAMERA";
    public static final String TAB_CONTEST = "TAB_CONTEST";
    public static final String TAB_PROFILE = "TAB_PROFILE";

    private String mCurrentTab;
    private TabHost mTabHost;
    private int LOADER_ID_COMMENT = 21;
    private int LOADER_ID_CONTEST = 22;
    private CommandLoader loader;
    private String TAG = "tag";
    private FeedFragment feedFragment;
    private int LOADER_ID_LIKE = 10;
    private int LOADER_ID_DISLIKE = 11;
    private int id;
    private int index;

    @Override
    public Loader<StatusCode> onCreateLoader(int id, Bundle args) {

        loader = new CommandLoader(this, args);
        Log.d(TAG, "onCreateLoader: " + loader.hashCode());

        return loader;
    }

    @Override
    public void onLoadFinished(Loader<StatusCode> loader, StatusCode code) {
        if (code.isSuccess()) {
            if (loader.getId() == LOADER_ID_CONTEST) {
                Toast.makeText(this, "Post is added to contest", Toast.LENGTH_SHORT).show();
            }
            updateFeed(loader);

        } else {
            Toast.makeText(this, code.getError().get(0).errorMessage, Toast.LENGTH_SHORT).show();
        }
        getLoaderManager().destroyLoader(loader.getId());
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

    public void contest(SelfieImage selfieImage) {
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

        ft.replace(R.id.realtabcontent, fragment, tag);

        if (shouldAdd) {
            /*
             * here you can create named backstack for realize another logic.
             * ft.addToBackStack("name of your backstack");
             */
            ft.addToBackStack(null);
        } else {
            /*
             * and remove named backstack:
             * manager.popBackStack("name of your backstack",
             * FragmentManager.POP_BACK_STACK_INCLUSIVE); or remove whole:
             * manager.popBackStack(null,
             * FragmentManager.POP_BACK_STACK_INCLUSIVE);
             */
            manager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
        ft.commit();
    }

    /*
     * create 3 tabs with name and image and add it to TabHost
     */
    public void initializeTabs() {

        TabHost.TabSpec spec;

        spec = mTabHost.newTabSpec(TAB_HOME);
        spec.setContent(new TabHost.TabContentFactory() {
            public View createTabContent(String tag) {
                return findViewById(R.id.realtabcontent);
            }
        });
        spec.setIndicator(createTabView(R.drawable.home_selector, "home"));

        mTabHost.addTab(spec);

        spec = mTabHost.newTabSpec(TAB_LIKED);
        spec.setContent(new TabHost.TabContentFactory() {
            public View createTabContent(String tag) {
                return findViewById(R.id.realtabcontent);
            }
        });
        spec.setIndicator(createTabView(R.drawable.liked_selector, "liked"));
        mTabHost.addTab(spec);

        spec = mTabHost.newTabSpec(TAB_CAMERA);
        spec.setContent(new TabHost.TabContentFactory() {
            public View createTabContent(String tag) {
                return findViewById(R.id.realtabcontent);
            }
        });
        spec.setIndicator(createTabView(R.drawable.icon_make_shot, "camera"));
        mTabHost.addTab(spec);

        spec = mTabHost.newTabSpec(TAB_CONTEST);
        spec.setContent(new TabHost.TabContentFactory() {
            public View createTabContent(String tag) {
                return findViewById(R.id.realtabcontent);
            }
        });
        spec.setIndicator(createTabView(R.drawable.contest_selector, "contest"));
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
        if (data != null) {
            Comment comment = data.getExtras().getParcelable("comment");
            int index = data.getExtras().getInt("position");
            comment(comment, index);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_actionbar);
        mTabHost = (TabHost) findViewById(android.R.id.tabhost);
        mTabHost.setup();
        if (savedInstanceState != null) {
            mCurrentTab = savedInstanceState.getString(M_CURRENT_TAB);
            initializeTabs();
            mTabHost.setCurrentTabByTag(mCurrentTab);
            /*
             * when resume state it's important to set listener after
             * initializeTabs
             */
            mTabHost.setOnTabChangedListener(listener);
        } else {
            mTabHost.setOnTabChangedListener(listener);
            initializeTabs();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
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

            mCurrentTab = tabId;

            if (tabId.equals(TAB_HOME)) {
                feedFragment = new FeedFragment();

                addArguments(feedFragment);
                pushFragments(feedFragment, false, false, TAB_HOME);
            } else if (tabId.equals(TAB_LIKED)) {
                pushFragments(new FeedFragment(), false, false, TAB_LIKED);
            } else if (tabId.equals(TAB_CAMERA)) {
                startActivity(new Intent(getApplicationContext(), SelectImageActivity.class));
            } else if (tabId.equals(TAB_CONTEST)) {
                pushFragments(new ContestFragment(), false, false, TAB_CONTEST);
            } else if (tabId.equals(TAB_PROFILE)) {
                pushFragments(new ProfileFragment(), false, false, TAB_PROFILE);
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
        if (loader.getId() == LOADER_ID_COMMENT || loader.getId() == LOADER_ID_LIKE + selfieImage.getId() || loader.getId() == LOADER_ID_DISLIKE + selfieImage.getId()) {
            if (selfieImage.getId() == id) {
                feedFragment.updateGridView(index, selfieImage);
            }
        }
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

}
