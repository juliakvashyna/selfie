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
import android.widget.Toast;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.ViewSwitcher;

import com.bigdropinc.selfieking.R;
import com.bigdropinc.selfieking.activities.editimages.CropActivity;
import com.bigdropinc.selfieking.adapters.CommentAdapter;
import com.bigdropinc.selfieking.adapters.EndlessScrollListener;
import com.bigdropinc.selfieking.adapters.NotificationtAdapter;
import com.bigdropinc.selfieking.adapters.ViewPagerAdapter;
import com.bigdropinc.selfieking.adapters.ViewPagerItem;

import com.bigdropinc.selfieking.controller.loaders.Command;
import com.bigdropinc.selfieking.controller.loaders.CommandLoader;
import com.bigdropinc.selfieking.controller.loaders.Constants;
import com.bigdropinc.selfieking.model.responce.ResponseListSelfie;
import com.bigdropinc.selfieking.model.responce.StatusCode;
import com.bigdropinc.selfieking.model.responce.Winner;
import com.bigdropinc.selfieking.model.responce.notification.Notification;
import com.bigdropinc.selfieking.model.selfie.Contest;
import com.bigdropinc.selfieking.model.selfie.SelfieImage;
import com.google.android.gms.internal.im;
import com.google.android.gms.internal.lm;
import com.google.android.gms.internal.mo;

public class ContestFragment extends Fragment implements OnCheckedChangeListener, LoaderManager.LoaderCallbacks<StatusCode> {

