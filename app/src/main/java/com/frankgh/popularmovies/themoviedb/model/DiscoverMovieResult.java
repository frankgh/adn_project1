package com.frankgh.popularmovies.themoviedb.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.frankgh.popularmovies.util.AndroidUtil;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by francisco on 11/25/15.
 */
public class DiscoverMovieResult implements Parcelable {

    // this is used to regenerate your object. All Parcelables must have a CREATOR that implements these two methods
    public static final Parcelable.Creator<DiscoverMovieResult> CREATOR = new Parcelable.Creator<DiscoverMovieResult>() {
        public DiscoverMovieResult createFromParcel(Parcel in) {
            return new DiscoverMovieResult(in);
        }

        public DiscoverMovieResult[] newArray(int size) {
            return new DiscoverMovieResult[size];
        }
    };

    private final String LOG_TAG = DiscoverMovieResult.class.getSimpleName();
    private final String DEFAULT_IMAGE_SIZE = "w342";

    @SerializedName("backdrop_path")
    private String backdropPath;
    private Boolean adult;
    @SerializedName("genre_ids")
    private List<Integer> genreIds = new ArrayList<Integer>();
    private Integer id;
    @SerializedName("original_language")
    private String originalLanguage;
    @SerializedName("original_title")
    private String originalTitle;
    private String overview;
    @SerializedName("release_date")
    private String releaseDate;
    @SerializedName("poster_path")
    private String posterPath;
    private Double popularity;
    private String title;
    private Boolean video;
    @SerializedName("vote_average")
    private Double voteAverage;
    @SerializedName("vote_count")
    private Integer voteCount;

    private DiscoverMovieResult(Parcel in) {
        backdropPath = AndroidUtil.readStringFromParcel(in);
        adult = AndroidUtil.readBooleanFromParcel(in);
        genreIds = AndroidUtil.readIntegerListFromParcel(in);
        id = AndroidUtil.readIntegerFromParcel(in);
        originalLanguage = AndroidUtil.readStringFromParcel(in);
        originalTitle = AndroidUtil.readStringFromParcel(in);
        overview = AndroidUtil.readStringFromParcel(in);
        releaseDate = AndroidUtil.readStringFromParcel(in);
        posterPath = AndroidUtil.readStringFromParcel(in);
        popularity = AndroidUtil.readDoubleFromParcel(in);
        title = AndroidUtil.readStringFromParcel(in);
        video = AndroidUtil.readBooleanFromParcel(in);
        voteAverage = AndroidUtil.readDoubleFromParcel(in);
        voteCount = AndroidUtil.readIntegerFromParcel(in);
    }

    public Boolean getAdult() {
        return adult;
    }

    public String getBackdropPath() {
        return backdropPath;
    }

    public List<Integer> getGenreIds() {
        return genreIds;
    }

    public Integer getId() {
        return id;
    }

    public String getOriginalLanguage() {
        return originalLanguage;
    }

    public String getOriginalTitle() {
        return originalTitle;
    }

    public String getOverview() {
        return overview;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public String getPosterPath() {
        return posterPath;
    }

    public Double getPopularity() {
        return popularity;
    }

    public String getTitle() {
        return title;
    }

    public Boolean getVideo() {
        return video;
    }

    public Double getVoteAverage() {
        return voteAverage;
    }

    public Integer getVoteCount() {
        return voteCount;
    }

    /**
     * Returns the Absolute Path to the poster image with a default size
     *
     * @return the absolute path
     */
    public String getPosterAbsolutePath() {
        return getPosterAbsolutePath(DEFAULT_IMAGE_SIZE);
    }

    /**
     * Returns the Absolute Path to the poster image. Possible sizes are:
     * w92, w154, w185, w342", "w500", "w780", or "original"
     *
     * @param imageSize one of the possible sizes: w92, w154, w185, w342, w500, w780, or original
     * @return the absolute path for the given imageSize
     */
    public String getPosterAbsolutePath(String imageSize) {
        return "http://image.tmdb.org/t/p/" + imageSize + getPosterPath();
    }

    /**
     * Returns the Absolut Path the the backdrop image with a default size
     *
     * @return the absolute path
     */
    public String getBackDropAbsolutePath() {
        return getBackDropAbsolutePath(DEFAULT_IMAGE_SIZE);
    }

    /**
     * Returns the Absolute Path to the backdrop image. Possible sizes are:
     * w92, w154, w185, w342", "w500", "w780", or "original"
     *
     * @param imageSize one of the possible sizes: w92, w154, w185, w342, w500, w780, or original
     * @return the absolute path for the given imageSize
     */
    public String getBackDropAbsolutePath(String imageSize) {
        return "http://image.tmdb.org/t/p/" + imageSize + getBackdropPath();
    }

    public void writeToParcel(Parcel out, int flags) {
        AndroidUtil.writeToParcel(backdropPath, out);
        AndroidUtil.writeToParcel(adult, out);
        AndroidUtil.writeToParcel(genreIds, out);
        AndroidUtil.writeToParcel(id, out);
        AndroidUtil.writeToParcel(originalLanguage, out);
        AndroidUtil.writeToParcel(originalTitle, out);
        AndroidUtil.writeToParcel(overview, out);
        AndroidUtil.writeToParcel(releaseDate, out);
        AndroidUtil.writeToParcel(posterPath, out);
        AndroidUtil.writeToParcel(popularity, out);
        AndroidUtil.writeToParcel(title, out);
        AndroidUtil.writeToParcel(video, out);
        AndroidUtil.writeToParcel(voteAverage, out);
        AndroidUtil.writeToParcel(voteCount, out);
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
