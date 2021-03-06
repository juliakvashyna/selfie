package com.bigdropinc.selfieking.adapters;

import java.util.ArrayList;
import java.util.List;

import android.widget.GridView;

import com.bigdropinc.selfieking.model.responce.Winner;
import com.bigdropinc.selfieking.model.selfie.Contest;
import com.bigdropinc.selfieking.model.selfie.SelfieImage;

public class ViewPagerItem {

    private int viewPagerItemId;
    private String month;
    private int year;
    private int vote;
    private Contest contest;
    private int monthNumber;
    private int count;
    private Winner winner;
    public boolean end;
    public int mypage = 0;
    private List<SelfieImage> selfies = new ArrayList<SelfieImage>();
    GridView gridView;
    ImageAdapter imageAdapter;

    public ImageAdapter getImageAdapter() {
        return imageAdapter;
    }

    public void setImageAdapter(ImageAdapter imageAdapter) {
        this.imageAdapter = imageAdapter;
    }

    public ViewPagerItem(int selfieId) {
        super();
        this.viewPagerItemId = selfieId;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public List<SelfieImage> getSelfies() {
        return selfies;
    }

    public void setSelfies(List<SelfieImage> selfies) {
        this.selfies = selfies;
    }

    public void addSelfies(List<SelfieImage> selfies) {
        if (selfies != null && selfies.size() > 0) {
            if (!this.selfies.containsAll(selfies)) {
                this.selfies.addAll(selfies);
                updateImageAdapter();
            } else {
                end = true;
            }
        }

    }

    public GridView getGridView() {
        return gridView;
    }

    public void setGridView(GridView gridView) {
        this.gridView = gridView;
    }

    public int getSelfieId() {
        return viewPagerItemId;
    }

    public void setSelfieId(int selfieId) {
        this.viewPagerItemId = selfieId;
    }

    public int getMonthNumber() {
        return monthNumber;
    }

    public void setMonthNumber(int monthNumber) {
        this.monthNumber = monthNumber;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public Winner getWinner() {
        return winner;
    }

    public void setWinner(Winner winner) {
        this.winner = winner;
    }

    public Contest getContest() {
        return contest;
    }

    public void setContest(Contest contest) {
        this.contest = contest;
    }

    public int getVote() {
        return vote;
    }

    public void setVote(int vote) {
        this.vote = vote;
    }

    private void updateImageAdapter() {
        if (imageAdapter != null) {
            // imageAdapter.setNotifyOnChange(false);
            imageAdapter.setImages((selfies));
            imageAdapter.notifyDataSetChanged();
        }
    }
}
