package com.bigdropinc.selfieking.controller.managers;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;

public class DialogManager {

	public static void displayAlert(Activity activity, int res) {
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setMessage(res).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
			}
		});
		AlertDialog d = builder.create();
		d.show();
	}
}
