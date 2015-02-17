package com.bigdropinc.selfieking.controller;

public interface UrlRequest {
    String ADDRESS = "http://selfieking.bigdropinc.com";
    // login
    String GET_USER = ADDRESS + "/api/user/get";
    String LOGIN = ADDRESS + "/api/login";
    String LOGIN_FB = ADDRESS + "/api/fb/login";
    String REGISTR_USER = ADDRESS + "/api/register";
    String RESET_PASSWORD = ADDRESS + "/api/resetPassword";

    // profile 
    String EDIT_PROFILE = ADDRESS + "/api/profile/edit";

    // selfies
    String POST_SELFIE = ADDRESS + "/api/post/new";
    String GET_SELFIES = ADDRESS + "/api/post/list";
    String DELETE_SELFIE = ADDRESS + "/api/post/delete";
    String ADD_COMMENT = ADDRESS + "/api/post/comment";
    String LIKE = ADDRESS + "/api/post/like";
    String DISLIKE = ADDRESS + "/api/post/dislike";
    String GET_POST = ADDRESS + "/api/post/get";
    String CHANGE_PASSWORD = ADDRESS + "/api/password/change";
    String DELETE_ACCOUNT = ADDRESS + "/api/account/delete";
    String ADD_CONTEST = ADDRESS + "/api/contest/add";
    String GET_CONTEST = ADDRESS + "/api/contest/get";
    String GET_LIKED = ADDRESS + "/api/post/liked";
}
