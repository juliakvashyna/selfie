package com.bigdropinc.selfieking.activities.social;

import java.util.ArrayList;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.bigdropinc.selfieking.R;
import com.bigdropinc.selfieking.adapters.CommentAdapter;
import com.bigdropinc.selfieking.controller.InternetChecker;
import com.bigdropinc.selfieking.controller.loaders.Command;
import com.bigdropinc.selfieking.controller.loaders.CommandLoader;
import com.bigdropinc.selfieking.controller.loaders.Constants;
import com.bigdropinc.selfieking.controller.managers.login.LoginManagerImpl;
import com.bigdropinc.selfieking.model.responce.StatusCode;
import com.bigdropinc.selfieking.model.selfie.Comment;
import com.bigdropinc.selfieking.model.selfie.CommentSelfieImage;
import com.bigdropinc.selfieking.model.selfie.SelfieImage;

public class CommentsActivity extends Activity implements LoaderManager.LoaderCallbacks<StatusCode> {

    private CommandLoader commandLoader;
    private ListView listView;
    private ArrayList<Comment> commentsList = new ArrayList<Comment>();
    private CommentAdapter adapter;
    private int LOADER_ID = 12;
    private int postId;
    private EditText commentEditText;
    private Button sendCommentButton;
    private Button backButton;
    private Context previousActivity;
    private int position;
    private int LOADER_ID_COMMENT = 25;
    private CommentSelfieImage selfieImage;
    private int userId;

    @Override
    public Loader<StatusCode> onCreateLoader(int id, Bundle args) {
        commandLoader = new CommandLoader(this, args);
        return commandLoader;
    }

    @Override
    public void onLoadFinished(Loader<StatusCode> loader, StatusCode arg1) {
        if (loader.getId() == LOADER_ID_COMMENT) {
            selfieImage = ((CommandLoader) loader).getCommentSelfieImage();
            commentsList = selfieImage.getComments();
            // adapter = new CommentAdapter(getApplicationContext(),
            // R.layout.comment_item, commentsList);
            adapter.clear();
            adapter.addAll(commentsList);
            adapter.notifyDataSetChanged();
        } else {
            initComments(loader);

        }
        getLoaderManager().destroyLoader(loader.getId());
    }

    @Override
    public void onLoaderReset(Loader<StatusCode> arg0) {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        setContentView(R.layout.activity_comments);
        initView();
        initListeners();
        postId = getIntent().getExtras().getInt("postId");
        position = getIntent().getExtras().getInt("position");
        userId=getIntent().getExtras().getInt("userId");
        previousActivity = getIntent().getExtras().getParcelable("previous");
        if (InternetChecker.isNetworkConnected()) {
            startLoader();
        } else {
            InternetChecker.showNotInternetError(this);
        }
        if (getIntent() != null && getIntent().getExtras() != null) {
            position = getIntent().getExtras().getInt("index");
        }

    }

    private void initListeners() {
        sendCommentButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                comment();
                hideSoftKeyboard(CommentsActivity.this);
            }
        });
        backButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

    }

    private void comment() {
        Comment comment = createComment();
        Bundle bundle = getCommentBundle(comment);
        getLoaderManager().initLoader(LOADER_ID_COMMENT, bundle, CommentsActivity.this).forceLoad();
        commentEditText.getText().clear();
        commentEditText.clearFocus();

    }

    @Override
    public void onBackPressed() {
        settingResult();
        super.onBackPressed();
    }

    private void settingResult() {
        Bundle bundle2 = new Bundle();
        bundle2.putParcelable("selfie", selfieImage);
        Intent intent = new Intent();
        intent.putExtra("selfie", selfieImage);
        intent.putExtra("index", position);
        setResult(35, intent);
    }

    private Bundle getCommentBundle(Comment comment) {
        Bundle bundle = new Bundle();
        Command command = new Command(Command.ADD_COMMENT);
        command.setComment(comment);
        command.setSelfieImage(selfieImage);
        bundle.putParcelable(Constants.COMMAND, command);
        return bundle;
    }

    private Comment createComment() {
        Comment comment = new Comment();
        comment.setPostId(postId);
        comment.setUserId(userId);
        comment.setText(commentEditText.getText().toString());
        return comment;
    }

    private void initView() {
        listView = (ListView) findViewById(R.id.commentsListView);
        commentEditText = (EditText) findViewById(R.id.commentEditText);
        sendCommentButton = (Button) findViewById(R.id.commentButton);
        backButton = (Button) findViewById(R.id.commentsBack);
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
        selfieImage.setUserId(userId);
        command.setSelfieImage(selfieImage);

        budle.putParcelable(Command.BUNDLE_NAME, command);
        return budle;
    }

    private void initComments(Loader<StatusCode> loader) {
        selfieImage = ((CommandLoader) loader).getCommentSelfieImage();
        commentsList = selfieImage.getComments();
        adapter = new CommentAdapter(this, R.layout.comment_item, commentsList);
        listView.setAdapter(adapter);
    }

    private static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }
}
