package com.bigdropinc.selfieking.model.selfie;

import android.os.Parcel;
import android.os.Parcelable;

public class Contest implements Parcelable {
    private String year;
    private int month;

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
}
