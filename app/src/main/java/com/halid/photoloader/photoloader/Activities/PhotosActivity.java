package com.halid.photoloader.photoloader.Activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.halid.photoloader.photoloader.Adapters.PhotosAdapter;
import com.halid.photoloader.photoloader.Helpers.DownloadHelper;
import com.halid.photoloader.photoloader.Helpers.EndlessScrollListener;
import com.halid.photoloader.photoloader.Models.Album;
import com.halid.photoloader.photoloader.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class PhotosActivity extends AppCompatActivity {

    private GridView gridView;
    private ProgressBar progressBar;

    private FloatingActionButton fab;

    private AccessToken token;
    private Long albumId;

    private ArrayList<Album> arrAlbums = new ArrayList<> ();
    private static List<Long> selectedPhotos = new ArrayList<> (); //static because need to pass the same ref to the adapter
    private PhotosAdapter adapter;
    private GraphRequest nextRequest;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photos);

        token = AccessToken.getCurrentAccessToken();
        selectedPhotos.clear();

        progressBar = (ProgressBar) findViewById(R.id.viewLoading);
        gridView = (GridView) findViewById(R.id.photos_view);
        fab = (FloatingActionButton) findViewById(R.id.share_FAB);

        // getting albumId passed by the AlbumActivity
        Intent intent = getIntent();
        albumId = intent.getLongExtra("albumId", 0);
        setTitle(intent.getStringExtra("title") + " Album");

        gridView.setOnScrollListener(new EndlessScrollListener() {
            @Override
            public boolean onLoadMore(int page, int totalItemsCount) {
                // Triggered only when new data needs to be appended to the list
                if (nextRequest != null) {
                    Log.d("Loading more", String.valueOf(arrAlbums.size()));

                    Runnable run = new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),
                                    "Loading more photos", Toast.LENGTH_SHORT)
                                    .show();
                        }
                    };
                    PhotosActivity.this.runOnUiThread(run);
                    nextRequest.executeAsync();
                } else {
                    Runnable run = new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),
                                    "No more photos", Toast.LENGTH_SHORT)
                                    .show();
                        }
                    };
                    PhotosActivity.this.runOnUiThread(run);
                }
                return true; // ONLY if more data is actually being loaded; false otherwise.
            }
        });

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                CheckBox checkBox = (CheckBox) view.findViewById(R.id.grid_item_checkbox);
                if (selectedPhotos.contains(id)){
                    selectedPhotos.remove(id);
                    checkBox.setChecked(false);
                }else {
                    selectedPhotos.add(id);
                    checkBox.setChecked(true);
                }
            }
        });

        final PhotosActivity context = this;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fab.setVisibility(View.GONE);
                // call the helper and tell him to download our toffs
              new DownloadHelper(context, selectedPhotos).downlodPhotos();
            }
        });

        new getAlbumsData().execute();
    }

    // Handle the response
    public void HandleResponse(GraphResponse response) {
        try {
            Log.d("Calling", "HandleResponse");
            JSONArray JAAlbums = (response.getJSONObject()).getJSONArray("data");

            if (JAAlbums.length() == 0) {
                Runnable run = new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),
                                "No more photos", Toast.LENGTH_SHORT)
                                .show();
                    }
                };
                PhotosActivity.this.runOnUiThread(run);

            } else {
                nextRequest = response.getRequestForPagedResults(GraphResponse.PagingDirection.NEXT);

                if(nextRequest != null){
                    nextRequest.setCallback(new GraphRequest.Callback() {
                        @Override
                        public void onCompleted(GraphResponse response) {
                            HandleResponse(response);
                        }});
                }else
                {
                    Runnable run = new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),
                                    "No more photos",
                                    Toast.LENGTH_SHORT).show();
                        }
                    };
                    PhotosActivity.this.runOnUiThread(run);
                }

                ParseAlbums(JAAlbums);
                }
            } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Deserialize JsonArray to Albums Array
    private void ParseAlbums (JSONArray albumsArray){
        try {
            Log.d("ParseAlbums", String.valueOf(albumsArray.length()));

            for (int i = 0; i < albumsArray.length(); i++) {
                Album photo = new Album();

                JSONObject JAlbum = albumsArray.getJSONObject(i);

                photo.setCoverUrl(JAlbum.getString("picture"));
                photo.setId(JAlbum.getLong("id"));
                arrAlbums.add(photo);
                selectedPhotos.add(photo.getId());
                adapter.notifyDataSetChanged();
            }

            Log.d("Current size", String.valueOf(arrAlbums.size()));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // Get data asynchronously from facebook CDN
    private class getAlbumsData extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            // SHOW THE PROGRESS BAR (SPINNER) WHILE LOADING ALBUMS
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... params) {
            Log.d("Calling", "getAlbumsData");

            GraphRequest request = GraphRequest.newGraphPathRequest(token,
                    "/" + albumId +"/photos",
                    new GraphRequest.Callback() {
                        @Override
                        public void onCompleted(GraphResponse response) {HandleResponse(response);}
            });

            Bundle parameters = new Bundle();
            parameters.putString("fields", "id, picture");
            parameters.putString("limit", "6");
            request.setParameters(parameters);
            request.executeAsync();

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            // SET THE ADAPTER TO THE LISTVIEW
            Log.d("onPostExecute Pictures", String.valueOf(arrAlbums.size()));
            adapter = new PhotosAdapter(PhotosActivity.this, arrAlbums, selectedPhotos);
            gridView.setAdapter(adapter);

            // HIDE THE PROGRESS BAR (SPINNER) AFTER LOADING ALBUMS
            progressBar.setVisibility(View.GONE);
        }
    }

}
