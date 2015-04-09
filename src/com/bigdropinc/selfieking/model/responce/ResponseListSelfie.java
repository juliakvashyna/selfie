package com.bigdropinc.selfieking.model.responce;

import java.util.List;

/**
 * Class for correct prase json with jackson
 * @author bigdrop
 *
 */
public class ResponseListSelfie {
    public String status;

    public List<ResponceError> error;
    public  ResponcePosts posts= new ResponcePosts();
    public Winner winner;
 
}
