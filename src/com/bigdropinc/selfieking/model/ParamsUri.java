package com.bigdropinc.selfieking.model;

import android.net.Uri;

public class ParamsUri {

    private int width;
    private int height;
    private Uri uri;

    public ParamsUri(int width, int height, Uri uri) {
        super();
        this.width = width;
        this.height = height;
        this.uri = uri;
    }

    public ParamsUri(Uri uri) {
        this.uri = uri;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public Uri getUri() {
        return uri;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }
}
