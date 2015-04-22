package com.bigdropinc.selfieking.activities.login;

import java.util.concurrent.ExecutionException;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.Loader;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.bigdropinc.selfieking.R;
import com.bigdropinc.selfieking.controller.loaders.Command;
import com.bigdropinc.selfieking.controller.loaders.CommandLoader;
import com.bigdropinc.selfieking.model.User;
import com.bigdropinc.selfieking.model.responce.StatusCode;

public class ResetPassActivity extends Activity implements LoaderManager.LoaderCallbacks<StatusCode> {
    private int LOADER_ID = 2;
    protected StatusCode code;
    protected CommandLoader loader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_pass);
        final EditText email = (EditText) findViewById(R.id.resetEmailEditText);
        Button send = (Button) findViewById(R.id.resetPasswordSendButton);
        Button close = (Button) findViewById(R.id.changePasswordBack);
        close.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
               onBackPressed();
                
            }
        });
        initListeners(email, send);
    }

    private void initListeners(final EditText email, Button send) {
        send.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                resetPass(email);
            }
        });
    }

    @Override
    public Loader<StatusCode> onCreateLoader(int id, Bundle args) {
        if (id == LOADER_ID) {
            loader = new CommandLoader(this, args);
            Log.d("tag", "onCreateLoader: " + loader.hashCode());
        }
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<StatusCode> arg0, StatusCode arg1) {
        Toast.makeText(this, "Check your email,  please", Toast.LENGTH_LONG).show();
        this.finish();

    }

    @Override
    public void onLoaderReset(Loader<StatusCode> arg0) {
        // TODO Auto-generated method stub

    }

    private void resetPass(final EditText email) {
        if (email.getText().toString().isEmpty()) {
            Toast.makeText(ResetPassActivity.this, "Type email, please", Toast.LENGTH_LONG).show();
        } else {
            Bundle bundle = new Bundle();
            User user = new User();
            user.setEmail(email.getText().toString());
            bundle.putParcelable("command", new Command(Command.RESET_PASSWORD, user));
            getLoaderManager().initLoader(LOADER_ID, bundle, ResetPassActivity.this).forceLoad();

            try {
                code = loader.registrTask.get();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (ExecutionException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

}
