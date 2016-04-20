package com.halid.photoloader.photoloader.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.halid.photoloader.photoloader.Models.Album;
import com.halid.photoloader.photoloader.R;

import java.util.ArrayList;

public class AlbumsAdapter extends BaseAdapter {
    private final Context mContext;
    private static LayoutInflater inflater=null;

    // Constructor
    public AlbumsAdapter(Context ctx, ArrayList<Album> albums) {
        mContext = ctx;
        inflater = ( LayoutInflater )ctx.
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        Albums = albums;
    }

    public int getCount() {
        return Albums.size();
    }

    public Object getItem(int position) {
        return Albums.get(position);
    }

    public long getItemId(int position) {
        return Albums.get(position).getId();
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(final int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.cell, null);
        }

        //load the album from the array
        Album bum = Albums.get(position);

        //get view template references
        ImageView coverView = (ImageView) convertView.findViewById(R.id.coverView);
        TextView titleView = (TextView) convertView.findViewById(R.id.titleView);

        titleView.setText(bum.getTitle());

        //Librairy to help manupilating images see github.com/bumptech/glide
        //download the cover image and load into the imageView
         Glide
             .with(mContext)
             .load(bum.getCoverUrl())
             .placeholder(R.drawable.com_facebook_button_icon_blue)
             .centerCrop()
             .crossFade()
             .into(coverView);

        return convertView;
    }

    // Keep all Images in array
    public ArrayList<Album> Albums = new ArrayList<> ();
}

