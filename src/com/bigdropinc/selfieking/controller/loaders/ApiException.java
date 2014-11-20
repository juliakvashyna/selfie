package com.bigdropinc.selfieking.controller.loaders;

import java.util.ArrayList;
import java.util.List;

import com.bigdropinc.selfieking.model.responce.ResponceError;

public class ApiException extends Exception {

    public String status;
    public List<ResponceError> error;
    /**
     * 
     */
    private static final long serialVersionUID = 8048082245603458341L;

    public ApiException(String status, List<ResponceError> error) {
        super();
        this.status = status;
        this.error = error;
    }

    public ApiException(String status, ResponceError error) {
        super();
        this.status = status;
        this.error = new ArrayList<ResponceError>();
        this.error.add(error);
    }

    public ApiException(String status) {
        this.status = status;
        this.error = new ArrayList<ResponceError>();
        ResponceError error = new ResponceError();
        error.errorMessage = status;
        this.error.add(error);
    }
}
