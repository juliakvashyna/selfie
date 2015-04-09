package com.bigdrop.selfieking.gcm;

import android.os.Parcel;
import android.os.Parcelable;

import com.bigdropinc.selfieking.model.Password;

public class DeviceInfo implements Parcelable {
    String uuid;
    String platform;
    String token;
    String userToken;
    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUserToken() {
        return userToken;
    }

    public void setUserToken(String userToken) {
        this.userToken = userToken;
    }

   
    public DeviceInfo() {
    }

    @Override
    public int describeContents() {

        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(uuid);
        dest.writeString(platform);
        dest.writeString(token);
        dest.writeString(userToken);

    }

    public static final Parcelable.Creator<DeviceInfo> CREATOR = new Parcelable.Creator<DeviceInfo>() {
        public DeviceInfo createFromParcel(Parcel in) {
            return new DeviceInfo(in);
        }

        public DeviceInfo[] newArray(int size) {
            return new DeviceInfo[size];
        }
    };

    private DeviceInfo(Parcel in) {
        uuid = in.readString();
        platform = in.readString();
        token = in.readString();
        userToken=in.readString();
        
    }

}
