package com.halid.photoloader.photoloader.Activities;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
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

        //check if connection available
        if (!isNetworkAvailable()){
             new Runnable() {

                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(),
                            "No internet", Toast.LENGTH_LONG)
                            .show();
                }
            };
            return;
        }

        //if session expired request new login
        if (AccessToken.getCurrentAccessToken() == null || ( AccessToken.getCurrentAccessToken()).isExpired()){

        callbackManager = CallbackManager.Factory.create();

        loginButton = (LoginButton) findViewById(R.id.login_button);
        List<String> permissionNeeds = Arrays.asList("user_photos", "email", "user_birthday", "public_profile");
        loginButton.setReadPermissions(permissionNeeds);
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                AccessToken token = loginResult.getAccessToken();
                //token.getUserId()
                System.out.println("onSuccess");
            }

            @Override
            public void onCancel() {
                System.out.println("onCancel");
            }

            @Override
            public void onError(FacebookException exception) {
                Log.v("LoginActivity", exception.getCause().toString());
            }
        });

        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        System.out.println("onSuccess");
                        AccessToken token = loginResult.getAccessToken();

                        Intent albumsView = new Intent(LoginActivity.this, AlbumsActivity.class);
                        albumsView .putExtra("UserId", token.getUserId());
                        albumsView .putExtra("AccessToken", token.getToken());
                        albumsView.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(albumsView);
                    }

                    @Override
                    public void onCancel() {
                        // App code
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        // App code
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


    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
