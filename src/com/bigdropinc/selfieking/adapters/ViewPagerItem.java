package com.bigdropinc.selfieking.adapters;

import java.util.List;

import com.bigdropinc.selfieking.model.selfie.SelfieImage;

public class ViewPagerItem {

    private int viewPagerItemId;
    private String month;
    private int year;

    private int monthNumber;
    private int count;
    private List<SelfieImage> selfies;

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
}
