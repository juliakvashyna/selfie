package com.bigdropinc.selfieking.controller.loaders;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Loader;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.bigdropinc.selfieking.controller.HttpClientHelper;
import com.bigdropinc.selfieking.controller.InternetChecker;
import com.bigdropinc.selfieking.controller.UrlRequest;
import com.bigdropinc.selfieking.controller.managers.login.LoginManagerImpl;
import com.bigdropinc.selfieking.model.Password;
import com.bigdropinc.selfieking.model.User;
import com.bigdropinc.selfieking.model.responce.ResponceError;
import com.bigdropinc.selfieking.model.responce.ResponseListNotification;
import com.bigdropinc.selfieking.model.responce.ResponseListSelfie;
import com.bigdropinc.selfieking.model.responce.StatusCode;
import com.bigdropinc.selfieking.model.responce.notification.Notification;
import com.bigdropinc.selfieking.model.selfie.CommentSelfieImage;
import com.bigdropinc.selfieking.model.selfie.SelfieImage;
import com.bigdropinc.selfieking.model.selfie.Vote;

public class CommandLoader extends Loader<StatusCode> {

    private static String TAG = "tag";
    private User user;
    private SelfieImage selfieImage;
    private CommentSelfieImage commentSelfieImage;

    private HttpClientHelper helper = new HttpClientHelper();
    public RegistrAsyncTask registrTask;
    private StatusCode statusCode = new StatusCode();
    private Password password;
    private String commandName;
    private List<SelfieImage> selfies;
    private Command command;
    private Activity activity;
    private ResponseListSelfie responseListSelfie;
    private List<Notification> notifications;

    public CommandLoader(Activity activity, Bundle bundle) {
        super(activity.getApplicationContext());
        if (bundle != null) {
            command = bundle.getParcelable("command");
            user = command.getUser();
            selfieImage = command.getSelfieImage();
            commandName = command.getCommand();
            password = command.getPassword();
            this.activity = activity;
            Log.d(TAG, "CommandName " + commandName);
        }
    }

    public Command getCommand() {
        return command;
    }

    public void setCommand(Command command) {
        this.command = command;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public CommandLoader(Context context) {
        super(context);
    }

    public List<SelfieImage> getSelfies() {
        return selfies;
    }

    public void setSelfies(List<SelfieImage> selfies) {
        this.selfies = selfies;
    }

    public SelfieImage getSelfieImage() {
        return selfieImage;
    }

    public void setSelfieImage(SelfieImage selfieImage) {
        this.selfieImage = selfieImage;
    }

    public CommentSelfieImage getCommentSelfieImage() {
        return commentSelfieImage;
    }

    public void setCommentSelfieImage(CommentSelfieImage commentSelfieImage) {
        this.commentSelfieImage = commentSelfieImage;
    }

    @Override
    protected void onStartLoading() {
        // Log.d(TAG, hashCode() + " onStartLoading");

        if (takeContentChanged())
            forceLoad();
    }

    @Override
    protected void onStopLoading() {
        super.onStopLoading();
        // Log.d(TAG, hashCode() + " onStopLoading");
    }

    @Override
    protected void onAbandon() {
        super.onAbandon();
        // Log.d(TAG, hashCode() + " onAbandon");
    }

    @Override
    protected void onReset() {
        super.onReset();
        // Log.d(TAG, hashCode() + " onReset");
    }

    void getResultFromTask(StatusCode result) {
        deliverResult(result);
    }

    @Override
    protected void onForceLoad() {
        super.onForceLoad();
        if (InternetChecker.isNetworkConnected()) {
            // Log.d(TAG, hashCode() + " onForceLoad");
            if (registrTask != null)
                registrTask.cancel(true);
            registrTask = new RegistrAsyncTask();
            registrTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, user);
        } else {
            InternetChecker.showNotInternetError(activity);
        }
    }

    public ResponseListSelfie getResponseListSelfie() {
        return responseListSelfie;
    }

    public void setResponseListSelfie(ResponseListSelfie responseListSelfie) {
        this.responseListSelfie = responseListSelfie;
    }

    public class RegistrAsyncTask extends AsyncTask<User, Void, StatusCode> {

