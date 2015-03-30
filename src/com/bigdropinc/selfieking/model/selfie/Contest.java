package com.bigdropinc.selfieking.model.selfie;

import android.os.Parcel;
import android.os.Parcelable;

public class Contest implements Parcelable {
    public static final String contest_date = "contest_date desc";
    public static final String rate_desc = "rate desc";
    private String year;
    private int month;
    private String order;
    private int offset;

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(year);
        dest.writeInt(month);
    }

    public static final Parcelable.Creator<Contest> CREATOR = new Parcelable.Creator<Contest>() {
        public Contest createFromParcel(Parcel in) {
            return new Contest(in);
        }

        public Contest[] newArray(int size) {
            return new Contest[size];
        }
    };

    private Contest(Parcel in) {

        year = in.readString();
        month = in.readInt();
    }

    public Contest() {
        // TODO Auto-generated constructor stub
    }

    @Override
    public int describeContents() {
        // TODO Auto-generated method stub
        return 0;
    }

    public String getOrder() {
        return order;
    }

    public void setOrderDate() {
        this.order = contest_date;
    }

    public void setOrderRate() {
        this.order = rate_desc;
    }

    public void setOrder() {
        if (order.equals(contest_date)) {
            setOrderRate();
        } else
            setOrderDate();
        
    }

    public void setOrder(String order) {
        this.order = order;

    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }
}
