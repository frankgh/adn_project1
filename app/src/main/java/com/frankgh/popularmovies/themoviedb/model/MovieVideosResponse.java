package com.frankgh.popularmovies.themoviedb.model;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Francisco Guerrero <email>me@frankgh.com</email> on 12/18/15.
 */
public class MovieVideosResponse {

    private Integer id;
    private List<Video> results = new ArrayList<Video>();

    public Integer getId() {
        return id;
    }

    public List<Video> getResults() {
        return results;
    }

    @Override
    public String toString() {
        return "MovieVideosResponse{" +
                "id=" + id +
                ", results=" + results +
                '}';
    }
}