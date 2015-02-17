package com.bigdropinc.selfieking.activities.social;

import java.util.ArrayList;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
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
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
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

    private ArrayList<SelfieImage> feedList = new ArrayList<SelfieImage>();
    private FeedAdapter feedAdapter;
    private GridView gridView;
    ListView listView;
    private RelativeLayout title;
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
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<StatusCode> loader, StatusCode code) {
        if (code.isSuccess()) {
            more = (ArrayList<SelfieImage>) ((CommandLoader) loader).getSelfies();
             if (more.size() > 0) {
                feedList.addAll(more);
                feedAdapter.notifyDataSetChanged();
                adapter.notifyDataSetChanged();
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_feed, container, false);
        feedList.clear();
        init();
        return rootView;
    }

    void updateGridView(int index, SelfieImage selfieImage) {
        if (feedList.size() > 0) {
            feedList.set(index, selfieImage);
            feedAdapter.notifyDataSetChanged();
            adapter.notifyDataSetChanged();
        } else {
            Log.d("tag", "FeedFragment feedList.size()=0");
        }
    }

    private void init() {
        liked = checkFragment();
        String commandName = getCommandName();
        command = new Command(commandName, LoginManagerImpl.getInstance().getUser());
        if (InternetChecker.isNetworkConnected()) {
            initViews();
        } else {
            InternetChecker.showNotInternetError(getActivity());
        }
    }

    private String getCommandName() {
        return liked ? Command.GET_LIKED : Command.GET_SELFIES;
    }

    private boolean checkFragment() {
        return (getTag().equals(MyActionBarActivity.TAB_LIKED));
    }

    private void initViews() {
        listView = (ListView) rootView.findViewById(R.id.feedListView);
        gridView = (GridView) rootView.findViewById(R.id.likedGridView);
        tileButton = (Button) rootView.findViewById(R.id.tile);
        initListeners();
        if (liked) {
            title = (RelativeLayout) rootView.findViewById(R.id.likedLay);
        } else {
            title = (RelativeLayout) rootView.findViewById(R.id.feedLay);
        }
        initFeed();
        initGridview();
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
        SelfieImage selfie = (SelfieImage) parent.getItemAtPosition(position);
        Intent intent = new Intent(getActivity().getApplicationContext(), OneSelfieActivity.class);
        intent.putExtra(OneSelfieActivity.INTENT_SELFIE_ID, selfie.getId());
        getActivity().startActivity(intent);
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
        startLoader(0);
    }

    private void loadMore(int page) {
        if (!end) {
            startLoader(page);
        }
    }

    private void startLoader(int page) {
        command.setOffset(page);
        bundle.putParcelable(Command.BUNDLE_NAME, command);
        getLoaderManager().initLoader(LOADER_ID + page, bundle, FeedFragment.this).forceLoad();
    }

    private void initGridOrList() {
        if (!tile) {
            tileButton.setBackgroundResource(R.drawable.list_selector);
            gridView.setVisibility(View.VISIBLE);
            listView.setVisibility(View.INVISIBLE);
        } else {
            tileButton.setBackgroundResource(R.drawable.tile_selector);
            gridView.setVisibility(View.INVISIBLE);
            listView.setVisibility(View.VISIBLE);
        }
        tile = !tile;
    }



}
