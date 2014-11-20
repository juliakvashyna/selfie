package com.bigdropinc.selfieking.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bigdropinc.selfieking.R;
import com.bigdropinc.selfieking.controller.managers.FileManager;

import java.util.List;

public class BottomMenuAdapter extends ArrayAdapter<MenuItem> {

    private ImageView imageView;
    private TextView textView;
    private Context context;
    private int r;

    public BottomMenuAdapter(Context context, int r, List<MenuItem> objects) {

        super(context, 0, objects);
        this.context = context;
        this.r = r;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        convertView = LayoutInflater.from(context).inflate(r, parent, false);
        imageView = (ImageView) convertView.findViewById(R.id.mimage);
        textView = (TextView) convertView.findViewById(R.id.mtitle);
        MenuItem itemMenu = getItem(position);
        if (itemMenu.getImageres() != 0) {
            imageView.setImageResource(itemMenu.getImageres());
        } else {
            imageView.setImageBitmap(FileManager.getBitmapFromAsset(itemMenu.getPath()));
        }
        if (itemMenu.getTitleres() != 0) {
            String text = context.getResources().getString((itemMenu.getTitleres()));

            textView.setText(text);
        }

        return convertView;
    }
}
