package com.bigdropinc.selfieking.adapters;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bigdrop.selfieking.db.DatabaseManager;
import com.bigdropinc.selfieking.R;
import com.bigdropinc.selfieking.activities.social.CommentsActivity;
import com.bigdropinc.selfieking.activities.social.MyActionBarActivity;
import com.bigdropinc.selfieking.activities.social.OneSelfieActivity;
import com.bigdropinc.selfieking.controller.CustomPicasso;
import com.bigdropinc.selfieking.controller.UrlRequest;
import com.bigdropinc.selfieking.controller.managers.login.LoginManagerImpl;
import com.bigdropinc.selfieking.model.User;
import com.bigdropinc.selfieking.model.selfie.Comment;
import com.bigdropinc.selfieking.model.selfie.Like;
import com.bigdropinc.selfieking.model.selfie.SelfieImage;
import com.bigdropinc.selfieking.views.RoundedImageView;

public class FeedAdapter extends ArrayAdapter<SelfieImage> {

    private static final int second = 1000;
    private static final int week = 604800000;
    private static final int day = 86400000;
    private static final int hour = 3600000;
    private static final int minute = 60000;
    private int IMAGE_SIZE = 400;
    private int AVATAR_SIZE = 200;
    private Activity context;
    private int r;
    private List<SelfieImage> objects;

    public FeedAdapter(Activity context, int r, List<SelfieImage> objects) {
        super(context, 0, objects);
        this.context = context;
        this.setObjects(objects);
        this.r = r;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        SelfieImage feedItem = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(r, parent, false);
            holder = new ViewHolder();
            holder.imageView = (ImageView) convertView.findViewById(R.id.fimage);
            holder.timeTextView = (TextView) convertView.findViewById(R.id.ftime);
            holder.likes = (TextView) convertView.findViewById(R.id.likes);
            holder.comments = (TextView) convertView.findViewById(R.id.comments);
            holder.nameTextView = (TextView) convertView.findViewById(R.id.fName);
            holder.descTextView = (TextView) convertView.findViewById(R.id.fdescription);
            holder.likeButton = (Button) convertView.findViewById(R.id.flike);
            holder.contentButton = (Button) convertView.findViewById(R.id.fcontent);
            holder.commentButton = (Button) convertView.findViewById(R.id.fcomment);
            holder.commentsTextView = (TextView) convertView.findViewById(R.id.commentsListTextView);
            holder.commentEditText = (EditText) convertView.findViewById(R.id.commentEditText);
            holder.avatar = (RoundedImageView) convertView.findViewById(R.id.favatar);
            holder.sendCommentButton = (Button) convertView.findViewById(R.id.commentButton);
            holder.commentLayout = (LinearLayout) convertView.findViewById(R.id.commentLayout);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        initListeners(holder, feedItem, position);
        String token = LoginManagerImpl.getInstance().getToken();
        User user = DatabaseManager.getInstance().findUser(token);
        holder.nameTextView.setText(user.getUserName());
        if (feedItem != null) {
            String imageUrl = getImageUrl(feedItem);
            CustomPicasso.getImageLoader(context).load(UrlRequest.ADDRESS + imageUrl).resize(IMAGE_SIZE, IMAGE_SIZE).noFade().error(R.drawable.notfound).into(holder.imageView);
            holder.likes.setText(String.valueOf(feedItem.getLikes()));
            holder.comments.setText(String.valueOf(feedItem.getComment()));
            if (feedItem.getDescription() != null && !feedItem.getDescription().isEmpty()) {
                holder.descTextView.setText(feedItem.getDescription());
                holder.descTextView.setVisibility(View.VISIBLE);
            }
            holder.timeTextView.setText(getTimeString(feedItem.getDate()));
            CustomPicasso.getImageLoader(context).load("http://i.dailymail.co.uk/i/pix/2014/03/10/article-0-1C2B325500000578-458_634x699.jpg").resize(AVATAR_SIZE, AVATAR_SIZE).into(holder.avatar);
        }
        return convertView;
    }

    private String getImageUrl(SelfieImage feedItem) {
        String imageUrl;
        if (feedItem.getImageMedium()!= null && !feedItem.getImageMedium().equals("false"))
            imageUrl = feedItem.getImageMedium();
        else
            imageUrl = feedItem.getImage();
        return imageUrl;
    }

