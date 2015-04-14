package com.bigdropinc.selfieking.adapters;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bigdropinc.selfieking.R;
import com.bigdropinc.selfieking.activities.profile.ProfileActivity;
import com.bigdropinc.selfieking.activities.social.ContestFragment;
import com.bigdropinc.selfieking.activities.social.OneSelfieActivity;
import com.bigdropinc.selfieking.controller.CustomPicasso;
import com.bigdropinc.selfieking.controller.UrlRequest;
import com.bigdropinc.selfieking.model.selfie.Contest;
import com.bigdropinc.selfieking.views.RoundedImageView;

public class ViewPagerAdapter extends PagerAdapter {

    private static final String TOP_RATED = "Top rated";
    private static final String MOST_RECENT = "Most recent";
    // Views that can be reused.
    private final List<View> mDiscardedViews = new ArrayList<View>();
    // Views that are already in use.
    private final SparseArray<View> mBindedViews = new SparseArray<View>();

    private final ArrayList<ViewPagerItem> mItems;
    private final LayoutInflater mInflator;
    private final int mResourceId;
    private final Activity context;
    private ViewPager mViewPager;
    private ContestFragment fragment;

    public ViewPagerAdapter(Activity context, int viewRes, List<ViewPagerItem> list, Fragment fragment) {
        this.mItems = (ArrayList<ViewPagerItem>) list;
        this.context = context;
        mInflator = LayoutInflater.from(context);
        mResourceId = viewRes;
        this.fragment = (ContestFragment) fragment;

    }

    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    public ViewPager getmViewPager() {
        return mViewPager;
    }

    public void setmViewPager(ViewPager mViewPager) {
        this.mViewPager = mViewPager;
    }

    @Override
    public int getCount() {
        if (mItems != null)
            return mItems.size();
        return 0;
    }

    @Override
    public boolean isViewFromObject(View v, Object obj) {
        return v == mBindedViews.get(mItems.indexOf(obj));
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        View view = mBindedViews.get(position);
        if (view != null) {
            mDiscardedViews.add(view);
            mBindedViews.remove(position);
            container.removeView(view);
        }
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View child = mDiscardedViews.isEmpty() ? mInflator.inflate(mResourceId, container, false) : mDiscardedViews.remove(0);
        ViewPagerItem data = mItems.get(position);
        initView(child, data, position);
        mBindedViews.append(position, child);
        container.addView(child, 0);
        return data;
    }

    public void add(ViewPagerItem item) {
        mItems.add(item);
    }

    public ViewPagerItem remove(int position) {
        return mItems.remove(position);
    }

    public void clear() {
        mItems.clear();
    }

    /**
     * Initiate the view here.
     */
    public void initView(View v, final ViewPagerItem item, int position) {
        TextView month = (TextView) v.findViewById(R.id.monthTextView);
        LinearLayout layout = (LinearLayout) v.findViewById(R.id.winnerLayout);
        LinearLayout monthlayout = (LinearLayout) v.findViewById(R.id.monthLayout);
        TextView topRecent = (TextView) v.findViewById(R.id.topRecentTextView);
        ImageButton topRecentImage = (ImageButton) v.findViewById(R.id.topRecentImageButton);
        initGridView(v, item);
        month.setText(item.getMonth() + ", " + item.getYear());
        if (item.getMonthNumber() <= Calendar.getInstance().get(Calendar.MONTH)) {
            layout.setVisibility(View.VISIBLE);
            monthlayout.setVisibility(View.GONE);
            topRecent.setVisibility(View.GONE);
            topRecentImage.setVisibility(View.GONE);
            if (item.getWinner() != null) {
                initWinner(v, item);
            }
        } else {
            topRecent.setVisibility(View.VISIBLE);
            topRecentImage.setVisibility(View.VISIBLE);
            layout.setVisibility(View.GONE);
            monthlayout.setVisibility(View.VISIBLE);
        }
        TextView count = (TextView) v.findViewById(R.id.count);
        TextView vote = (TextView) v.findViewById(R.id.votesLeft);
        initTopRecent(v, item);
        count.setText(String.valueOf(item.getCount()));
        vote.setText(String.valueOf(item.getVote()));

        initButtons(v, item);
    }

