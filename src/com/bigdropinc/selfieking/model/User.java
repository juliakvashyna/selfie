package com.bigdropinc.selfieking.model;

import java.util.Date;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonAutoDetect.Visibility;

import android.os.Parcel;
import android.os.Parcelable;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@JsonAutoDetect(fieldVisibility = Visibility.ANY)
@DatabaseTable(tableName = "user")
public class User implements Parcelable {
    @DatabaseField(id = true, generatedId = false)
    private int id;
    @DatabaseField
    @JsonProperty("username")
    private String userName;
    @JsonProperty("name")
    private String name;
    @DatabaseField
    private String email;
    private String password;
    @DatabaseField
    @JsonProperty("profession")
    private String job;
    @DatabaseField
    @JsonProperty("website")
    private String site;
    @DatabaseField
    private byte gender;
    @DatabaseField
    private Date userRegDate;
    @DatabaseField
    private String last_visit;
    @DatabaseField
    private String reg_date;
    @DatabaseField
    @JsonProperty("token")
    private String token;
    @DatabaseField
    private String status;
    @DatabaseField
    private String phone;
    @DatabaseField
    private String userAvatar;
    private String avatar;
    private byte[] avatarBytes;

    public String getAvatar() {
        return userAvatar;
    }

    public void setAvatar(String avatar) {
        this.userAvatar = avatar;
    }

    public User(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public User(String username, String email, String password) {
        this.userName = username;
        this.password = password;
        this.email = email;
    }

    public User(String token) {
        this.token = token;

    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String login) {
        this.userName = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUserLatDate() {
        return last_visit;
    }

    public void setUserLatDate(String userLatDate) {
        this.last_visit = userLatDate;
    }

    public String getJob() {
        return job;
    }

    public void setJob(String job) {
        this.job = job;
    }

    public String getSite() {
        return site;
    }

    public void setSite(String site) {
        this.site = site;
    }

    public byte getGender() {
        return gender;
    }

    public void setGender(byte gender) {
        this.gender = gender;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    @Override
    public int describeContents() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(userName);
        dest.writeString(email);
        dest.writeString(password);
        dest.writeString(token);
        dest.writeString(phone);
        dest.writeString(site);
        dest.writeString(job);
    }

    public static final Parcelable.Creator<User> CREATOR = new Parcelable.Creator<User>() {
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        public User[] newArray(int size) {
            return new User[size];
        }
    };

    private User(Parcel in) {
        id = in.readInt();
        userName = in.readString();
        email = in.readString();
        password = in.readString();
        token = in.readString();
        phone = in.readString();
        site = in.readString();
        job = in.readString();
    }

    public User() {
        // TODO Auto-generated constructor stub
    }

    public Date getUserRegDate() {
        return userRegDate;
    }

    public void setUserRegDate(Date userRegDate) {
        this.userRegDate = userRegDate;
    }

    public String getReg_date() {
        return reg_date;
    }

    public void setReg_date(String reg_date) {
        this.reg_date = reg_date;
    }

    public String getLast_visit() {
        return last_visit;
    }

    public void setLast_visit(String last_visit) {
        this.last_visit = last_visit;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public byte[] getAvatarBytes() {
        return avatarBytes;
    }

    public void setAvatarBytes(byte[] avatarBytes) {
        this.avatarBytes = avatarBytes;
    }
}
