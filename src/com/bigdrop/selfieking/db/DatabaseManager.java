package com.bigdrop.selfieking.db;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.Log;

import com.bigdropinc.selfieking.model.User;
import com.bigdropinc.selfieking.model.selfie.EditImage;
import com.bigdropinc.selfieking.model.selfie.SelfieImage;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;

public final class DatabaseManager {
    static DatabaseManager instance;
    private DatabaseHelper helper;

    public static void init(Context ctx) {
        if (null == instance) {
            instance = new DatabaseManager(ctx);
        }
    }

    public static DatabaseManager getInstance() {
        return instance;
    }

    private DatabaseManager(Context ctx) {
        helper = new DatabaseHelper(ctx);
    }

    public User addUser(User user) {
        try {
            helper.getUserDao().createOrUpdate(user);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return user;
    }

    public void updateUser(User user) {
        try {
            int i = helper.getUserDao().update(user);
            System.out.println(i);
        } catch (SQLException e) {

            e.printStackTrace();
        }
    }

    public void deleteUser(User note) {
        try {
            helper.getUserDao().delete(note);
        } catch (SQLException e) {

            e.printStackTrace();
        }
    }

    public void deleteSelfie(int id) {
        try {
            helper.getSelfieDao().deleteById(id);
        } catch (SQLException e) {

            e.printStackTrace();
        }
        Log.d("db", "delete" + id);
    }

    public List<EditImage> getAllTasks() {
        List<EditImage> tasks = null;
        try {
            tasks = helper.getSelfieDao().queryForAll();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tasks;
    }

    public EditImage addSelfie(EditImage editImage) {
        try {
            editImage = helper.getSelfieDao().createIfNotExists(editImage);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        Log.d("db", "add " + editImage.toString());
        return editImage;
    }

    public void updateSelfie(EditImage editImage) {
        int i = 0;
        try {
            i = helper.getSelfieDao().update(editImage);
            System.out.println(i);
        } catch (SQLException e) {

            e.printStackTrace();
        }
        Log.d("db", "update " + editImage.toString());

        Log.d("db", "update i= " + i);
    }

    public void addSelfies(List<EditImage> task) {
        try {
            for (EditImage image : task)
                helper.getSelfieDao().createIfNotExists(image);
        } catch (SQLException e) {

            e.printStackTrace();
        }
    }

    public void EditImage(EditImage task) {
        try {
            helper.getSelfieDao().update(task);
        } catch (SQLException e) {

            e.printStackTrace();
        }
    }

    public void removeSelfie(EditImage task) {
        try {
            helper.getSelfieDao().delete(task);
        } catch (SQLException e) {

            e.printStackTrace();
        }
    }

    public User findUser(String token) {
        try {

            QueryBuilder<User, Integer> queryBuilder = helper.getUserDao().queryBuilder();

            queryBuilder.where().eq("token", token);

            PreparedQuery<User> preparedQuery = queryBuilder.prepare();

            List<User> tasks = helper.getUserDao().query(preparedQuery);
            if (tasks.size() > 0)
                return tasks.get(0);
        } catch (SQLException e) {

            e.printStackTrace();
        }
        return new User(token);
    }

    public EditImage findEditImage(int id) {
        List<EditImage> tasks = new ArrayList<EditImage>();
        try {

            QueryBuilder<EditImage, Integer> queryBuilder = helper.getSelfieDao().queryBuilder();

            queryBuilder.where().eq("id", id);

            PreparedQuery<EditImage> preparedQuery = queryBuilder.prepare();

            tasks = helper.getSelfieDao().query(preparedQuery);

            if (tasks.size() > 0)
                return tasks.get(0);
        } catch (SQLException e) {

            e.printStackTrace();
        } catch (RuntimeException e) {

            e.printStackTrace();
        }
 
        Log.d("db", "get" + tasks.toString());
        return new EditImage(id);
    }
}
