package com.frankgh.popularmovies.themoviedb.model;

import android.content.ContentValues;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.frankgh.popularmovies.data.MoviesContract;
import com.frankgh.popularmovies.util.AndroidUtil;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

/**
 * An object representing a Movie
 * <p/>
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

    private Integer internalId;
    @SerializedName("backdrop_path")
    private String backdropPath;
    private Boolean adult;
    @SerializedName("genre_ids")
    private List<Integer> genreIds = new ArrayList<>();
    @SerializedName("id")
    private Integer movieId;
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
        movieId = AndroidUtil.readIntegerFromParcel(in);
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

    public Movie(String backdropPath, boolean adult, String genreIds, int movieId,
                 String originalLanguage, String originalTitle, String overview,
                 String releaseDate, String posterPath, double popularity,
                 String title, boolean video, double voteAverage, int voteCount, int internalId) {
        this.backdropPath = backdropPath;
        this.adult = adult;
        this.genreIds = new Gson()
                .fromJson(genreIds, new TypeToken<List<String>>() {
                }.getType());
        this.movieId = movieId;
        this.originalLanguage = originalLanguage;
        this.originalTitle = originalTitle;
        this.overview = overview;
        this.releaseDate = releaseDate;
        this.posterPath = posterPath;
        this.popularity = popularity;
        this.title = title;
        this.video = video;
        this.voteAverage = voteAverage;
        this.voteCount = voteCount;
        this.internalId = internalId;
    }

//    public Movie(int movieId, String title, double voteAverage, String backdropPath,
//                 String posterPath, String releaseDate, String overview) {
//        this.movieId = movieId;
//        this.title = title;
//        this.voteAverage = voteAverage;
//        this.backdropPath = backdropPath;
//        this.posterPath = posterPath;
//        this.releaseDate = releaseDate;
//        this.overview = overview;
//    }

    public Boolean getAdult() {
        return adult;
    }

    public String getBackdropPath() {
        return backdropPath;
    }

    public List<Integer> getGenreIds() {
        return genreIds;
    }

    public Integer getMovieId() {
        return movieId;
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

    public Integer getInternalId() {
        return internalId;
    }

    public void writeToParcel(Parcel out, int flags) {
        AndroidUtil.writeToParcel(backdropPath, out);
        AndroidUtil.writeToParcel(adult, out);
        AndroidUtil.writeToParcel(genreIds, out);
        AndroidUtil.writeToParcel(movieId, out);
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

    public boolean hasUpdates(ContentValues value) {
        return !TextUtils.equals(value.getAsString(MoviesContract.MovieEntry.COLUMN_BACKDROP_PATH), getBackdropPath()) ||
                value.getAsBoolean(MoviesContract.MovieEntry.COLUMN_ADULT) != getAdult() ||
                //!TextUtils.equals(value.getAsString(MoviesContract.MovieEntry.COLUMN_GENRE_IDS), movie.getGenreIds()) ||
                !movieId.equals(value.getAsInteger(MoviesContract.MovieEntry.COLUMN_MOVIE_ID)) ||
                !TextUtils.equals(value.getAsString(MoviesContract.MovieEntry.COLUMN_ORIGINAL_LANGUAGE), getOriginalLanguage()) ||
                !TextUtils.equals(value.getAsString(MoviesContract.MovieEntry.COLUMN_ORIGINAL_TITLE), getOriginalTitle()) ||
                !TextUtils.equals(value.getAsString(MoviesContract.MovieEntry.COLUMN_OVERVIEW), getOverview()) ||
                !TextUtils.equals(value.getAsString(MoviesContract.MovieEntry.COLUMN_RELEASE_DATE), getReleaseDate()) ||
                !TextUtils.equals(value.getAsString(MoviesContract.MovieEntry.COLUMN_POSTER_PATH), getPosterPath()) ||
                Double.compare(value.getAsDouble(MoviesContract.MovieEntry.COLUMN_POPULARITY), getPopularity()) != 0 ||
                !TextUtils.equals(value.getAsString(MoviesContract.MovieEntry.COLUMN_TITLE), getTitle()) ||
                value.getAsBoolean(MoviesContract.MovieEntry.COLUMN_VIDEO) != getVideo() ||
                Double.compare(value.getAsDouble(MoviesContract.MovieEntry.COLUMN_VOTE_AVERAGE), getVoteAverage()) != 0 ||
                !voteCount.equals(value.getAsInteger(MoviesContract.MovieEntry.COLUMN_VOTE_COUNT));
    }
}
