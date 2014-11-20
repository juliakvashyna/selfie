package com.bigdropinc.selfieking.adapters;

import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.bigdropinc.selfieking.R;
import com.bigdropinc.selfieking.controller.CustomPicasso;
import com.bigdropinc.selfieking.controller.UrlRequest;
import com.bigdropinc.selfieking.model.selfie.SelfieImage;

public class ImageAdapter extends ArrayAdapter<SelfieImage> {

    private Context mContext;
    private List<SelfieImage> images;

    public ImageAdapter(Context context, int r, List<SelfieImage> images) {
        super(context, 0, images);
        this.images = images;
        mContext = context;
    }

    @Override
    public int getCount() {
        if (images != null)
            return images.size();
        return 0;
    }

    @Override
    public SelfieImage getItem(int position) {
        return images.get(position);
    }

    @Override
    public long getItemId(int arg0) {
        return 0;
    }

    private static final int IMAGE_SIZE = 150;

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {
            imageView = new ImageView(mContext);
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            imageView.setAdjustViewBounds(true);
            imageView.setPadding(1, 1, 1, 1);
        } else {
            imageView = (ImageView) convertView;
        }
        SelfieImage image = getItem(position);
        Log.d("pager", UrlRequest.ADDRESS + image.getImage()
                );
        CustomPicasso.getImageLoader(mContext).load(UrlRequest.ADDRESS + image.getImage()).resize(IMAGE_SIZE, IMAGE_SIZE).error(R.drawable.notfound).into(imageView);
        return imageView;
    }

    class ViewHolder {
        ImageView imageView;
    }

}
