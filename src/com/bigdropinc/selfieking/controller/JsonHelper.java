package com.bigdropinc.selfieking.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import android.util.Log;

import com.bigdropinc.selfieking.controller.loaders.ApiException;
import com.bigdropinc.selfieking.model.User;
import com.bigdropinc.selfieking.model.responce.ResponceCommentSelfie;
import com.bigdropinc.selfieking.model.responce.ResponceError;
import com.bigdropinc.selfieking.model.responce.ResponceSelfie;
import com.bigdropinc.selfieking.model.responce.ResponceUser;
import com.bigdropinc.selfieking.model.responce.ResponseListSelfie;
import com.bigdropinc.selfieking.model.responce.StatusCode;
import com.bigdropinc.selfieking.model.selfie.CommentSelfieImage;
import com.bigdropinc.selfieking.model.selfie.SelfieImage;

public class JsonHelper {

    private static final String TAG = "tag";

    public User parseUser(String content) throws ApiException {
        ResponceUser responce = null;

        try {
            responce = new ObjectMapper().readValue(content, ResponceUser.class);
        } catch (JsonParseException e) {

            e.printStackTrace();
        } catch (JsonMappingException e) {

            e.printStackTrace();
        } catch (IOException e) {

            e.printStackTrace();
        }
        User user = new User();
        if (responce == null) {
            throw new ApiException("ResponceUser is null");
        } else if (responce.error != null) {
            throw new ApiException(responce.status, responce.error); 
        } else {
            user = responce.user;
            if (user != null)
                user.setToken(responce.token);
        }
        return user;
    }

    public StatusCode parseMessage(String content) {
        StatusCode code = new StatusCode();

        try {
            code = new ObjectMapper().readValue(content, StatusCode.class);
        } catch (JsonParseException e) {
            e.printStackTrace();
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return code;
    }

    public List<SelfieImage> parseSelfies(String content) throws ApiException {
        ResponseListSelfie response = null;
        try {
            response = new ObjectMapper().readValue(content, ResponseListSelfie.class);
        } catch (JsonParseException e) {
            e.printStackTrace();
            initInternetException();
        } catch (JsonMappingException e) {
            e.printStackTrace();
            initInternetException();
        } catch (IOException e) {
            e.printStackTrace();
            initInternetException();
        }
        if (response == null) {
            Log.d(TAG, "ResponseListSelfie is null");
            return new ArrayList<SelfieImage>();
        } else if (response.error != null) {
            throw new ApiException(response.status, response.error);
        }
        return response.posts.list;

    }

    public ResponseListSelfie parseResponseListSelfie(String content) throws ApiException {
        ResponseListSelfie response = null;
        try {
            response = new ObjectMapper().readValue(content, ResponseListSelfie.class);
        } catch (JsonParseException e) {
            e.printStackTrace();
            initInternetException();
        } catch (JsonMappingException e) {
            e.printStackTrace();
            initInternetException();
        } catch (IOException e) {
            e.printStackTrace();
            initInternetException();
        }
        if (response == null) {
            Log.d(TAG, "ResponseListSelfie is null");
            return response;
        } else if (response.error != null) {
            throw new ApiException(response.status, response.error);
        }
        return response;

    }

    private void initInternetException() throws ApiException {
        List<ResponceError> error = new ArrayList<ResponceError>();
        error.add(new ResponceError("Too slow internet connection", 1));
        throw new ApiException("Failure", error);
    }

    public SelfieImage parseSelfie(String content) {
        ResponceSelfie response = null;
        try {
            ObjectMapper mapper = new ObjectMapper();

            response = mapper.readValue(content, ResponceSelfie.class);
        } catch (JsonParseException e) {
            e.printStackTrace();
        } catch (JsonMappingException e) {

            e.printStackTrace();
        } catch (IOException e) {

            e.printStackTrace();
        }
        if (response != null)
            return response.post;
        return new SelfieImage();

    }

    public CommentSelfieImage parseCommentSelfie(String content) {
        ResponceCommentSelfie response = null;
        try {
            ObjectMapper mapper = new ObjectMapper();

            response = mapper.readValue(content, ResponceCommentSelfie.class);
        } catch (JsonParseException e) {
            e.printStackTrace();
        } catch (JsonMappingException e) {

            e.printStackTrace();
        } catch (IOException e) {

            e.printStackTrace();
        }
        if (response != null)
            return response.post;
        return new CommentSelfieImage();

    }
}
