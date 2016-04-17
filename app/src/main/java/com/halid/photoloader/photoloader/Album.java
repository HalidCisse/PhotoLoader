package com.halid.photoloader.photoloader;

public class Album
{
    private String _title;
    private long _id;
    private String _cover;

    public Album (String title, long id, String coverUrl){
        this._title = title;
        this._id = id;
        this._cover = coverUrl;
    }

    public String getTitle() {
        return _title;
    }
    public void setTitle(String title) {
        this._title = title;
    }

    public long getId() {
        return _id;
    }
    public void setId(long id) {
        this._id = id;
    }

    public String getCoverUrl() {
        return _cover;
    }
    public void setCoverUrl(String coverUrl) {
        this._cover = coverUrl;
    }
}
