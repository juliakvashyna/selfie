package com.bigdropinc.selfieking.model.responce;

import java.util.List;

import com.bigdropinc.selfieking.model.responce.notification.Notification;
import com.bigdropinc.selfieking.model.selfie.SelfieImage;

/**
 * Class for correct prase json with jackson
 * 
 * @author bigdrop
 * 
 */
public class ResponceNotifications {
    public int offset;
    public int limit;
    public int count;
    public int vote;
    public String order;
    public List<Notification> list;
}