        @Override
        protected StatusCode doInBackground(User... params) {
            try {
                if (Command.REGISTR.equals(commandName)) {
                    user = helper.registr(user);
                } else if (Command.RESET_PASSWORD.equals(commandName)) {
                    statusCode = helper.resetPassword(user);
                } else if (Command.POST_SELFIE.equals(commandName)) {
                    statusCode = helper.postSelfie(selfieImage, false);
                } else if (Command.POST_SELFIE_CONTEST.equals(commandName)) {
                    statusCode = helper.postSelfie(selfieImage, true);
                } else if (Command.DELETE_SELFIE.equals(commandName)) {
                    statusCode = helper.deleteSelfie(selfieImage);
                } else if (Command.GET_SELFIES.equals(commandName)) {
                    if (user != null)
                        setResponseListSelfie(helper.getSelfies(command.getOffset(), user.getId()));
                    else
                        setResponseListSelfie(helper.getSelfies(command.getOffset()));

                } else if (Command.LOGIN.equals(commandName)) {
                    user = helper.getUser(user.getEmail(), user.getPassword());
                } else if (Command.LOGIN_FB.equals(commandName)) {
                    user = helper.loginFB(user.getEmail(), user.getUserName());
                } else if (Command.EDIT_PROFILE.equals(commandName)) {
                    user = helper.editProfile(user);

                } else if (Command.AVATAR.equals(commandName)) {
                    user = helper.loadAvatar(command.getUser().getAvatarBytes());

                } else if (Command.ADD_COMMENT.equals(commandName)) {
                    commentSelfieImage = helper.commentSelfie(command.getComment());
                } else if (Command.LIKE.equals(commandName)) {
                    selfieImage = helper.likeSelfie(command.getLike(), UrlRequest.LIKE);
                } else if (Command.DISLIKE.equals(commandName)) {
                    selfieImage = helper.likeSelfie(command.getLike(), UrlRequest.DISLIKE);
                } else if (Command.GET_SELFIE.equals(commandName)) {
                    commentSelfieImage = helper.getSelfieWithComments(selfieImage);
                } else if (Command.GET_USER.equals(commandName)) {
                    if (user != null) {
                        user = helper.getUser(LoginManagerImpl.getInstance().getToken(), user.getId());
                    } else
                        user = helper.getUser(LoginManagerImpl.getInstance().getToken());
                } else if (Command.CHANGE_PASSWORD.equals(commandName)) {
                    statusCode = helper.changePassword(password);
                } else if (Command.DELETE_ACCOUNT.equals(commandName)) {
                    statusCode = helper.deleteAccount();
                } else if (Command.ADD_CONTEST.equals(commandName)) {
                    selfieImage = helper.contest(selfieImage);
                } else if (Command.GET_CONTEST.equals(commandName)) {
                    responseListSelfie = helper.getContest(command.getContest());
                    setResponseListSelfie(responseListSelfie);
                } else if (Command.VOTE.equals(commandName)) {
                    Vote vote = command.getVote();
                    statusCode = helper.vote(vote.getPostId(), vote.getRate());
                    if (statusCode.isSuccess()) {
                        selfieImage = helper.getSelfie(vote.getPostId());
                    }
                } else if (Command.REGISTR_DEVICE.equals(commandName)) {
                    statusCode = helper.registrDevice(command.getDeviceInfo());
                } else if (Command.GET_NOTIFICATIONS.equals(commandName)) {
                    notifications = helper.getNotifications(command.getOffset());
                }
            } catch (ApiException e) {
                statusCode.setCode(e.status);
                statusCode.setError((ArrayList<ResponceError>) e.error);
            }
            if (statusCode.getCode() == null || statusCode.getCode().isEmpty()) {
                statusCode.setCode("Success");
            }
            return statusCode;
        }

        @Override
        protected void onPostExecute(StatusCode result) {
            super.onPostExecute(result);
            getResultFromTask(result);
        }
    }

    public List<Notification> getNotifications() {
        return notifications;
    }

    public void setNotifications(List<Notification> notifications) {
        this.notifications = notifications;
    }
}
