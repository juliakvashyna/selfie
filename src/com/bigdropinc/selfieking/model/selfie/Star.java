package com.bigdropinc.selfieking.model.selfie;

import android.os.Parcel;
import android.os.Parcelable;

public class Star implements Parcelable {
    int my;
    int total;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(my);
        dest.writeInt(total);
    }

    public static final Parcelable.Creator<Star> CREATOR = new Parcelable.Creator<Star>() {
        public Star createFromParcel(Parcel in) {
            return new Star(in);
        }

        public Star[] newArray(int size) {
            return new Star[size];
        }
    };

    private Star(Parcel in) {
        my = in.readInt();

        total = in.readInt();
    }

    public Star() {
        super();
        // TODO Auto-generated constructor stub
    }

    public int getMy() {
        return my;
    }

    public void setMy(int my) {
        this.my = my;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

}