    private static final String TAG = "tag";
    private View rootView;
    private ViewPager viewPager;
    private ViewSwitcher switcher;
    private RadioGroup segmentText;
    private CommandLoader loader;
    public List<ViewPagerItem> list = new ArrayList<ViewPagerItem>(12);
    private List<Notification> notificationsList = new ArrayList<Notification>(30);
    private ViewPagerAdapter adapter;
    private Calendar calendar = Calendar.getInstance();
    private ListView notificationsListView;
    private int LOADER_ID = 100;
    private int LOADER_ID_NOTIFICATIONS = 25;
    private int LOADER_ID_ORDER = 1000;
    private int LOADER_ID_VOTE = 35;
    private ProgressDialog dialog;
    private int monthNumber;
    public static int curMonthNumber;
    public static String curOrder;
    private String order;
    NotificationtAdapter notificationtAdapter;
    public static boolean vote;
    private boolean was;
    int checkedId;

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        this.checkedId = checkedId;
        if (checkedId == R.id.button_one) {
            switcher.showNext();
        } else if (checkedId == R.id.button_two) {
            switcher.showPrevious();
            if (notificationtAdapter != null && notificationtAdapter.getCount() == 0) {
                Toast.makeText(getActivity(), "There is no information yet", Toast.LENGTH_LONG).show();
            }
        } 
    }

    @Override
    public void onResume() {
        super.onResume();
        if (vote) {
            Contest contest = getContest(curMonthNumber, curOrder);
            Bundle bundle = getContestBundle(contest, 0);
            getLoaderManager().initLoader(LOADER_ID_VOTE, bundle, this).forceLoad();
            vote = false;
        }
    }

    @Override
    public Loader<StatusCode> onCreateLoader(int arg0, Bundle args) {
        loader = new CommandLoader(getActivity(), args);
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<StatusCode> loader, StatusCode statusCode) {
        was = true;
        if (loader.getId() == LOADER_ID_NOTIFICATIONS) {
            showNotifications(loader, statusCode);
        } else if (loader.getId() == LOADER_ID_VOTE) {
            updateAfterVote(loader);
        } else {
            showContest(loader, statusCode);
        }
        getLoaderManager().destroyLoader(loader.getId());
    }

    private void updateAfterVote(Loader<StatusCode> loader) {
        ResponseListSelfie responseListSelfie = ((CommandLoader) loader).getResponseListSelfie();
        if (responseListSelfie != null && responseListSelfie.posts != null) {
            Contest contest = getContest(monthNumber, ((CommandLoader) loader).getCommand().getContest().getOrder());
            int i = monthNumber - 1;
            if (i >= 0 && i < list.size()) {
                ViewPagerItem itemfromlist = list.get(i);
                itemfromlist.mypage = 0;
                ArrayList<SelfieImage> more = (ArrayList<SelfieImage>) responseListSelfie.posts.list;
                itemfromlist.setContest(contest);
                itemfromlist.end = false;
                itemfromlist.getSelfies().clear();
                itemfromlist.setSelfies(more);
                itemfromlist.setVote(responseListSelfie.posts.vote);
                adapter.notifyDataSetChanged();
            }

        }
    }

    @Override
    public void onLoaderReset(Loader<StatusCode> arg0) {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_contest, container, false);
        switcher = (ViewSwitcher) rootView.findViewById(R.id.contestSwitcher);
        if (!was)
            initPager();
        segmentText = (RadioGroup) rootView.findViewById(R.id.segment_text);
        segmentText.setOnCheckedChangeListener(this);
        notificationsListView = (ListView) rootView.findViewById(R.id.notificationsListView);
        initNotifications();
        return rootView;
    }

    public void changeSort(Contest contest, int monthNumber) {
        contest.setMonth(monthNumber);
        Bundle bundle = getContestBundle(contest);
        dialog = ProgressDialog.show(getActivity(), "", "");
        int id = LOADER_ID_ORDER + monthNumber;
        getLoaderManager().initLoader(id, bundle, this).forceLoad();
    }

    public void loadMore(ViewPagerItem item) {

        Log.d("contest", "loadMore page " + item.mypage);
        if (!item.end && item.getSelfies().size() < item.getCount()) {

            Contest contest = getContest(item.getMonthNumber(), item.getContest().getOrder());
            Bundle bundle = getContestBundle(contest, item.mypage);
            getLoaderManager().initLoader(LOADER_ID + item.getMonthNumber() + item.mypage, bundle, this).forceLoad();

        }
    }

    private void initNotifications() {
        if (notificationsList.size() == 0) {
            notificationtAdapter = new NotificationtAdapter(getActivity(), R.layout.notification_item, notificationsList);
            notificationsListView.setAdapter(notificationtAdapter);
            notificationsListView.setOnScrollListener(new EndlessScrollListener() {
                @Override
                public void onLoadMore(int page, int totalItemsCount) {
                    startNotifications(page);
                }
            });
            startNotifications(0);
        }
    }

    private void startNotifications(int ofset) {
        Bundle bundle = new Bundle();
        Command command = new Command(Command.GET_NOTIFICATIONS);
        command.setOffset(ofset);
        bundle.putParcelable(Constants.COMMAND, command);
        getLoaderManager().initLoader(LOADER_ID_NOTIFICATIONS, bundle, this).forceLoad();
    }

    private void showContest(Loader<StatusCode> loader, StatusCode statusCode) {
        ResponseListSelfie responseListSelfie = ((CommandLoader) loader).getResponseListSelfie();
        int monthNumber = ((CommandLoader) loader).getCommand().getContest().getMonth();
        Contest contest = getContest(monthNumber, ((CommandLoader) loader).getCommand().getContest().getOrder());
        int i = monthNumber - 1;
        if (i >= 0 && i < list.size()) {
            ViewPagerItem itemfromlist = list.get(i);
            itemfromlist.setContest(contest);

            if (responseListSelfie != null) {
                ArrayList<SelfieImage> more = (ArrayList<SelfieImage>) responseListSelfie.posts.list;
                if (loader.getId() > LOADER_ID_ORDER) {
                    Log.d("contest", "order");
                    changeorder(responseListSelfie, itemfromlist, more);
                } else if (loader.getId() > LOADER_ID) {
                    addMoreOrEnd(itemfromlist, more);
                } else if (statusCode.isSuccess()) {
                    setSelfies(responseListSelfie, itemfromlist, contest);
                }
            }
            this.monthNumber = monthNumber;
            this.order = contest.getOrder();
        }
    }

    private void showNotifications(Loader<StatusCode> loader, StatusCode statusCode) {
        notificationsList = ((CommandLoader) loader).getNotifications();
        if (notificationsList != null) {
            notificationtAdapter.addAll(notificationsList);
            notificationtAdapter.notifyDataSetChanged();
        }

    }

    private void addMoreOrEnd(ViewPagerItem itemfromlist, ArrayList<SelfieImage> more) {
        if (more.size() > 0) {
            addMore(itemfromlist, more);
            Log.d("contest", "addMore " + itemfromlist.getMonth() + "list size " + itemfromlist.getSelfies().size());
        } else {
            itemfromlist.end = true;
            Log.d("contest", "end true " + itemfromlist.getMonth());
        }

    }

    private void addMore(ViewPagerItem itemfromlist, List<SelfieImage> more) {
        itemfromlist.addSelfies(more);
    }

    private void changeorder(ResponseListSelfie responseListSelfie, ViewPagerItem itemfromlist, List<SelfieImage> more) {
        itemfromlist.end = false;
        itemfromlist.getSelfies().clear();
        itemfromlist.setSelfies(more);
        adapter.notifyDataSetChanged();
        if (dialog != null)
            dialog.cancel();
    }

    private void setSelfies(ResponseListSelfie responseListSelfie, ViewPagerItem itemfromlist, Contest contest) {
        if (responseListSelfie != null && responseListSelfie.posts != null && itemfromlist != null) {
            itemfromlist.setSelfies(responseListSelfie.posts.list);
            itemfromlist.setVote(responseListSelfie.posts.vote);
            itemfromlist.setContest(contest);

            itemfromlist.setCount(responseListSelfie.posts.count);
            itemfromlist.setWinner(responseListSelfie.winner);
            adapter.notifyDataSetChanged();
        }
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

    public void initPager() {
        viewPager = (ViewPager) rootView.findViewById(R.id.contestViewPager);
        initMonths();
        int current = Calendar.getInstance().get(Calendar.MONTH);
        viewPager.setOffscreenPageLimit(1);
        adapter = new ViewPagerAdapter(getActivity(), R.layout.contest_view_pager_item, list, this);
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(current);
        adapter.setmViewPager(viewPager);
    }

    private void initMonths() {

        ViewPagerItem item;
        int year = Calendar.getInstance().get(Calendar.YEAR);
        int currentMonth = Calendar.getInstance().get(Calendar.MONTH);
        list = new ArrayList<ViewPagerItem>();
        String order;
        String m;
        for (int i = 0; i < currentMonth; i++) {
            item = new ViewPagerItem(i);
            order = Contest.rate_desc;
            item.setContest(initMonth(i + 1, order));
            item.setMonthNumber(i + 1);
            item.setYear(year);
            // item.setContest(getContest(i, order));
            calendar.set(Calendar.MONTH, i);
            m = calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.US);
            item.setMonth(m);
            list.add(item);
        }
        item = new ViewPagerItem(currentMonth);
        order = Contest.contest_date;
        item.setContest(initMonth(currentMonth + 1, order));
        item.setMonthNumber(currentMonth + 1);
        item.setYear(year);
        calendar.set(Calendar.MONTH, currentMonth);
        m = calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.US);
        item.setMonth(m);
        list.add(item);
        // was = true;
    }

    private Contest initMonth(int monthNumber, String order) {

        Contest contest = getContest(monthNumber, order);
        Bundle bundle = getContestBundle(contest);
        getLoaderManager().initLoader(monthNumber, bundle, this).forceLoad();
        return contest;
    }

    private Contest getContest(int monthNumber, String order) {
        Contest contest = new Contest();
        contest.setYear(String.valueOf(calendar.get(Calendar.YEAR)));
        contest.setMonth(monthNumber);
        contest.setOrder(order);
        return contest;
    }

    private void updateVote(int monthNumber) {
        // monthNumber = monthNumber + 1;
        Bundle bundle = getContestBundle(getContest(monthNumber, order));
        getLoaderManager().initLoader(monthNumber, bundle, this).forceLoad();

    }

}
