package com.bigdropinc.selfieking.controller.managers.login;

import com.bigdropinc.selfieking.model.User;

public interface LoginManager {

    void signIn(int id, String token);

    void signIn(String login, String token);

    User getUser();

    String getToken();

    boolean check();

    void signOut();

    void setToken(String token);

    int getId();

}
