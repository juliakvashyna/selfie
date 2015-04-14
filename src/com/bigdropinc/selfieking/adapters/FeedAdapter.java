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
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.RatingBar.OnRatingBarChangeListener;
import android.widget.TextView;

import com.bigdropinc.selfieking.R;
import com.bigdropinc.selfieking.activities.profile.ProfileActivity;
import com.bigdropinc.selfieking.activities.social.CommentsActivity;
import com.bigdropinc.selfieking.activities.social.ContestFragment;
import com.bigdropinc.selfieking.activities.social.MyActionBarActivity;
import com.bigdropinc.selfieking.activities.social.OneSelfieActivity;
import com.bigdropinc.selfieking.controller.CustomPicasso;
import com.bigdropinc.selfieking.controller.UrlRequest;
import com.bigdropinc.selfieking.controller.managers.login.LoginManagerImpl;
import com.bigdropinc.selfieking.model.selfie.Like;
import com.bigdropinc.selfieking.model.selfie.SelfieImage;
import com.bigdropinc.selfieking.views.RoundedImageView;
import com.squareup.picasso.Callback;

public class FeedAdapter extends ArrayAdapter<SelfieImage> {

    private static final int second = 1000;
    private static final int week = 604800000;
    private static final int day = 86400000;
    private static final int hour = 3600000;
    private static final int minute = 60000;
    private int IMAGE_SIZE = 320;
    private int AVATAR_SIZE = 200;
    private Activity context;
    private int r;
    private List<SelfieImage> objects;
    private ContestFragment contestFragment;

    public FeedAdapter(Activity context, int r, List<SelfieImage> objects) {
        super(context, 0, objects);
        this.context = context;
        this.setObjects(objects);
        this.r = r;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        final SelfieImage feedItem = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(r, parent, false);
            holder = new ViewHolder();
            holder.imageView = (ImageView) convertView.findViewById(R.id.fimage);
            holder.descTextView = (TextView) convertView.findViewById(R.id.fdescription);
            holder.locationTextView = (TextView) convertView.findViewById(R.id.flocation);
            holder.placeImageView = (ImageView) convertView.findViewById(R.id.place);
            holder.progressBar = (ProgressBar) convertView.findViewById(R.id.progressBar);
            holder.progressBar.setVisibility(View.VISIBLE);
            if (r == R.layout.feed_item) {
                holder.timeTextView = (TextView) convertView.findViewById(R.id.ftime);
                holder.likes = (TextView) convertView.findViewById(R.id.likes);
                holder.comments = (TextView) convertView.findViewById(R.id.comments);
                holder.nameTextView = (TextView) convertView.findViewById(R.id.fName);
                holder.likeButton = (Button) convertView.findViewById(R.id.flike);
                holder.contentButton = (Button) convertView.findViewById(R.id.fcontent);
                holder.commentButton = (Button) convertView.findViewById(R.id.fcomment);
                holder.commentsTextView = (TextView) convertView.findViewById(R.id.commentsListTextView);
                holder.avatar = (RoundedImageView) convertView.findViewById(R.id.favatar);
            } else {
                holder.contentButton = (Button) convertView.findViewById(R.id.contestButton);
            }
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        if (r == R.layout.feed_item) {
            initListeners(holder, feedItem, position);
        } else {
            initAddToContest(holder, feedItem);
        }
        if (feedItem != null) {
            fillSelfe(holder, feedItem);
        }
        return convertView;
    }

    private void fillSelfe(ViewHolder holder, final SelfieImage feedItem) {
        fillImage(holder, feedItem);
        fillDescription(holder, feedItem);
        if (feedItem.getLocation() != null && !feedItem.getLocation().isEmpty()) {
            holder.locationTextView.setText(feedItem.getLocation());
            holder.locationTextView.setVisibility(View.VISIBLE);
            holder.placeImageView.setVisibility(View.VISIBLE);
        } else {
            holder.locationTextView.setVisibility(View.GONE);
            holder.placeImageView.setVisibility(View.GONE);
        }
        if (r == R.layout.feed_item) {
            holder.likes.setText(String.valueOf(feedItem.getStars().getTotal()));
            holder.comments.setText(String.valueOf(feedItem.getComment()));
            holder.nameTextView.setText(feedItem.getUserName());
            holder.likeButton.setSelected(feedItem.isLiked());
            holder.contentButton.setSelected(feedItem.isInContest());
            holder.commentButton.setSelected(feedItem.getComment() != 0);
            holder.timeTextView.setText(getTimeString(feedItem.getDate()));
            fillAvatar(holder, feedItem);

        }
    }

    private void fillAvatar(ViewHolder holder, final SelfieImage feedItem) {
        String url = "http://i.dailymail.co.uk/i/pix/2014/03/10/article-0-1C2B325500000578-458_634x699.jpg";
        String userAvatar = feedItem.getUserAvatar();
        if (userAvatar != null && userAvatar != "")
            url = UrlRequest.ADDRESS + userAvatar;
        CustomPicasso.getImageLoader(context).load(url).into(holder.avatar);
    }

    private void fillDescription(ViewHolder holder, final SelfieImage feedItem) {
        if (feedItem.getDescription() != null && !feedItem.getDescription().isEmpty()) {
            holder.descTextView.setText(feedItem.getDescription());
            holder.descTextView.setVisibility(View.VISIBLE);
        } else {
            holder.descTextView.setVisibility(View.GONE);
        }
    }

