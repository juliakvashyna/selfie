package com.bigdropinc.selfieking.adapters;

import java.util.ArrayList;
import java.util.List;

import android.widget.GridView;

import com.bigdropinc.selfieking.model.responce.Winner;
import com.bigdropinc.selfieking.model.selfie.Contest;
import com.bigdropinc.selfieking.model.selfie.SelfieImage;

public class TutorialViewPagerItem {

    int page;
    int layoutRes;
    int resTitle;

    public TutorialViewPagerItem(int page, int layoutRes) {
        super();
        this.page = page;
        this.layoutRes = layoutRes;
    }

    public int getTitle() {
        return resTitle;
    }

    public void setTitle(int title) {
        this.resTitle = title;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getLayoutRes() {
        return layoutRes;
    }

    public void setLayoutRes(int layoutRes) {
        this.layoutRes = layoutRes;
    }

}
