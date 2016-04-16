package com.halid.photoloader.photoloader;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;

public class AlbumsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_albums);

        //Intent intent = getIntent();
       // String userId = intent.getStringExtra("UserId");
        //String token = intent.getStringExtra("AccessToken");

        AccessToken accessToken = AccessToken.getCurrentAccessToken();

        GraphRequest request = GraphRequest.newGraphPathRequest(
                accessToken,
                "/"+ accessToken.getUserId() + "/albums",
                new GraphRequest.Callback() {
                    @Override
                    public void onCompleted(GraphResponse response)
                    {
                        // Insert your code here
                    }
                });

        request.executeAsync();


    }
}
