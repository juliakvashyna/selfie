package com.bigdrop.selfieking.db;

import java.sql.SQLException;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.bigdropinc.selfieking.model.User;
import com.bigdropinc.selfieking.model.selfie.EditImage;
import com.bigdropinc.selfieking.model.selfie.SelfieImage;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

public class DatabaseHelper extends OrmLiteSqliteOpenHelper {
	private static final String DATABASE_NAME = "TaskManager.sqlite";
	private static final int DATABASE_VERSION = 1;
	private static final String TAG = DatabaseHelper.class.getSimpleName();

	private Dao<User, Integer> userDao = null;
	private Dao<EditImage, Integer> selfieDao = null;


	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);

	}

	@Override
	public void onCreate(SQLiteDatabase db, ConnectionSource connectionSource) {
		try {
			TableUtils.createTableIfNotExists(connectionSource, User.class);
			TableUtils.createTableIfNotExists(connectionSource, EditImage.class);

		} catch (SQLException e) {
			Log.e(TAG, "error creating DB " + DATABASE_NAME);
			throw new RuntimeException(e);
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource, int oldV, int newV) {
		Log.i(TAG, "onUpgrade");
		try {
			TableUtils.dropTable(connectionSource, User.class, true);
			TableUtils.dropTable(connectionSource, EditImage.class, true);
			onCreate(db, connectionSource);
		} catch (SQLException e) {
			Log.e(TAG, "Can't drop databases", e);
			throw new RuntimeException(e);

		}
	}


	public Dao<User, Integer> getUserDao() throws SQLException {
		if (userDao == null) {
			userDao = getDao(User.class);
		}
		return userDao;
	}

	public Dao<EditImage, Integer> getSelfieDao() throws SQLException {
		if (selfieDao == null) {
			selfieDao = getDao(EditImage.class);
		}
		return selfieDao;
	}

	
}
