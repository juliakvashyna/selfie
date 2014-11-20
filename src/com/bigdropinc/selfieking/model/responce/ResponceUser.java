package com.bigdropinc.selfieking.model.responce;

import java.util.List;

import com.bigdropinc.selfieking.model.User;

/**
 * Class for correct prase json with jackson
 * 
 * @author bigdrop
 * 
 */
public class ResponceUser {
    public String status;
    public String token;
    public User user;
    public List<ResponceError> error;
}
