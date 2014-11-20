package com.bigdropinc.selfieking.controller.asynctasks;

import android.graphics.Bitmap;
import android.os.AsyncTask;

import com.bigdropinc.selfieking.controller.managers.FileManager;
import com.bigdropinc.selfieking.model.ParamsUri;

public class LoadBitmap extends AsyncTask<ParamsUri, Void, Bitmap> {


	@Override
	protected Bitmap doInBackground(ParamsUri... params) {
		ParamsUri paramsUri = params[0];
		return FileManager.getImageFromUri(paramsUri.getUri(), paramsUri.getWidth(), paramsUri.getHeight());
	}

}
