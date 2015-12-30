package com.frankgh.popularmovies.themoviedb.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.frankgh.popularmovies.util.AndroidUtil;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * An object representing a Movie
 *
 * Created by francisco on 11/25/15.
 */
public class Movie implements Parcelable {

    // this is used to regenerate your object. All Parcelables must have a CREATOR that implements these two methods
    public static final Parcelable.Creator<Movie> CREATOR = new Parcelable.Creator<Movie>() {
        public Movie createFromParcel(Parcel in) {
            return new Movie(in);
        }

        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };

    private final String LOG_TAG = Movie.class.getSimpleName();

    @SerializedName("backdrop_path")
    private String backdropPath;
    private Boolean adult;
    @SerializedName("genre_ids")
    private List<Integer> genreIds = new ArrayList<>();
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

    public Movie(Parcel in) {
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

    public Movie(int id, String title, double voteAverage, String backdropPath,
                 String posterPath, String releaseDate, String overview) {
        this.id = id;
        this.title = title;
        this.voteAverage = voteAverage;
        this.backdropPath = backdropPath;
        this.posterPath = posterPath;
        this.releaseDate = releaseDate;
        this.overview = overview;
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
