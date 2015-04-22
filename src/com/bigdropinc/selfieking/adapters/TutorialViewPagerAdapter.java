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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bigdropinc.selfieking.R;
import com.bigdropinc.selfieking.activities.login.TutorialActivity;
import com.bigdropinc.selfieking.activities.profile.ProfileActivity;
import com.bigdropinc.selfieking.activities.social.ContestFragment;
import com.bigdropinc.selfieking.activities.social.OneSelfieActivity;
import com.bigdropinc.selfieking.controller.CustomPicasso;
import com.bigdropinc.selfieking.controller.UrlRequest;
import com.bigdropinc.selfieking.model.selfie.Contest;
import com.bigdropinc.selfieking.views.RoundedImageView;

public class TutorialViewPagerAdapter extends PagerAdapter {

    // Views that can be reused.
    private final List<View> mDiscardedViews = new ArrayList<View>();
    // Views that are already in use.
    private final SparseArray<View> mBindedViews = new SparseArray<View>();

    private final ArrayList<TutorialViewPagerItem> mItems;
    private final LayoutInflater mInflator;
    private final TutorialActivity context;
    private ViewPager mViewPager;

    public TutorialViewPagerAdapter(Activity context, List<TutorialViewPagerItem> list) {
        this.mItems = (ArrayList<TutorialViewPagerItem>) list;
        this.context = (TutorialActivity) context;
        mInflator = LayoutInflater.from(context);

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
        View child = mDiscardedViews.isEmpty() ? mInflator.inflate(mItems.get(position).getLayoutRes(), container, false) : mDiscardedViews.remove(0);
        TutorialViewPagerItem data = mItems.get(position);
        initView(child, data);
        mBindedViews.append(position, child);
        container.addView(child, 0);
        return data;
    }

    public void add(TutorialViewPagerItem item) {
        mItems.add(item);
    }

    public TutorialViewPagerItem remove(int position) {
        return mItems.remove(position);
    }

    public void clear() {
        mItems.clear();
    }

    /**
     * Initiate the view here.
     */
    public void initView(View v, final TutorialViewPagerItem item) {
        Button bt = (Button) v.findViewById(R.id.getStarted);
        if (bt != null) {
            bt.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    context.goSelectImage();
                }
            });
        }

    }

}
