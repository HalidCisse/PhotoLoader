package com.halid.photoloader.photoloader.Helpers;

import android.os.AsyncTask;
import android.os.Bundle;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.halid.photoloader.photoloader.Album;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by HALID on 4/16/16.
 */
public class AlbumsHelpers extends AsyncTask<Void, Void, Void> {

    public ArrayList<Album> Albums = new ArrayList<> ();


    @Override
    protected Void doInBackground(Void... params) {

        final AccessToken accessToken = AccessToken.getCurrentAccessToken();

        GraphRequest request = GraphRequest.newGraphPathRequest( accessToken, "/" + accessToken.getUserId() + "/albums",
                new GraphRequest.Callback() {
                    @Override
                    public void onCompleted(GraphResponse response) {
                        try {
                            JSONObject datas = response.getJSONObject();

                            JSONArray albums = datas.getJSONArray("data");
                            for (int i = 0; i < albums.length(); i++) {

                                final JSONObject albumJson = albums.getJSONObject(i);
                                //System.out.println("albumJson " + albumJson.toString(1));

                                final Album album = new Album(
                                        albumJson.getString("name"),
                                        albumJson.getLong("id"),"");

                                if (albumJson.has("cover_photo")) {
                                    String coverUrl ="https://graph.facebook.com/"
                                            + (albumJson.getJSONObject("cover_photo")).getString("id")
                                            + "/picture?type=album"
                                            + "&access_token="
                                            + accessToken.getToken();
                                    album.setCoverUrl(coverUrl);

                                    //Log.d("Cover Url", albumJson.getString("name") + " " + coverUrl);
                                }

                                //Log.d("New Album" , albumJson.getString("name"));
                                Albums.add(album);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });

        Bundle parameters = new Bundle();
        parameters.putString("fields", "cover_photo, name");
        request.setParameters(parameters);
        request.executeAndWait();

        System.out.println("Got " + Albums.size() + " Albums");
        return null;
    }
}

