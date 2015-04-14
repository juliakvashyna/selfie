package com.bigdropinc.selfieking.adapters;

import java.util.ArrayList;
import java.util.List;

import android.widget.GridView;

import com.bigdropinc.selfieking.model.responce.Winner;
import com.bigdropinc.selfieking.model.selfie.Contest;
import com.bigdropinc.selfieking.model.selfie.SelfieImage;

public class TutorialViewPagerItem {
    public TutorialViewPagerItem(int page, int imageRes) {
        super();
        this.page = page;
        this.imageRes = imageRes;
    }

    int page;
    int imageRes;

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getImageRes() {
        return imageRes;
    }

    public void setImageRes(int imageRes) {
        this.imageRes = imageRes;
    }

}
