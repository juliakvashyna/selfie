package com.bigdropinc.selfieking.adapters;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.bigdropinc.selfieking.R;
import com.bigdropinc.selfieking.model.selfie.Comment;

public class CommentAdapter extends ArrayAdapter<Comment> {

    private Context context;
    private int r;

    public CommentAdapter(Context context, int resource, List<Comment> objects) {
        super(context, resource,  objects);
        this.context = context;
        this.r = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        Comment comment = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(r, parent, false);
            holder = new ViewHolder();
            holder.userTextView = (TextView) convertView.findViewById(R.id.commentUser);
            holder.commentTextView = (TextView) convertView.findViewById(R.id.commentText);
            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.userTextView.setText(comment.getUserName());
        holder.commentTextView.setText(comment.getText());
        return convertView;
    }

    class ViewHolder {
        TextView userTextView;
        TextView commentTextView;

    }

}
