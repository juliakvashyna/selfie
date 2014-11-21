package com.bigdropinc.selfieking.activities.social;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Loader;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.ContextThemeWrapper;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.bigdropinc.selfieking.R;
import com.bigdropinc.selfieking.adapters.FeedAdapter;
import com.bigdropinc.selfieking.controller.loaders.Command;
import com.bigdropinc.selfieking.controller.loaders.CommandLoader;
import com.bigdropinc.selfieking.controller.loaders.Constants;
import com.bigdropinc.selfieking.controller.managers.login.LoginManagerImpl;
import com.bigdropinc.selfieking.model.responce.StatusCode;
import com.bigdropinc.selfieking.model.selfie.Comment;
import com.bigdropinc.selfieking.model.selfie.Like;
import com.bigdropinc.selfieking.model.selfie.SelfieImage;

public class OneSelfieActivity extends Activity implements LoaderManager.LoaderCallbacks<StatusCode> {
    public static final String INTENT_SELFIE_ID = "selfieId";
    private static final int LOADER_ID_ONESELFIE = 1;
    private ArrayList<SelfieImage> feedList;
    private FeedAdapter feedAdapter;
    private ListView listView;
    private SelfieImage selfieImage;
    private int LOADER_ID = 6;
    private int LOADER_ID_COMMENT = 7;
    private int LOADER_ID_LIKE = 8;
    private int LOADER_ID_DISLIKE = 9;
    private static final int LOADER_ID_CONTEST = 10;
    private CommandLoader loader;
    private Bundle bundle;
    private Command command;

    @Override
    public Loader<StatusCode> onCreateLoader(int id, Bundle args) {
        loader = new CommandLoader(this, args);
        Log.d("tag", "onCreateLoader: " + loader.hashCode());
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<StatusCode> loader, StatusCode statusCode) {
        if (loader.getId() == LOADER_ID_ONESELFIE) {
            initSelfie();
        } else {
            updateSelfie(loader, statusCode);
        }
        getLoaderManager().destroyLoader(loader.getId());
    }

    @Override
    public void onLoaderReset(Loader<StatusCode> arg0) {
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.selfie_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.delete:
            showPopup(R.string.deleteOneSelfie);
            break;
        default:
            return super.onContextItemSelected(item);
        }
        return super.onContextItemSelected(item);
    }

    public void comment(Comment comment) {
        Bundle bundle = getCommentBundle(comment);
        getLoaderManager().initLoader(LOADER_ID_COMMENT, bundle, OneSelfieActivity.this).forceLoad();
    }

    public void like(Like like, boolean liked) {
        Bundle bundle = getLiketBundle(like, liked);
        if (liked) {
            resetLoader(like, LOADER_ID_LIKE);
            getLoaderManager().initLoader(LOADER_ID_DISLIKE + like.getPostId(), bundle, OneSelfieActivity.this).forceLoad();
        } else {
            resetLoader(like, LOADER_ID_DISLIKE);
            getLoaderManager().initLoader(LOADER_ID_LIKE + like.getPostId(), bundle, OneSelfieActivity.this).forceLoad();
        }
    }

    public void contest(SelfieImage selfieImage) {
        Bundle bundle = getContestBundle(selfieImage);
        getLoaderManager().initLoader(LOADER_ID_CONTEST, bundle, OneSelfieActivity.this).forceLoad();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_one_selfie);
        startLoaderForSelfie();
        init();
        initFeed();

    }

    private void initSelfie() {
        selfieImage = ((CommandLoader) loader).getCommentSelfieImage();
        feedList.add(selfieImage);
        feedAdapter.notifyDataSetChanged();
    }

    private void startLoaderForSelfie() {
        bundle = new Bundle();
        command = new Command(Command.GET_SELFIE);
        int id = getIntent().getExtras().getInt(INTENT_SELFIE_ID);
        selfieImage = new SelfieImage(id);
        selfieImage.setToken(LoginManagerImpl.getInstance().getToken());
        command.setSelfieImage(selfieImage);
        bundle.putParcelable(Command.BUNDLE_NAME, command);
        getLoaderManager().initLoader(LOADER_ID_ONESELFIE, bundle, OneSelfieActivity.this).forceLoad();
    }

    private void updateSelfie(Loader<StatusCode> loader, StatusCode statusCode) {
        if (statusCode.isSuccess()) {
            if (loader.getId() == LOADER_ID_CONTEST) {
                Toast.makeText(this, "Post is added to contest", Toast.LENGTH_SHORT).show();
            } else {
                updateGridView(loader);
            }
        } else {
            Toast.makeText(this, statusCode.getError().get(0).errorMessage, Toast.LENGTH_SHORT).show();
        }
    }

    private void resetLoader(Like like, int id) {
        if (getLoaderManager().getLoader(id + like.getPostId()) != null)
            getLoaderManager().getLoader(id + like.getPostId()).reset();
    }

    private void updateGridView(Loader<StatusCode> loader) {
        feedList.remove(selfieImage);
        selfieImage = ((CommandLoader) loader).getSelfieImage();
        feedList.add(selfieImage);
        feedAdapter.notifyDataSetChanged();
    }

    private void init() {
        listView = (ListView) findViewById(R.id.oneSelfieListView);
        listView.setItemsCanFocus(true);
    }

    private void initFeed() {
        feedList = new ArrayList<SelfieImage>();
        feedAdapter = new FeedAdapter(this, R.layout.feed_item, feedList);
        listView.setAdapter(feedAdapter);

    }

    private void showPopup(int message) {
        AlertDialog.Builder popupBuilder = new AlertDialog.Builder(new ContextThemeWrapper(this, android.R.style.Theme_Holo_Dialog));
        popupBuilder.setMessage(message);
        popupBuilder.setPositiveButton("Delete", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                delete();
            }
        });
        popupBuilder.setNegativeButton("Cancel", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        popupBuilder.show();
    }

    private void delete() {
        Bundle bundle = new Bundle();
        Command command = new Command(Command.DELETE_SELFIE);
        selfieImage.setToken(LoginManagerImpl.getInstance().getToken());
        command.setSelfieImage(selfieImage);
        bundle.putParcelable("command", command);
        getLoaderManager().initLoader(LOADER_ID, bundle, OneSelfieActivity.this).forceLoad();
    }

    private Bundle getCommentBundle(Comment comment) {
        Bundle bundle = new Bundle();
        Command command = new Command(Command.ADD_COMMENT);
        command.setComment(comment);
        bundle.putParcelable(Constants.COMMAND, command);
        return bundle;
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

    private Bundle getContestBundle(SelfieImage selfieImage) {
        Bundle bundle = new Bundle();
        Command command = new Command(Command.ADD_CONTEST);
        command.setSelfieImage(selfieImage);
        bundle.putParcelable(Constants.COMMAND, command);
        return bundle;
    }
}
