package com.bigdropinc.selfieking.adapters;

import java.util.ArrayList;
import java.util.List;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.bigdropinc.selfieking.R;
import com.bigdropinc.selfieking.activities.social.ContestFragment;
import com.bigdropinc.selfieking.activities.social.OneSelfieActivity;

public class ViewPagerAdapter extends PagerAdapter {

    // Views that can be reused.
    private final List<View> mDiscardedViews = new ArrayList<View>();
    // Views that are already in use.
    private final SparseArray<View> mBindedViews = new SparseArray<View>();

    private final ArrayList<ViewPagerItem> mItems;
    private final LayoutInflater mInflator;
    private final int mResourceId;
    private final Context context;
    private ViewPager mViewPager;
    private ContestFragment fragment;

    public ViewPagerAdapter(Context context, int viewRes, List<ViewPagerItem> list, Fragment fragment) {
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
    public void initView(View v, ViewPagerItem item, int position) {
        TextView month = (TextView) v.findViewById(R.id.monthTextView);
        month.setText(item.getMonth()+", "+item.getYear());
        TextView count = (TextView) v.findViewById(R.id.count);
        count.setText(String.valueOf(item.getCount()));
        initGridView(v, item);
        initButtons(v);
    }

    private void initButtons(View v) {
        Button next = (Button) v.findViewById(R.id.nextMonth);
        Button back = (Button) v.findViewById(R.id.backMonth);
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
        GridView gridView = (GridView) v.findViewById(R.id.contestGridView);
        gridView.setAdapter(new ImageAdapter(context, R.layout.image_item_gridview, item.getSelfies()));
        gridView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                startOneActivity(item.getSelfies().get(position).getId());
            }
        });
    }

    private void startOneActivity(int id) {
        if (id != 0) {
            Intent intent = new Intent(context, OneSelfieActivity.class);
            intent.putExtra("selfieId", id);
            context.startActivity(intent);
        } else {
            Toast.makeText(context, "Post was deleted", Toast.LENGTH_SHORT).show();
        }
    }
}
