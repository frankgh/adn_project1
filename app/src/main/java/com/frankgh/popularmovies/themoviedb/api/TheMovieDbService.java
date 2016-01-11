package com.frankgh.popularmovies.themoviedb.api;

import com.frankgh.popularmovies.themoviedb.model.DiscoverMovieResponse;
import com.frankgh.popularmovies.themoviedb.model.MovieReviewsResponse;
import com.frankgh.popularmovies.themoviedb.model.MovieVideosResponse;

import retrofit.Call;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.http.Query;

/**
 * TheMovieDb Service Interface
 * <p/>
 * Created by francisco on 11/24/15.
 */
public interface TheMovieDbService {

    String SORT_BY_POPULARITY = "popularity";
    String SORT_BY_VOTE_AVERAGE = "vote_average";
    String SORT_BY_RELEASE_DATE = "release_date";
    String SORT_BY_REVENUE = "revenue";
    String SORT_BY_PRIMARY_RELEASE_DATE = "primary_release_date";
    String SORT_BY_ORIGINAL_TITLE = "original_title";
    String SORT_BY_VOTE_COUNT = "vote_count";

    String SORT_ORDER_ASC = "asc";
    String SORT_ORDER_DESC = "desc";

    @POST("/3/discover/movie")
    Call<DiscoverMovieResponse> discoverMovies(
            @Query("sort_by") String sortBy);

    @GET("/3/movie/{id}/videos")
    Call<MovieVideosResponse> movieVideos(
            @Path("id") String id);

    @GET("/3/movie/{id}/reviews")
    Call<MovieReviewsResponse> movieReviews(
            @Path("id") String id,
            @Query("page") Integer page,
            @Query("append_to_response") String appendToResponse);

}
