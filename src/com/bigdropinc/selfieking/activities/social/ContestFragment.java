package com.bigdropinc.selfieking.activities.social;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Loader;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.ViewSwitcher;

import com.bigdropinc.selfieking.R;
import com.bigdropinc.selfieking.adapters.ViewPagerAdapter;
import com.bigdropinc.selfieking.adapters.ViewPagerItem;
import com.bigdropinc.selfieking.controller.loaders.Command;
import com.bigdropinc.selfieking.controller.loaders.CommandLoader;
import com.bigdropinc.selfieking.controller.loaders.Constants;
import com.bigdropinc.selfieking.model.responce.StatusCode;
import com.bigdropinc.selfieking.model.selfie.Contest;
import com.bigdropinc.selfieking.model.selfie.SelfieImage;

public class ContestFragment extends Fragment implements OnCheckedChangeListener, LoaderManager.LoaderCallbacks<StatusCode> {

    private static final String TAG = "tag";
    private View rootView;
    private ViewPager viewPager;
    private ViewSwitcher switcher;
    private RadioGroup segmentText;
    private List<SelfieImage> images;
    private CommandLoader loader;
    private List<ViewPagerItem> list = new ArrayList<ViewPagerItem>(12);
    private ViewPagerAdapter adapter;
    private Contest contest;
    private Calendar calendar = Calendar.getInstance();

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
        if (statusCode.isSuccess()) {
            images = (ArrayList<SelfieImage>) ((CommandLoader) loader).getSelfies();

            int monthNumber = ((CommandLoader) loader).getCommand().getContest().getMonth();
            monthNumber--;
            ViewPagerItem itemfromlist = list.get(monthNumber);
            itemfromlist.setSelfies(images);
            itemfromlist.setCount(images.size());
            list.set(monthNumber, itemfromlist);
            adapter.notifyDataSetChanged();
        }
        // } else {
        // Toast.makeText(getActivity(),
        // statusCode.getError().get(0).errorMessage,
        // Toast.LENGTH_SHORT).show();
        // }
        getLoaderManager().destroyLoader(loader.getId());
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
        return rootView;
    }

    private Bundle getContestBundle(Contest contest) {
        Bundle bundle = new Bundle();
        Command command = new Command(Command.GET_CONTEST);
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
        viewPager.setAdapter(adapter);
      
        viewPager.setCurrentItem(current);
        adapter.setmViewPager(viewPager);
    }

    private void initMonths() {
        ViewPagerItem item;
        String m;
        for (int i = 0; i <= 11; i++) {
            initMonth(i);
            item = new ViewPagerItem(i);
            item.setMonthNumber(i);
            // item.setSelfies(new ArrayList<SelfieImage>());
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
        Bundle bundle = getContestBundle(contest);
        getLoaderManager().initLoader(monthNumber, bundle, this).forceLoad();
    }

}
