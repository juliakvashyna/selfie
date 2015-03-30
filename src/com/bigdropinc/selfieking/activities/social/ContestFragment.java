package com.bigdropinc.selfieking.activities.social;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import android.app.Fragment;
import android.app.LoaderManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.Loader;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.ViewSwitcher;

import com.bigdropinc.selfieking.R;
import com.bigdropinc.selfieking.activities.editimages.CropActivity;
import com.bigdropinc.selfieking.adapters.ViewPagerAdapter;
import com.bigdropinc.selfieking.adapters.ViewPagerItem;

import com.bigdropinc.selfieking.controller.loaders.Command;
import com.bigdropinc.selfieking.controller.loaders.CommandLoader;
import com.bigdropinc.selfieking.controller.loaders.Constants;
import com.bigdropinc.selfieking.model.responce.ResponseListSelfie;
import com.bigdropinc.selfieking.model.responce.StatusCode;
import com.bigdropinc.selfieking.model.responce.Winner;
import com.bigdropinc.selfieking.model.selfie.Contest;
import com.bigdropinc.selfieking.model.selfie.SelfieImage;
import com.google.android.gms.internal.im;
import com.google.android.gms.internal.lm;

public class ContestFragment extends Fragment implements OnCheckedChangeListener, LoaderManager.LoaderCallbacks<StatusCode> {

