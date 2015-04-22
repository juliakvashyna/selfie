package com.bigdropinc.selfieking.adapters;

import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.bigdropinc.selfieking.R;
import com.bigdropinc.selfieking.controller.CustomPicasso;
import com.bigdropinc.selfieking.controller.UrlRequest;
import com.bigdropinc.selfieking.model.selfie.SelfieImage;

public class ImageAdapter extends ArrayAdapter<SelfieImage> {

    private Context mContext;
    private List<SelfieImage> images;

    public List<SelfieImage> getImages() {
        return images;
    }

    public void setImages(List<SelfieImage> images) {
        this.images = images;
    }

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
        if (position < images.size())
            return images.get(position);
        return null;
    }

    @Override
    public long getItemId(int arg0) {
        return 0;
    }

    private static final int IMAGE_SIZE = 250;

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {
            imageView = new ImageView(mContext);
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
          //  imageView.setLayoutParams(new LayoutParams(250, 250));
            imageView.setAdjustViewBounds(true);
            imageView.setPadding(4, 4, 4, 4);
         
        } else {
            imageView = (ImageView) convertView;
        }
        SelfieImage image = getItem(position);
        if (image != null) {
            String imageUrl = getImageUrl(image);
          // resize(IMAGE_SIZE, IMAGE_SIZE)
            CustomPicasso.getImageLoader(mContext).load(UrlRequest.ADDRESS + imageUrl).resize(IMAGE_SIZE, IMAGE_SIZE).placeholder(R.drawable.icon_bg).error(R.drawable.notfound).into(imageView);
        }
        return imageView;
    }

    private String getImageUrl(SelfieImage feedItem) {
        String imageUrl;

        if (feedItem.getImageSmall() != null && !feedItem.getImageSmall().equals("false"))
            imageUrl = feedItem.getImageSmall();
        else
            imageUrl = feedItem.getImage ();
        return imageUrl;
    }

    @Override
    public void notifyDataSetChanged() {
        // TODO Auto-generated method stub
        super.notifyDataSetChanged();
    }

    // class ViewHolder {
    // ImageView imageView;
    // }

}
