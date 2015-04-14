package com.bigdropinc.selfieking.activities.login;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.bigdropinc.selfieking.R;
import com.bigdropinc.selfieking.R.id;
import com.bigdropinc.selfieking.R.layout;
import com.bigdropinc.selfieking.adapters.TutorialViewPagerAdapter;
import com.bigdropinc.selfieking.adapters.TutorialViewPagerItem;
import com.bigdropinc.selfieking.adapters.ViewPagerAdapter;
import com.bigdropinc.selfieking.util.SystemUiHider;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.view.MotionEvent;
import android.view.View;

public class TutorialActivity extends Activity {

    private ViewPager viewPager;
    private TutorialViewPagerAdapter adapter;
    private List<TutorialViewPagerItem> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_tutorial);

        viewPager = (ViewPager) findViewById(R.id.contestViewPager);
        list = new ArrayList<TutorialViewPagerItem>();
        list.add(new TutorialViewPagerItem(1, R.drawable.tutorial_1));
        list.add(new TutorialViewPagerItem(2, R.drawable.tutorial_2));
        list.add(new TutorialViewPagerItem(3, R.drawable.tutorial_3));
        adapter = new TutorialViewPagerAdapter(this, R.layout.tutorial_view_pager_item, list);
        viewPager.setAdapter(adapter);
        adapter.setmViewPager(viewPager);
    }

}