    private static final String TAG = "tag";
    private View rootView;
    private ViewPager viewPager;
    private ViewSwitcher switcher;
    private RadioGroup segmentText;
    private CommandLoader loader;
    private List<ViewPagerItem> list = new ArrayList<ViewPagerItem>(12);
    private ViewPagerAdapter adapter;
    public Contest contest;
    private Calendar calendar = Calendar.getInstance();
    private ListView notificationsListView;
    private int LOADER_ID = 100;
    private int LOADER_ID_ORDER = 1000;
    private boolean end;
    private List<SelfieImage> more;
    private ProgressDialog dialog;
    private int monthNumber;
    private int page;
    public static boolean vote;

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        if (checkedId == R.id.button_one) {
            switcher.showNext();
        } else if (checkedId == R.id.button_two) {
            switcher.showPrevious();
        }
    }

    @Override
    public Loader<StatusCode> onCreateLoader(int arg0, Bundle args) {
        loader = new CommandLoader(getActivity(), args);
        Log.d(TAG, "onCreateLoader: " + loader.hashCode());
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<StatusCode> loader, StatusCode statusCode) {
        ResponseListSelfie responseListSelfie = ((CommandLoader) loader).getResponseListSelfie();
        monthNumber = ((CommandLoader) loader).getCommand().getContest().getMonth();
        monthNumber = monthNumber - 1;
        ViewPagerItem itemfromlist = list.get(monthNumber);
        more = (ArrayList<SelfieImage>) responseListSelfie.posts.list;
        if (loader.getId() > LOADER_ID_ORDER) {
            Log.d("contest", "LOADER_ID_ORDER");
            changeorder(responseListSelfie, itemfromlist);
        } else if (loader.getId() > LOADER_ID) {
            Log.d("contest", "LOADER_ID more");
            if (more.size() > 0) {
                addMore(itemfromlist);
            } else {
                end = true;
            }
        } else if (statusCode.isSuccess()) {
            Log.d("contest", "setSelfies");
            setSelfies(responseListSelfie, itemfromlist);
        }
        getLoaderManager().destroyLoader(loader.getId());
    }

    @Override
    public void onResume() {
        super.onResume();
        if (vote) {
            updateVote(monthNumber);
            vote = false;
        }
    }

    private void addMore(ViewPagerItem itemfromlist) {
        itemfromlist.addSelfies(more);

        // adapter.getImageAdapter().addAll(more);

   //      adapter.updateImageAdapter();
    //    adapter.getImageAdapter().setNotifyOnChange(true);
      //  adapter.getGridView().smoothScrollToPosition(page);
        adapter.notifyDataSetChanged();
        adapter.getGridView().smoothScrollByOffset(page);
        Log.d("contest", "page "+page);
  
    }

    private void changeorder(ResponseListSelfie responseListSelfie, ViewPagerItem itemfromlist) {
        end = false;
        itemfromlist.getSelfies().clear();
        itemfromlist.addSelfies(more);
        // if (more != null)                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           
        // ;
        adapter.updateImageAdapter();
        adapter.notifyDataSetChanged();
        adapter.getImageAdapter().setNotifyOnChange(true);

        // setSelfies(responseListSelfie, itemfromlist);
        if (dialog != null)
            dialog.cancel();
    }

    private void setSelfies(ResponseListSelfie responseListSelfie, ViewPagerItem itemfromlist) {
        itemfromlist.addSelfies(more);
        adapter.updateImageAdapter();
        itemfromlist.setVote(responseListSelfie.posts.vote);
        itemfromlist.setContest(contest);
        itemfromlist.setCount(responseListSelfie.posts.count);

        itemfromlist.setWinner(responseListSelfie.winner);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(Loader<StatusCode> arg0) {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_contest, container, false);
        switcher = (ViewSwitcher) rootView.findViewById(R.id.contestSwitcher);
        initPager();
        segmentText = (RadioGroup) rootView.findViewById(R.id.segment_text);
        segmentText.setOnCheckedChangeListener(this);
        notificationsListView = (ListView) rootView.findViewById(R.id.notificationsListView);
        return rootView;
    }

    private Bundle getContestBundle(Contest contest) {
        Bundle bundle = new Bundle();
        Command command = new Command(Command.GET_CONTEST);
        command.setContest(contest);
        contest.setOffset(0);
        bundle.putParcelable(Constants.COMMAND, command);
        return bundle;
    }

    private Bundle getContestBundle(Contest contest, int page) {
        Bundle bundle = new Bundle();
        Command command = new Command(Command.GET_CONTEST);
        contest.setOffset(page);
        command.setContest(contest);
        bundle.putParcelable(Constants.COMMAND, command);
        return bundle;
    }

    private void initPager() {
        viewPager = (ViewPager) rootView.findViewById(R.id.contestViewPager);
        initMonths();
        int current = Calendar.getInstance().get(Calendar.MONTH);
        viewPager.setOffscreenPageLimit(5);
        adapter = new ViewPagerAdapter(getActivity(), R.layout.contest_view_pager_item, list, this);
        // adapter.getImageAdapter().setNotifyOnChange(true);

        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(current);
        adapter.setmViewPager(viewPager);
    }

    private void initMonths() {
        ViewPagerItem item;
        int year = Calendar.getInstance().get(Calendar.YEAR);
        int currentMonth = Calendar.getInstance().get(Calendar.MONTH);
        String m;
        for (int i = 0; i <= currentMonth; i++) {
            initMonth(i + 1);
            item = new ViewPagerItem(i);
            item.setMonthNumber(i);
            item.setYear(year);
            item.setContest(contest);

            calendar.set(Calendar.MONTH, i);
            m = calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.US);
            item.setMonth(m);
            list.add(item);
        }
    }

    public void initMonth(int monthNumber) {
        contest = new Contest();
        contest.setYear(String.valueOf(calendar.get(Calendar.YEAR)));
        contest.setMonth(monthNumber);
        contest.setOrderDate();
        Bundle bundle = getContestBundle(contest);
        getLoaderManager().initLoader(monthNumber, bundle, this).forceLoad();
    }

    public void changeSort(Contest contest, int monthNumber) {
        // contest.setOrder();
        this.contest = contest;
        Bundle bundle = getContestBundle(contest);
        // Log.d("contest", contest.getOrder());
        // end = false;
        dialog = ProgressDialog.show(getActivity(), "", "");
        int id = LOADER_ID_ORDER + monthNumber;
        getLoaderManager().initLoader(id, bundle, this).forceLoad();

    }

    public void updateVote(int monthNumber) {
        Bundle bundle = getContestBundle(contest);
        getLoaderManager().initLoader(monthNumber, bundle, this).forceLoad();

    }

    public void loadMore(int monthNumber, int page) {
        this.page = page;
        if (!end) {
            // Log.d("contest", "load more page " + page + " monthNumber " +
            // monthNumber);
            Bundle bundle = getContestBundle(contest, page);
            getLoaderManager().initLoader(LOADER_ID + monthNumber + page, bundle, this).forceLoad();

        }
    }

}
