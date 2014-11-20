package com.bigdropinc.selfieking.model.selfie;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonAutoDetect.Visibility;

import android.os.Parcel;
import android.os.Parcelable;

@JsonAutoDetect(fieldVisibility = Visibility.ANY)
public class Like implements Parcelable {
    private int postId;
    private String token;

    public int getPostId() {
        return postId;
    }

    public void setPostId(int postId) {
        this.postId = postId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(postId);
        dest.writeInt(postId);
    }

    public static final Parcelable.Creator<Like> CREATOR = new Parcelable.Creator<Like>() {
        public Like createFromParcel(Parcel in) {
            return new Like(in);
        }

        public Like[] newArray(int size) {
            return new Like[size];
        }
    };

    private Like(Parcel in) {
        postId = in.readInt();

        token = in.readString();
    }

    public Like() {
        // TODO Auto-generated constructor stub
    }
}
