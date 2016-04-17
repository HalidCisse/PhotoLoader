package com.halid.photoloader.photoloader;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.GridView;

import com.facebook.AccessToken;

public class PhotosActivity extends AppCompatActivity {

    private GridView gridView;
    private AccessToken token;
    private Long albumId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photos);

        token = AccessToken.getCurrentAccessToken();
        Intent intent = getIntent();
        albumId = Long.getLong(intent.getStringExtra("albumId"), 0) ;

        Log.d("Photos Activity opened with album Id ", albumId.toString());
    }
}
