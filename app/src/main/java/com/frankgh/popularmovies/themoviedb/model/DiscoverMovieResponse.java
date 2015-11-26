package com.frankgh.popularmovies.themoviedb.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by francisco on 11/25/15.
 */
public class DiscoverMovieResponse {

    private List<DiscoverMovieResult> results = new ArrayList<DiscoverMovieResult>();
    @SerializedName("total_pages")
    private Integer totalPages;
    @SerializedName("total_results")
    private Integer totalResults;

    public List<DiscoverMovieResult> getResults() {
        return results;
    }

    public Integer getTotalPages() {
        return totalPages;
    }

    public Integer getTotalResults() {
        return totalResults;
    }
}
