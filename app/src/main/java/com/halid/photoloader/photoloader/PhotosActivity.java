package com.halid.photoloader.photoloader;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AbsListView;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.halid.photoloader.photoloader.Helpers.PhotosAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class PhotosActivity extends AppCompatActivity {

    private GridView gridView;
    private AccessToken token;
    private Long albumId;

    private boolean stopLoadingData;
    private boolean loadingMore;

    private String URL;
    private String pagingURL;

    private ProgressBar progressBar;
    private int current_page;

    private ArrayList<Album> arrAlbums = new ArrayList<> ();
    private PhotosAdapter adapter;
    private GraphRequest nextRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photos);

        token = AccessToken.getCurrentAccessToken();
        progressBar = (ProgressBar) findViewById(R.id.viewLoading);
        gridView = (GridView) findViewById(R.id.photos_view);

        // getting albumId passed by the AlbumActivity
        Intent intent = getIntent();
        albumId = intent.getLongExtra("albumId", 0);

        //Infinit scroll stuff
        gridView.setOnScrollListener(new AbsListView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {
                int lastInScreen = firstVisibleItem + visibleItemCount;
                if ((lastInScreen == totalItemCount) && !loadingMore) {
                    if (!stopLoadingData) {
                        // FETCH THE NEXT BATCH OF FEEDS
                        new loadMoreAlbums().execute();
                    }
                }
            }
        });


//        gridView.setOnScrollListener(new EndlessScrollListener() {
//            @Override
//            public boolean onLoadMore(int page, int totalItemsCount) {
//                // Triggered only when new data needs to be appended to the list
//                // Add whatever code is needed to append new items to your AdapterView
//                // or customLoadMoreDataFromApi(totalItemsCount);
//
//                Log.v("onLoadMore", String.valueOf(nextRequest != null));
//
//                if (nextRequest != null) {
//                  nextRequest.executeAsync();
//                }
//                return true; // ONLY if more data is actually being loaded; false otherwise.
//            }
//        });

       new getAlbumsData().execute();
    }


    public void HandleResponse(GraphResponse response) {
        try {
            JSONObject JOTemp = response.getJSONObject();

            JSONArray JAAlbums = JOTemp.getJSONArray("data");

            if (JAAlbums.length() == 0) {
                stopLoadingData = true;
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

                if (nextRequest == null){
                    stopLoadingData = true;
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
            for (int i = 0; i < albumsArray.length(); i++) {
                Album photo = new Album();

                JSONObject JAlbum = albumsArray.getJSONObject(i);

                photo.setCoverUrl(JAlbum.getString("picture"));
                photo.setId(JAlbum.getLong("id"));
                arrAlbums.add(photo);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private class getAlbumsData extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            // SHOW THE PROGRESS BAR (SPINNER) WHILE LOADING ALBUMS
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... params) {

            // CHANGE THE LOADING MORE STATUS TO PREVENT DUPLICATE CALLS FOR
            // MORE DATA WHILE LOADING A BATCH
            loadingMore = true;

            GraphRequest request = GraphRequest.newGraphPathRequest(token,
                    "/" + albumId +"/photos",
                    new GraphRequest.Callback() {
                        @Override
                        public void onCompleted(GraphResponse response) {HandleResponse(response);}
            });

            Bundle parameters = new Bundle();
            parameters.putString("fields", "id, picture");
            parameters.putString("limit", "10");
            request.setParameters(parameters);
            request.executeAsync();

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            // SET THE ADAPTER TO THE LISTVIEW
            gridView.setAdapter(adapter);

            // CHANGE THE LOADING MORE STATUS
            loadingMore = false;

            // HIDE THE PROGRESS BAR (SPINNER) AFTER LOADING ALBUMS
            progressBar.setVisibility(View.GONE);
        }
    }


    private class loadMoreAlbums extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            // SHOW THE BOTTOM PROGRESS BAR (SPINNER) WHILE LOADING MORE ALBUMS
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... params) {
            // SET LOADING MORE "TRUE"
            loadingMore = true;

            // INCREMENT CURRENT PAGE
            current_page += 1;

            GraphRequest request = GraphRequest.newGraphPathRequest(
                    token, "/" + albumId +"/photos",
                    new GraphRequest.Callback() {
                        @Override
                        public void onCompleted(GraphResponse response) {HandleResponse(response);};
                 });

            Bundle parameters = new Bundle();
            parameters.putString("fields", "id, picture");
            parameters.putString("limit", "10");
            request.setParameters(parameters);
            request.executeAsync();

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

            // get listview current position - used to maintain scroll position
            int currentPosition = gridView.getFirstVisiblePosition();

            // APPEND NEW DATA TO THE ARRAYLIST AND SET THE ADAPTER TO THE
            // GRIDVIEW
            adapter = new PhotosAdapter(PhotosActivity.this, arrAlbums);
            gridView.setAdapter(adapter);

            // Setting new scroll position
            gridView.setSelectionFromTop(currentPosition + 1, 0);

            // SET LOADINGMORE "FALSE" AFTER ADDING NEW FEEDS TO THE EXISTING LIST
            loadingMore = false;

            // HIDE PROGRESS BAR (SPINNER) AFTER LOADING MORE PHOTOS
            progressBar.setVisibility(View.GONE);
        }

    }
}
