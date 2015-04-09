package com.bigdropinc.selfieking.adapters;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.bigdropinc.selfieking.R;
import com.bigdropinc.selfieking.activities.profile.ProfileActivity;
import com.bigdropinc.selfieking.controller.CustomPicasso;
import com.bigdropinc.selfieking.controller.UrlRequest;
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
        final Comment comment = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(r, parent, false);
            holder = new ViewHolder();
            holder.userTextView = (TextView) convertView.findViewById(R.id.commentUser);
            holder.avatar = (RoundedImageView) convertView.findViewById(R.id.favatar);
            holder.commentTextView = (TextView) convertView.findViewById(R.id.commentText);

            // CustomPicasso.getImageLoader(context).load("http://i.dailymail.co.uk/i/pix/2014/03/10/article-0-1C2B325500000578-458_634x699.jpg").into(holder.avatar);

            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        fillAvatar(holder, comment);
        initListeners(holder, comment);
        holder.userTextView.setText(comment.getUserName());
        holder.commentTextView.setText(comment.getText());
        return convertView;
    }

    private void initListeners(ViewHolder holder, final Comment comment) {
        holder.avatar.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoUserProfile(comment.getUserId());
            }
        });
    }

    private void fillAvatar(ViewHolder holder, final Comment feedItem) {
        String url = "http://i.dailymail.co.uk/i/pix/2014/03/10/article-0-1C2B325500000578-458_634x699.jpg";
        String userAvatar = feedItem.getUserAvatar();
        if (userAvatar != null && userAvatar != "")
            url = UrlRequest.ADDRESS + userAvatar;
        CustomPicasso.getImageLoader(context).load(url).into(holder.avatar);
    }

    public boolean isNotification() {
        return isNotification;
    }

    public void setNotification(boolean isNotification) {
        this.isNotification = isNotification;
    }

    protected void gotoUserProfile(int userId) {
        Intent intent = new Intent(context.getApplicationContext(), ProfileActivity.class);
        intent.putExtra("userId", userId);
        context.startActivity(intent);

    }

    class ViewHolder {
        TextView userTextView;
        TextView commentTextView;
        private RoundedImageView avatar;

    }

}
