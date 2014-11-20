package com.bigdropinc.selfieking.controller.loaders;

import com.bigdropinc.selfieking.model.Password;
import com.bigdropinc.selfieking.model.User;
import com.bigdropinc.selfieking.model.selfie.Comment;
import com.bigdropinc.selfieking.model.selfie.Contest;
import com.bigdropinc.selfieking.model.selfie.Like;
import com.bigdropinc.selfieking.model.selfie.SelfieImage;

import android.os.Parcel;
import android.os.Parcelable;

public class Command implements Parcelable {
    public static final String BUNDLE_NAME = "command";
    public static final String REGISTR = "registr";
    public static final String DELETE_SELFIE = "delete";
    public static final String RESET_PASSWORD = "resetPassword";
    public static final String LOGIN = "login";
    public static final String POST_SELFIE = "postSelfie";
    public static final String GET_SELFIES = "selfies";
    public static final String GET_SELFIE = "selfie";
    public static final String EDIT_PROFILE = "editProfile";
    public static final String ADD_COMMENT = "addComment";
    public static final String LIKE = "like";
    public static final String DISLIKE = "dislike";
    public static final String COMMENTS = "comments";
    public static final String GET_USER = "getUser";
    public static final String DELETE_ACCOUNT = "deleteAccount";
    public static final String CHANGE_PASSWORD = "changePassword";
    public static final String ADD_CONTEST = "addContest";
    public static final String GET_CONTEST = "getContest";
    public static final String GET_LIKED = "getLiked";
    private String command;
    private Password password;
    private int offset;
    private User user;
    private SelfieImage selfieImage;
    private Comment comment;
    private Contest contest;
    private Like like;

    public Contest getContest() {
        return contest;
    }

    public void setContest(Contest contest) {
        this.contest = contest;
    }

    public Command(String command, User param) {
        super();
        this.command = command;
        this.user = param;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User param) {
        this.user = param;
    }

    public SelfieImage getSelfieImage() {
        return selfieImage;
    }

    public void setSelfieImage(SelfieImage selfieImage) {
        this.selfieImage = selfieImage;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public Comment getComment() {
        return comment;
    }

    public void setComment(Comment comment) {
        this.comment = comment;
    }

    public Like getLike() {
        return like;
    }

    public void setLike(Like like) {
        this.like = like;
    }

    public Password getPassword() {
        return password;
    }

    public void setPassword(Password password) {
        this.password = password;
    }

    @Override
    public int describeContents() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeString(command);
        dest.writeInt(offset);
        dest.writeParcelable(user, Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
        dest.writeParcelable(comment, Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
        dest.writeParcelable(like, Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
        dest.writeParcelable(password, Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
        dest.writeParcelable(contest, Parcelable.PARCELABLE_WRITE_RETURN_VALUE);

    }

    public static final Parcelable.Creator<Command> CREATOR = new Parcelable.Creator<Command>() {
        public Command createFromParcel(Parcel in) {
            return new Command(in);
        }

        public Command[] newArray(int size) {
            return new Command[size];
        }
    };

    private Command(Parcel in) {
        command = in.readString();
        offset = in.readInt();
        user = in.readParcelable(null);
        comment = in.readParcelable(null);
        like = in.readParcelable(null);
        password = in.readParcelable(null);
        contest = in.readParcelable(null);
    }

    public Command(String command) {
        this.command = command;
    }

}
