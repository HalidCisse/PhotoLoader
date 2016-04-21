package com.halid.photoloader.photoloader.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.halid.photoloader.photoloader.Helpers.Helper;
import com.halid.photoloader.photoloader.R;

import java.util.Arrays;
import java.util.List;

public class LoginActivity extends AppCompatActivity {

    private LoginButton loginButton;
    private CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this.getApplication());

        setContentView(R.layout.activity_login);

        // check if connection available, no internet NO LIFE
        Helper.EnsureConnected(this);

        //if session expired request new login
        if (AccessToken.getCurrentAccessToken() == null || ( AccessToken.getCurrentAccessToken()).isExpired()){
            callbackManager = CallbackManager.Factory.create();

            loginButton = (LoginButton) findViewById(R.id.login_button);
            List<String> permissionNeeds = Arrays.asList("user_photos", "email", "public_profile");
            loginButton.setReadPermissions(permissionNeeds);
            loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                @Override
                public void onSuccess(LoginResult loginResult) {
                    Intent albumsView = new Intent(LoginActivity.this, AlbumsActivity.class);
                    albumsView.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(albumsView);
                }
                @Override
                public void onCancel() {
                }
                @Override
                public void onError(FacebookException exception) {
                    exception.printStackTrace();
                }
            });
        }else {
            Intent albumsView = new Intent(LoginActivity.this, AlbumsActivity.class);
            albumsView.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(albumsView);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

}
