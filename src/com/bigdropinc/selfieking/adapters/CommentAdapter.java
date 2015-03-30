package com.bigdropinc.selfieking.adapters;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.bigdropinc.selfieking.R;
import com.bigdropinc.selfieking.controller.CustomPicasso;
import com.bigdropinc.selfieking.model.selfie.Comment;
import com.bigdropinc.selfieking.views.RoundedImageView;

public class CommentAdapter extends ArrayAdapter<Comment> {

    private Context context;
    private int r;
    private List<Comment> objects;
    private boolean isNotification;

    public CommentAdapter(Context context, int resource, List<Comment> objects) {
        super(context, resource, objects);
        this.context = context;
        this.objects = objects;
        this.r = resource;
    }

    public List<Comment> getObjects() {
        return objects;
    }

    public void setObjects(List<Comment> objects) {
        this.objects = objects;
    }

    @Override
    public int getCount() {
        return objects.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        Comment comment = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(r, parent, false);
            holder = new ViewHolder();
            holder.userTextView = (TextView) convertView.findViewById(R.id.commentUser);
            holder.avatar = (RoundedImageView) convertView.findViewById(R.id.favatar);
            holder.commentTextView = (TextView) convertView.findViewById(R.id.commentText);
          
            CustomPicasso.getImageLoader(context).load("http://i.dailymail.co.uk/i/pix/2014/03/10/article-0-1C2B325500000578-458_634x699.jpg").into(holder.avatar);

            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.userTextView.setText(comment.getUserName());
        holder.commentTextView.setText(comment.getText());
        return convertView;
    }

    public boolean isNotification() {
        return isNotification;
    }

    public void setNotification(boolean isNotification) {
        this.isNotification = isNotification;
    }

    class ViewHolder {
        TextView userTextView;
        TextView commentTextView;
        private RoundedImageView avatar;

    }

}
