package com.halid.photoloader.photoloader.Activities;

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
import com.halid.photoloader.photoloader.Adapters.AlbumsAdapter;
import com.halid.photoloader.photoloader.Models.Album;
import com.halid.photoloader.photoloader.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class AlbumsActivity extends AppCompatActivity {

    private GridView gridView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_albums);
        setTitle("My albums");

        gridView = (GridView) findViewById(R.id.gridview);

        //when album click
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                //Send intent to PhotosActivity
                Intent intent = new Intent(getApplicationContext(), PhotosActivity.class);

                //Pass album index
                intent.putExtra("albumId", id);
                intent.putExtra("title", ((Album) gridView.getAdapter().getItem(position)).getTitle());
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
                            if (response.getJSONObject() == null || !(response.getJSONObject()).has("data")){
                                return;
                            }
                            ArrayList<Album> Albums = new ArrayList<>();
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

                            // Sorting albums by names
                            Collections.sort(Albums, new Comparator<Album>() {
                                public int compare(Album lhs, Album rhs) {
                                    return lhs.getTitle().compareToIgnoreCase(rhs.getTitle());
                                }});

                            Log.v("Graph Albums", String.valueOf(Albums.size()));
                            gridView.setAdapter(new AlbumsAdapter(parent, Albums));
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