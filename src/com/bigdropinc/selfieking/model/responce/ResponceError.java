package com.bigdropinc.selfieking.model.responce;

/**
 * Class for correct parse json with jackson
 * 
 * @author bigdrop
 * 
 */
public class ResponceError {
    public String errorMessage;
    public int errorId;

    public ResponceError() {

    }

    public ResponceError(String errorMessage, int errorId) {
        super();
        this.errorMessage = errorMessage;
        this.errorId = errorId;
    }

}
