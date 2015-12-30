package com.frankgh.popularmovies.themoviedb.model;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Francisco Guerrero <email>me@frankgh.com</email> on 12/18/15.
 */
public class MovieReviewsResponse {

    private Integer id;
    private Integer page;
    private List<Review> results = new ArrayList<>();
    private Integer totalPages;
    private Integer totalResults;

    public Integer getId() {
        return id;
    }

    public Integer getPage() {
        return page;
    }

    public List<Review> getResults() {
        return results;
    }

    public Integer getTotalPages() {
        return totalPages;
    }

    public Integer getTotalResults() {
        return totalResults;
    }

    @Override
    public String toString() {
        return "MovieReviewsResponse{" +
                "id=" + id +
                ", page=" + page +
                ", results=" + results +
                ", totalPages=" + totalPages +
                ", totalResults=" + totalResults +
                '}';
    }
}