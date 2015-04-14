package com.bigdropinc.selfieking.adapters;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bigdropinc.selfieking.R;
import com.bigdropinc.selfieking.activities.profile.ProfileActivity;
import com.bigdropinc.selfieking.activities.social.OneSelfieActivity;
import com.bigdropinc.selfieking.controller.CustomPicasso;
import com.bigdropinc.selfieking.controller.UrlRequest;
import com.bigdropinc.selfieking.model.responce.notification.Notification;
import com.bigdropinc.selfieking.model.selfie.Comment;
import com.bigdropinc.selfieking.views.RoundedImageView;

public class NotificationtAdapter extends ArrayAdapter<Notification> {

    private Context context;
    private int r;
    private List<Notification> objects;
    private boolean isNotification;

    public NotificationtAdapter(Context context, int resource, List<Notification> objects) {
        super(context, resource, objects);
        this.context = context;
        this.objects = objects;
        this.r = resource;
    }

    public List<Notification> getObjects() {
        return objects;
    }

    public void setObjects(List<Notification> objects) {
        this.objects = objects;
    }

    @Override
    public int getCount() {
        return objects.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        final Notification notification = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(r, parent, false);
            holder = new ViewHolder();
            holder.userTextView = (TextView) convertView.findViewById(R.id.commentUser);
            holder.avatar = (RoundedImageView) convertView.findViewById(R.id.favatar);
            holder.dateTextView = (TextView) convertView.findViewById(R.id.commentDate);
            holder.image = (com.makeramen.roundedimageview.RoundedImageView) convertView.findViewById(R.id.smallImage);

            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        fillAvatar(holder, notification);
        initListeners(holder, notification);
        holder.userTextView.setText(notification.getActor().getName());
        String text = "";
        if (notification.getType().equals("comment")) {
            text = "<font color=#ffdfdd ><b>" + notification.getActor().getName() + "<b></font> <font color=#de8d8a> commented on your photo:</font> <font color=#ffdfdd> \"" + notification.getObj().getComment().getText() + "\"</font>";
            holder.dateTextView.setText(notification.getObj().getComment().getDate());
        } else if (notification.getType().equals("rating")) {
            text = "<font color=#ffdfdd ><b>" + notification.getActor().getName() + "<b></font> <font color=#de8d8a> voted your photo! </font>";
            holder.dateTextView.setText(notification.getObj().getVote().getDate());
        }
        holder.userTextView.setText(Html.fromHtml(text));
       
        CustomPicasso.getImageLoader(context).load(UrlRequest.ADDRESS + notification.getObj().getImage()).resize(50, 50).into(holder.image);

        return convertView;
    }

    private void initListeners(ViewHolder holder, final Notification notification) {
        holder.avatar.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoUserProfile(notification.getActor().getId());
            }
        });
        holder.image.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context.getApplicationContext(), OneSelfieActivity.class);
                intent.putExtra("selfieId", notification.getObj().getId());
                context.startActivity(intent);
                
            }
        });
    }

    private void fillAvatar(ViewHolder holder, final Notification feedItem) {
        String url = "http://i.dailymail.co.uk/i/pix/2014/03/10/article-0-1C2B325500000578-458_634x699.jpg";
        String userAvatar = feedItem.getActor().getAvatar();
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
        TextView dateTextView;
        com.makeramen.roundedimageview.RoundedImageView image;
        private RoundedImageView avatar;

    }

}