    private void initWinner(View v, final ViewPagerItem item) {
        TextView name = (TextView) v.findViewById(R.id.winnerName);
        TextView location = (TextView) v.findViewById(R.id.location);
        TextView role = (TextView) v.findViewById(R.id.winnerRole);
        TextView crowns = (TextView) v.findViewById(R.id.winnerCrowns);
        RoundedImageView avatar = (RoundedImageView) v.findViewById(R.id.avatarWinner);
        if (item.getWinner() != null) {
            name.setText(item.getWinner().getUserName());
            location.setText(item.getWinner().getLocation());
            // role.setText(item.getWinner().getRole());
            crowns.setText(String.valueOf(item.getWinner().getRate()));
            if (item.getWinner().getUserAvatar() != null && !item.getWinner().getUserAvatar().isEmpty()) {
                String url = UrlRequest.ADDRESS + item.getSelfies().get(0).getUserAvatar();
                CustomPicasso.getImageLoader(context).load(url).into(avatar);
            }
        }
        avatar.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoUserProfile(item.getWinner().getUserId());
            }
        });
    }

    protected void gotoUserProfile(int userId) {
        Intent intent = new Intent(context.getApplicationContext(), ProfileActivity.class);
        intent.putExtra("userId", userId);
        context.startActivity(intent);

    }

    private void changeTopRecent(final TextView topRecent, final ImageButton topRecentImage, ViewPagerItem item) {
        item.getContest().setOrder();
        changeTitle(topRecent, topRecentImage, item);
        item.mypage = 0;
        item.getSelfies().clear();
        fragment.changeSort(item.getContest(), item.getMonthNumber());

    }

    private void changeTitle(final TextView topRecent, final ImageButton topRecentImage, ViewPagerItem item) {
        String title = topRecent.getText().toString();

        if (item.getContest().getOrder().equals(Contest.rate_desc)) {
            topRecent.setText(TOP_RATED);
            topRecentImage.setImageResource(R.drawable.icon_view);

        } else {
            topRecent.setText(MOST_RECENT);
            topRecentImage.setImageResource(R.drawable.icon_calendar);

        }
        // item.getContest().setOrder();
    }

    private void changeTitle2(final TextView topRecent, final ImageButton topRecentImage, ViewPagerItem item) {
        String title = topRecent.getText().toString();
        if (title.equals(MOST_RECENT)) {
            topRecent.setText(TOP_RATED);
            topRecentImage.setImageResource(R.drawable.icon_view);

        } else {
            topRecent.setText(MOST_RECENT);
            topRecentImage.setImageResource(R.drawable.icon_calendar);

        }
    }

    private void initTopRecent(View v, final ViewPagerItem item) {
        final TextView topRecent = (TextView) v.findViewById(R.id.topRecentTextView);
        final ImageButton topRecentImage = (ImageButton) v.findViewById(R.id.topRecentImageButton);
        changeTitle(topRecent, topRecentImage, item);
        topRecent.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                changeTopRecent(topRecent, topRecentImage, item);
            }
        });
        topRecentImage.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                changeTopRecent(topRecent, topRecentImage, item);
            }
        });
    }

    private void initButtons(View v, ViewPagerItem item) {
        Button next = (Button) v.findViewById(R.id.nextMonth);

        Button back = (Button) v.findViewById(R.id.backMonth);
        if (item.getMonthNumber() == 1) {
            back.setVisibility(View.INVISIBLE);
        } else {
            back.setVisibility(View.VISIBLE);
        }
        if (item.getMonthNumber() == Calendar.getInstance().get(Calendar.MONTH) + 1) {
            next.setVisibility(View.INVISIBLE);
        } else {
            next.setVisibility(View.VISIBLE);
        }
        next.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mViewPager.setCurrentItem(getItem(+1), true);
            }
        });
        back.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mViewPager.setCurrentItem(getItem(-1), true);
            }
        });
    }

    private int getItem(int i) {
        return mViewPager.getCurrentItem() + i;
    }

    private void initGridView(View v, final ViewPagerItem item) {
        item.setGridView((GridView) v.findViewById(R.id.contestGridView));
        item.setImageAdapter(new ImageAdapter(context, R.layout.image_item_gridview, item.getSelfies()));
        item.getGridView().setAdapter(item.getImageAdapter());
        item.getGridView().setOnScrollListener(new EndlessScrollListener() {

            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                item.mypage = item.mypage + 9;
                fragment.loadMore(item);

            }
        });

        item.getGridView().setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                startOneActivity(item, position);
            }
        });
    }

    private void startOneActivity(ViewPagerItem item, int position) {
        int id = item.getSelfies().get(position).getId();
        if (id != 0) {
            Intent intent = new Intent(context, OneSelfieActivity.class);
            intent.putExtra("selfieId", id);
            intent.putExtra("monthNumber", item.getMonthNumber());
            intent.putExtra("order", item.getContest().getOrder());
            context.startActivityForResult(intent, 99);
        } else {
            Toast.makeText(context, "Post was deleted", Toast.LENGTH_SHORT).show();
        }
    }

}
