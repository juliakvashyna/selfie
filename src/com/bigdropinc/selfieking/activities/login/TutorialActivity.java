package com.bigdropinc.selfieking.activities.login;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.bigdropinc.selfieking.R;
import com.bigdropinc.selfieking.R.id;
import com.bigdropinc.selfieking.R.layout;
import com.bigdropinc.selfieking.activities.social.MyActionBarActivity;
import com.bigdropinc.selfieking.adapters.TutorialViewPagerAdapter;
import com.bigdropinc.selfieking.adapters.TutorialViewPagerItem;
import com.bigdropinc.selfieking.adapters.ViewPagerAdapter;
import com.bigdropinc.selfieking.util.SystemUiHider;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
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

        viewPager = (ViewPager) findViewById(R.id.tutorialViewPager);
        list = new ArrayList<TutorialViewPagerItem>();
        TutorialViewPagerItem t1 = new TutorialViewPagerItem(1, R.layout.tutorial_item1);
        t1.setTitle(R.string.tutorialTitle1);
        TutorialViewPagerItem t2 = new TutorialViewPagerItem(2, R.layout.tutorial_item2);
        t2.setTitle(R.string.tutorialTitle2);
        TutorialViewPagerItem t3 = new TutorialViewPagerItem(3, R.layout.tutorial_item3);
        t3.setTitle(R.string.tutorialTitle3);
        list.add(t1);
        list.add(t2);
        list.add(t3);
        adapter = new TutorialViewPagerAdapter(this,  list);
        viewPager.setAdapter(adapter);
        adapter.setmViewPager(viewPager);
    }

    public void goSelectImage() {
        TutorialActivity.this.finish();
        Intent intent = new Intent(getApplicationContext(), MyActionBarActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("signup", true);
        startActivity(intent);
    }

}
