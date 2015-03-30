package com.bigdropinc.selfieking.model.selfie;

import android.os.Parcel;
import android.os.Parcelable;

public class Vote implements Parcelable {
    public int getPostId() {
        return postId;
    }

    public void setPostId(int postId) {
        this.postId = postId;
    }

    public int getRate() {
        return rate;
    }

    public void setRate(int rate) {
        this.rate = rate;
    }

    int postId;
    int rate;

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(postId);
        dest.writeInt(rate);
    }

    public static final Parcelable.Creator<Vote> CREATOR = new Parcelable.Creator<Vote>() {
        public Vote createFromParcel(Parcel in) {
            return new Vote(in);
        }

        public Vote[] newArray(int size) {
            return new Vote[size];
        }
    };

    private Vote(Parcel in) {
        postId = in.readInt();
        rate = in.readInt();

    }

    public Vote() {
        // TODO Auto-generated constructor stub
    }

    public Vote(int postId, int rate) {
        super();
        this.postId = postId;
        this.rate = rate;
    }

    @Override
    public int describeContents() {
        // TODO Auto-generated method stub
        return 0;
    }

}
