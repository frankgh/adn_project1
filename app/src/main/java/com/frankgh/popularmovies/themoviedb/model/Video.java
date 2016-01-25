package com.frankgh.popularmovies.themoviedb.model;

import com.google.gson.annotations.SerializedName;

/**
 * @author Francisco Guerrero <email>me@frankgh.com</email> on 12/18/15.
 */
public class Video {

    private static final String YOU_TUBE_THUMBNAIL_URL = "http://img.youtube.com/vi/%s/hqdefault.jpg";

    private String id;
    @SerializedName("iso_639_1")
    private String iso6391;
    private String key;
    private String name;
    private String site;
    private Integer size;
    private String type;

    public String getId() {
        return id;
    }

    public String getIso6391() {
        return iso6391;
    }

    public String getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    public String getSite() {
        return site;
    }

    public Integer getSize() {
        return size;
    }

    public String getType() {
        return type;
    }

    public String getYouTubeThumbnailUrl() {
        if ("YouTube".equals(getSite())) {
            return String.format(YOU_TUBE_THUMBNAIL_URL, getKey());
        }
        return null;
    }

    @Override
    public String toString() {
        return "Video{" +
                "id='" + id + '\'' +
                ", iso6391='" + iso6391 + '\'' +
                ", key='" + key + '\'' +
                ", name='" + name + '\'' +
                ", site='" + site + '\'' +
                ", size=" + size +
                ", type='" + type + '\'' +
                '}';
    }
}