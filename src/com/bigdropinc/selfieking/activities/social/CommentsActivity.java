package com.bigdropinc.selfieking.activities.social;

import java.util.ArrayList;
import android.app.Activity;
import android.app.LoaderManager;
import android.content.Loader;
import android.os.Bundle;
import android.widget.ListView;
import com.bigdropinc.selfieking.R;
import com.bigdropinc.selfieking.adapters.CommentAdapter;
import com.bigdropinc.selfieking.controller.InternetChecker;
import com.bigdropinc.selfieking.controller.loaders.Command;
import com.bigdropinc.selfieking.controller.loaders.CommandLoader;
import com.bigdropinc.selfieking.controller.managers.login.LoginManagerImpl;
import com.bigdropinc.selfieking.model.responce.StatusCode;
import com.bigdropinc.selfieking.model.selfie.Comment;
import com.bigdropinc.selfieking.model.selfie.SelfieImage;

public class CommentsActivity extends Activity implements LoaderManager.LoaderCallbacks<StatusCode> {

    private CommandLoader commandLoader;
    private ListView listView;
    private ArrayList<Comment> commentsList = new ArrayList<Comment>();
    private CommentAdapter adapter;
    private int LOADER_ID = 12;
    private int postId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);
        initView();
        postId = getIntent().getExtras().getInt("postId");
        if (InternetChecker.isNetworkConnected()) {
            startLoader();
        } else {
            InternetChecker.showNotInternetError(this);
        }

    }

    private void initView() {
        listView = (ListView) findViewById(R.id.commentsListView);

    }

    private void startLoader() {
        Bundle budle = getBundle();
        getLoaderManager().initLoader(LOADER_ID, budle, this).forceLoad();

    }

    private Bundle getBundle() {
        Bundle budle = new Bundle();
        Command command = new Command(Command.GET_SELFIE);
        SelfieImage selfieImage = new SelfieImage();
        selfieImage.setToken(LoginManagerImpl.getInstance().getToken());
        selfieImage.setId(postId);
        command.setSelfieImage(selfieImage);
        budle.putParcelable(Command.BUNDLE_NAME, command);
        return budle;
    }

    @Override
    public Loader<StatusCode> onCreateLoader(int id, Bundle args) {
        commandLoader = new CommandLoader(this, args);
        return commandLoader;
    }

    @Override
    public void onLoadFinished(Loader<StatusCode> loader, StatusCode arg1) {
        initComments(loader);
        getLoaderManager().destroyLoader(loader.getId());
        // adapter.notifyDataSetChanged();
    }

    private void initComments(Loader<StatusCode> loader) {
        commentsList = ((CommandLoader) loader).getCommentSelfieImage().getComments();
        adapter = new CommentAdapter(getApplicationContext(), R.layout.comment_item, commentsList);
        listView.setAdapter(adapter);
    }

    @Override
    public void onLoaderReset(Loader<StatusCode> arg0) {

    }

}