    private void fillImage(ViewHolder holder, final SelfieImage feedItem) {
        String imageUrl = getImageUrl(feedItem);
        CustomPicasso.getImageLoader(context).load(UrlRequest.ADDRESS + imageUrl).resize(IMAGE_SIZE, IMAGE_SIZE).centerCrop().into(holder.imageView, new ImageLoadedCallback(holder.progressBar) {
            @Override
            public void onSuccess() {
                if (this.progressBar != null) {
                    this.progressBar.setVisibility(View.GONE);
                }
            }
        });
    }

    private void initAddToContest(ViewHolder holder, final SelfieImage feedItem) {
        holder.contentButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ((OneSelfieActivity) context).contest(feedItem);
            }
        });
    }

    private class ImageLoadedCallback implements Callback {
        ProgressBar progressBar;

        public ImageLoadedCallback(ProgressBar progBar) {
            progressBar = progBar;
        }

        @Override
        public void onSuccess() {

        }

        @Override
        public void onError() {

        }
    }

    private String getImageUrl(SelfieImage feedItem) {
        String imageUrl;
        if (feedItem.getImage() != null && !feedItem.getImage().equals("false"))
            imageUrl = feedItem.getImage();
        else
            imageUrl = feedItem.getImageMedium();
        return imageUrl;
    }

    @Override
    public int getCount() {
        return objects.size();
    }

    private void showPopup(ViewHolder holder, final SelfieImage selfie) {
        // Inflate the popup_layout.xml
        LinearLayout viewGroup = (LinearLayout) context.findViewById(R.id.popup);
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = layoutInflater.inflate(R.layout.popup_crowns, viewGroup);

        // Creating the PopupWindow
        final PopupWindow popup = new PopupWindow(layout, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, true);
        popup.setOutsideTouchable(true);
        popup.setBackgroundDrawable(new BitmapDrawable());
        final RatingBar ratingBar = (RatingBar) layout.findViewById(R.id.ratingBarCrowns);
        ratingBar.setMax(5);
        if (selfie != null && selfie.getStars() != null)
            ratingBar.setProgress((int) selfie.getStars().getMy());
        popup.showAsDropDown(holder.contentButton, 0, -230);
        popup.setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss() {
                if (context instanceof OneSelfieActivity) {
                    if ((int) ratingBar.getRating() != selfie.getStars().getMy())
                        ((OneSelfieActivity) context).vote(selfie.getId(), (int) ratingBar.getRating());
                }
            }
        });
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
                showPopup(holder, selfie);
                // contest(selfie, position);
            }
        });
        holder.commentButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startComment(selfie, position);
                // holder.commentLayout.setVisibility(View.VISIBLE);

            }
        });
        // holder.sendCommentButton.setOnClickListener(new OnClickListener() {
        // @Override
        // public void onClick(View v) {
        // // comment(selfie,
        // holder.commentEditText.getText().toString(),position);
        // Intent intent = new Intent(context.getApplicationContext(),
        // CommentsActivity.class);
        // intent.putExtra("postId", selfie.getId());
        // context.startActivity(intent);
        //
        // }
        // });
        holder.commentsTextView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                startComment(selfie, position);
            }

        });
        holder.avatar.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoUserProfile(selfie);
            }
        });
    }

    private void startComment(final SelfieImage selfie, final int position) {
        Intent intent = new Intent(context.getApplicationContext(), CommentsActivity.class);
        intent.putExtra("postId", selfie.getId());
        intent.putExtra("index", position);
        intent.putExtra("userId", selfie.getUserId());
        context.startActivityForResult(intent, 10);
    }

    private String getTimeString(String time) {
        String timeStr = "";
        Date postTime = getDate(time);
        if (postTime != null) {
            Calendar post = Calendar.getInstance();
            post.setTime(postTime);
            Calendar now = Calendar.getInstance();
            now.setTime(new Date());
            long difference = now.getTimeInMillis() - post.getTimeInMillis();
            if (difference <= minute) {
                timeStr = String.valueOf(difference / second) + " sec";
            } else if (difference <= hour) {
                timeStr = String.valueOf(difference / minute) + " min";
            } else if (difference <= day) {
                timeStr = String.valueOf(difference / hour) + " h";
            } else if (difference <= week) {
                timeStr = String.valueOf(difference / day) + " d";
            } else {
                timeStr = String.valueOf(now.get(Calendar.WEEK_OF_YEAR) - post.get(Calendar.WEEK_OF_YEAR)) + " weeks";
            }
        }
        return timeStr;
    }

    private Date getDate(String dateString) {
        Date date = null;
        try {
            date = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.ENGLISH).parse(dateString);
        } catch (ParseException e) {

            e.printStackTrace();
        } catch (NullPointerException e) {

            e.printStackTrace();
        }
        return date;
    }

    private void contest(SelfieImage selfie, int position) {

        if (context instanceof OneSelfieActivity) {
            ((OneSelfieActivity) context).contest(selfie);
        } else if (context instanceof MyActionBarActivity) {
            ((MyActionBarActivity) context).contest(selfie, position);
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
        private TextView locationTextView;
        private ImageView placeImageView;
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
        private ProgressBar progressBar;
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

    private void gotoUserProfile(final SelfieImage selfie) {
        Intent intent = new Intent(context.getApplicationContext(), ProfileActivity.class);
        int userId = selfie.getUserId();
        intent.putExtra("userId", userId);
        context.startActivity(intent);
    }

}
