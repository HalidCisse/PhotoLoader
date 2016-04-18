package com.halid.photoloader.photoloader.Adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.halid.photoloader.photoloader.Models.Album;
import com.halid.photoloader.photoloader.R;

import java.util.ArrayList;
import java.util.List;

public class PhotosAdapter extends BaseAdapter {
    private final Context mContext;
    private static List<Long> selectList;
    private static LayoutInflater inflater;

    // Constructor
    public PhotosAdapter(Context ctx, ArrayList<Album> photos, List<Long> selectList) {
        mContext = ctx;
        this.selectList = selectList;
        inflater = ( LayoutInflater )ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        ArrPhotos = photos;
    }

    public int getCount() {
        //System.out.println("getCount " + ArrPhotos.size());
        if (ArrPhotos == null){
            return 0;
        }
        return ArrPhotos.size();
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return ArrPhotos.get(position).getId();
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(final int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.image_item, null);
        }

        final Album photo = ArrPhotos.get(position);

        //get view references
        ImageView coverView = (ImageView) convertView.findViewById(R.id.grid_item_image);

        // if photo checked we need to map it
        final CheckBox checkBox = (CheckBox) convertView.findViewById(R.id.grid_item_checkbox);
        checkBox.setOnClickListener( new View.OnClickListener() {
            public void onClick(View v) {
                if (checkBox.isChecked() && !selectList.contains(photo.getId()))
                {
                    selectList.add(photo.getId());
                    Log.d("Add photo getView", String.valueOf(photo.getId()));
                }else{
                    selectList.remove(photo.getId());
                    Log.d("Remove photo getView", String.valueOf(photo.getId()));
                }
            }
        });

        //Library to help images manipulation see github.com/bumptech/glide
        //Download the cover image and load into the imageView
        Glide
            .with(mContext)
            .load(photo.getCoverUrl())
            .placeholder(R.drawable.com_facebook_button_icon_blue)
            .centerCrop()
            .crossFade()
            .into(coverView);

        return convertView;
    }

    // Keep all Images in array
    public ArrayList<Album> ArrPhotos = new ArrayList<> ();
}

