package com.halid.photoloader.photoloader;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class AlbumsActivity extends AppCompatActivity {

    private GridView gridView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_albums);

        gridView = (GridView) findViewById(R.id.gridview);

        //when album click
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {

                //Send intent to PhotosActivity
                Intent intent = new Intent(getApplicationContext(), PhotosActivity.class);

                //Pass album index
                intent.putExtra("albumId", id);
                startActivity(intent);
            }
        });

        final AlbumsActivity parent = this;
        final AccessToken accessToken = AccessToken.getCurrentAccessToken();

        //facebook graph API
        GraphRequest request = GraphRequest.newGraphPathRequest(accessToken, "/" + accessToken.getUserId() + "/albums",
                new GraphRequest.Callback() {
                    @Override
                    public void onCompleted(GraphResponse response) {
                        try {
                            ArrayList<Album> Albums = new ArrayList<>();
                            Log.d("Token", accessToken.getToken());
                            JSONArray albums = (response.getJSONObject()).getJSONArray("data");

                            for (int i = 0; i < albums.length(); i++) {
                                final JSONObject albumJson = albums.getJSONObject(i);

                                final Album album = new Album(
                                        albumJson.getString("name"),
                                        albumJson.getLong("id"), "");

                                if (albumJson.has("cover_photo")) {
                                    album.setCoverUrl("https://graph.facebook.com/"
                                            + (albumJson.getJSONObject("cover_photo")).getString("id")
                                            + "/picture?type=album"
                                            + "&access_token="
                                            + accessToken.getToken());
                                }
                                Albums.add(album);
                            }

                            Log.v("Graph Albums", String.valueOf(Albums.size()));
                            gridView.setAdapter(new GridAdapter(parent, Albums));

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });

        Bundle parameters = new Bundle();
        parameters.putString("fields", "cover_photo, name");
        request.setParameters(parameters);
        request.executeAsync();
    }

}