package com.bigdropinc.selfieking.model.responce;

import java.util.List;

import com.bigdropinc.selfieking.model.selfie.SelfieImage;

/**
 * Class for correct prase json with jackson
 * 
 * @author bigdrop
 * 
 */
public class ResponcePosts {
    public int offset;
    public int limit;
    public int count;
    public String order;
    public List<SelfieImage> list;
}
