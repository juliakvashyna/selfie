package com.bigdropinc.selfieking.model.responce;

import java.util.List;

/**
 * Class for correct prase json with jackson
 * @author bigdrop
 *
 */
public class ResponseListNotification {
    public String status;

    public List<ResponceError> error;
    public  ResponceNotifications notifications= new ResponceNotifications();
    public Winner winner;
 
}
