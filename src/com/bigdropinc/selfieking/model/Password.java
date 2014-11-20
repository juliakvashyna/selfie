package com.bigdropinc.selfieking.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Password implements Parcelable {
    private String old;
    private String newPassword;

    public String getOld() {
        return old;
    }

    public void setOld(String old) {
        this.old = old;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public Password() {
    }

    @Override
    public int describeContents() {

        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(old);
        dest.writeString(newPassword);

    }

    public static final Parcelable.Creator<Password> CREATOR = new Parcelable.Creator<Password>() {
        public Password createFromParcel(Parcel in) {
            return new Password(in);
        }

        public Password[] newArray(int size) {
            return new Password[size];
        }
    };

    private Password(Parcel in) {
        old = in.readString();
        newPassword = in.readString();
    }
}