    @Override
    public int getCount() {   
        return objects.size();
    }

    private void initListeners(final ViewHolder holder, final SelfieImage selfie, final int position) {
        holder.likeButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // FeedFragment.index=position;
                Log.d("listview", "FeedAdapter selfie id =" + selfie.getId());
                like(selfie, position);
            }
        });
        holder.contentButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                contest(selfie);
            }
        });
        holder.commentButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.commentLayout.setVisibility(View.VISIBLE);

            }
        });
        holder.sendCommentButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                comment(selfie, holder.commentEditText.getText().toString());
                holder.commentEditText.setText("");
                holder.commentLayout.setVisibility(View.GONE);
                hideKeyboard(holder);
            }
        });
        holder.commentsTextView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent intent = new Intent(context.getApplicationContext(), CommentsActivity.class);
                intent.putExtra("postId", selfie.getId());
                context.startActivity(intent);
            }
        });
    }

    private String getTimeString(String time) {
        String timeStr = "";
        Date postTime = getDate(time);

        Calendar post = Calendar.getInstance();
        post.setTime(postTime);
        Calendar now = Calendar.getInstance();
        now.setTime(new Date());
        long difference = now.getTimeInMillis() - post.getTimeInMillis();
        if (difference <= minute) {
            timeStr = String.valueOf(difference / second) + " seconds ago";
        } else if (difference <= hour) {
            timeStr = String.valueOf(difference / minute) + " min ago";
        } else if (difference <= day) {
            timeStr = String.valueOf(difference / hour) + " hours ago";
        } else if (difference <= week) {
            timeStr = String.valueOf(difference / day) + " days ago";
        } else {
            timeStr = String.valueOf(now.get(Calendar.WEEK_OF_YEAR) - post.get(Calendar.WEEK_OF_YEAR)) + " weeks ago";
        }
        return timeStr;
    }

    private Date getDate(String dateString) {
        Date date = null;
        try {
            date = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.ENGLISH).parse(dateString);
        } catch (ParseException e) {

            e.printStackTrace();
        }
        return date;
    }

    private void comment(SelfieImage selfie, String text) {
        Comment comment = createComment(selfie, text);
        if (context instanceof OneSelfieActivity) {
            ((OneSelfieActivity) context).comment(comment);
        } else if (context instanceof MyActionBarActivity) {
            ((MyActionBarActivity) context).comment(comment);
        }
    }

    private Comment createComment(SelfieImage selfie, String text) {
        Comment comment = new Comment();
        comment.setPostId(selfie.getId());
        comment.setText(text);
        return comment;
    }

    private void contest(SelfieImage selfie) {
        if (context instanceof OneSelfieActivity) {
            ((OneSelfieActivity) context).contest(selfie);
        } else if (context instanceof MyActionBarActivity) {
            ((MyActionBarActivity) context).contest(selfie);
        }
    }

    private void like(SelfieImage selfie, int position) {
        Like like = new Like();
        like.setPostId(selfie.getId());
        like.setToken(LoginManagerImpl.getInstance().getToken());
        if (context instanceof OneSelfieActivity) {
            ((OneSelfieActivity) context).like(like, selfie.isLiked());
        } else if (context instanceof MyActionBarActivity) {
            ((MyActionBarActivity) context).like(like, selfie.isLiked(), position);
        }
    }

    class ViewHolder {
        private ImageView imageView;
        private TextView descTextView;
        private TextView timeTextView;
        private TextView nameTextView;
        private TextView likes;
        private TextView comments;
        private TextView commentsTextView;
        private Button likeButton;
        private Button contentButton;
        private Button commentButton;
        private Button sendCommentButton;
        private EditText commentEditText;
        private RoundedImageView avatar;
        private LinearLayout commentLayout;
    }

    public List<SelfieImage> getObjects() {
        return objects;
    }

    public void setObjects(List<SelfieImage> objects) {
        this.objects = objects;
    }

    private void hideKeyboard(final ViewHolder holder) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(holder.commentEditText.getWindowToken(), 0);
    }
}
