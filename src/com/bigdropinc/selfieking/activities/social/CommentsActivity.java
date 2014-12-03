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
import android.widget.Button;
import android.widget.EditText;
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
    private EditText commentEditText;
    private Button sendCommentButton;
    private Context previousActivity;
    private int position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        setContentView(R.layout.activity_comments);
        initView();
        initListeners();
        postId = getIntent().getExtras().getInt("postId");
        position = getIntent().getExtras().getInt("position");
        previousActivity = getIntent().getExtras().getParcelable("previous");
        if (InternetChecker.isNetworkConnected()) {
            startLoader();
        } else {
            InternetChecker.showNotInternetError(this);
        }

    }

    private void initListeners() {
        sendCommentButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                comment();
                
            }
        });

    }

    private void comment() {

        Comment comment = createComment();

        Intent data = new Intent();
        Bundle bundle = new Bundle();
        bundle.putParcelable("comment", comment);
        bundle.putInt("position", position);
        data.putExtras(bundle);
        CommentsActivity.this.setResult(10, data);
        CommentsActivity.this.finish();
        // if (previousActivity instanceof OneSelfieActivity) {
        // ((OneSelfieActivity) ac.).comment(comment);
        // } else if (previousActivity instanceof MyActionBarActivity) {
        // ((MyActionBarActivity) getCallingActivity()).comment(comment,
        // position);
        // }
    }

    private Comment createComment() {
        Comment comment = new Comment();
        comment.setPostId(postId);
        comment.setText(commentEditText.getText().toString());
        return comment;
    }

    private void initView() {
        listView = (ListView) findViewById(R.id.commentsListView);
        commentEditText = (EditText) findViewById(R.id.commentEditText);
        sendCommentButton = (Button) findViewById(R.id.commentButton);

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
