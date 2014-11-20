package com.bigdropinc.selfieking.model.responce;

import java.util.ArrayList;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonAutoDetect.Visibility;

@JsonAutoDetect(fieldVisibility = Visibility.ANY)
public class StatusCode {

    private String status;
    private String token;
    private ArrayList<ResponceError> error = new ArrayList<ResponceError>();

    public StatusCode() {
        super();

    }

    public StatusCode(String status) {
        super();
        this.status = status;
    }

    public String getCode() {
        return status;
    }

    public void setCode(String code) {
        this.status = code;
    }

    public ArrayList<ResponceError> getError() {
        return error;
    }

    public void setError(ArrayList<ResponceError> error) {
        this.error = error;
    }

    @Override
    public String toString() {
        return "StatusCode [status=" + status + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((status == null) ? 0 : status.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        StatusCode other = (StatusCode) obj;
        if (status == null) {
            if (other.status != null)
                return false;
        } else if (!status.equals(other.status))
            return false;
        return true;
    }

    public boolean isSuccess() {

        return status != null && status.equals("Success");
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

}
