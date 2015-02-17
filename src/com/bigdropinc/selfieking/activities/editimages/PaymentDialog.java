package com.bigdropinc.selfieking.activities.editimages;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;

import com.bigdropinc.selfieking.R;
import com.makeramen.roundedimageview.RoundedImageView;

public class PaymentDialog extends Dialog implements android.view.View.OnClickListener {

    public Activity c;
    public Dialog d;
    public Button yes;
    public ImageButton close;
    public RoundedImageView imageView;
    android.view.View.OnClickListener listener;
    Bitmap bitmap;

    public PaymentDialog(Activity a, Bitmap bitmap, android.view.View.OnClickListener listener) {
        super(a);
        // TODO Auto-generated constructor stub
        this.c = a;
        this.listener = listener;
        this.bitmap = bitmap;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.payment_dialog);
        yes = (Button) findViewById(R.id.btn_yes);
        close = (ImageButton) findViewById(R.id.closedialog);
        imageView = (RoundedImageView) findViewById(R.id.shareImage);
        imageView.setImageBitmap(bitmap);
        yes.setOnClickListener(listener);
        close.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.closedialog:
            dismiss();
            break;

        default:
            break;
        }

    }

}
