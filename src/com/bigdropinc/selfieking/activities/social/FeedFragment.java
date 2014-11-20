package com.bigdropinc.selfieking.activities.social;

import java.util.ArrayList;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Loader;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.bigdropinc.selfieking.R;
import com.bigdropinc.selfieking.adapters.EndlessScrollListener;
import com.bigdropinc.selfieking.adapters.FeedAdapter;
import com.bigdropinc.selfieking.adapters.ImageAdapter;
import com.bigdropinc.selfieking.controller.InternetChecker;
import com.bigdropinc.selfieking.controller.loaders.Command;
import com.bigdropinc.selfieking.controller.loaders.CommandLoader;
import com.bigdropinc.selfieking.controller.managers.login.LoginManagerImpl;
import com.bigdropinc.selfieking.model.responce.StatusCode;
import com.bigdropinc.selfieking.model.selfie.SelfieImage;

public class FeedFragment extends Fragment implements LoaderManager.LoaderCallbacks<StatusCode> {

    public static final int LIMIT = EndlessScrollListener.VISIBLETHRESHOLD;
    private static final int LOADER_ID = 5;
    private static final String TAG = "tag";
    private static final String TAG_PAGE = "tagPage";
    private ArrayList<SelfieImage> feedList = new ArrayList<SelfieImage>();
    private FeedAdapter feedAdapter;
    private GridView gridView;
    private ListView listView;
    private LinearLayout title;
    private CommandLoader loader;
    private View rootView;
    private Bundle bundle = new Bundle();
    private Command command;
    private ArrayList<SelfieImage> more;
    public static int index;
    private boolean liked;
    private ImageAdapter adapter;
    private Button tileButton;
    private boolean tile;
    private boolean end;

    @Override
    public Loader<StatusCode> onCreateLoader(int id, Bundle args) {
        loader = new CommandLoader(getActivity(), args);
        Log.d(TAG, "onCreateLoader: " + loader.hashCode());
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<StatusCode> loader, StatusCode code) {
        if (code.isSuccess()) {
            Log.d(TAG, "onLoadFinished: " + loader.hashCode());
            more = (ArrayList<SelfieImage>) ((CommandLoader) loader).getSelfies();
            if (more.size() > 0) {
                feedList.addAll(more);
                if (liked) {
                    adapter.notifyDataSetChanged();
                } else {
                    feedAdapter.notifyDataSetChanged();
                }
            } else {
                end = true;
            }

        } else {
            Toast.makeText(getActivity(), code.getError().get(0).errorMessage, Toast.LENGTH_SHORT).show();
        }
        getLoaderManager().destroyLoader(loader.getId());
    }

    @Override
    public void onLoaderReset(Loader<StatusCode> loader) {
        Log.d(TAG, "onLoaderReset: " + loader.hashCode());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_feed, container, false);
        String commandName = getCommandName();
        command = new Command(commandName, LoginManagerImpl.getInstance().getUser());
        if (InternetChecker.isNetworkConnected()) {
            init();
        } else {
            InternetChecker.showNotInternetError(getActivity());
        }
        return rootView;
    }

    void updateGridView(int index, SelfieImage selfieImage) {
        feedList.set(index, selfieImage);
        feedAdapter.notifyDataSetChanged();
    }

    private String getCommandName() {
        return liked ? Command.GET_LIKED : Command.GET_SELFIES;
    }

    private boolean checkFragment() {
        return (getTag().equals(MyActionBarActivity.TAB_LIKED));
    }

    private void init() {
        listView = (ListView) rootView.findViewById(R.id.feedListView);
        gridView = (GridView) rootView.findViewById(R.id.likedGridView);
        tileButton = (Button) rootView.findViewById(R.id.tile);
        initListeners();
        liked = checkFragment();
        if (liked) {
            title = (LinearLayout) rootView.findViewById(R.id.likedLay);
        } else {
            title = (LinearLayout) rootView.findViewById(R.id.feedLay);

        }
        initFeed();
        initGridview();
        listView.setVisibility(View.VISIBLE);
        title.setVisibility(View.VISIBLE);
    }

    private void initListeners() {
        tileButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                initGridOrList();
            }
        });
    }

    private void initGridview() {
        adapter = new ImageAdapter(getActivity(), R.layout.image_item_gridview, feedList);
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

    private void startOneSelfieActivity(AdapterView<?> parent, int position) {
        // TODO Auto-generated method stub

    }

    private void initFeed() {
        feedAdapter = new FeedAdapter(getActivity(), R.layout.feed_item, feedList);

        listView.setAdapter(feedAdapter);
        listView.setOnScrollListener(new EndlessScrollListener() {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                loadMore(page);
            }
        });
        command.setOffset(0);
        Log.d(TAG_PAGE, "offset =" + 0);
        bundle.putParcelable("command", command);
        getLoaderManager().initLoader(LOADER_ID, bundle, FeedFragment.this).forceLoad();
    }

    private void loadMore(int page) {
        if (!end) {
            Log.d(TAG_PAGE, "offset =" + page);
            command.setOffset(page);
            bundle.putParcelable("command", command);
            getLoaderManager().initLoader(LOADER_ID + page, bundle, FeedFragment.this).forceLoad();
        }
    }

    private void initGridOrList() {
        if (!tile) {
            gridView.setVisibility(View.VISIBLE);
            listView.setVisibility(View.INVISIBLE);

        } else {
            listView.setVisibility(View.VISIBLE);
            gridView.setVisibility(View.INVISIBLE);
        }
        tile = !tile;
    }

}