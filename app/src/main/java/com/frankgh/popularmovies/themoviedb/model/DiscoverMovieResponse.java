package com.frankgh.popularmovies.themoviedb.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.frankgh.popularmovies.util.AndroidUtil;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * DiscoverMovieResponse class
 *
 * Created by francisco on 11/25/15.
 */
public class DiscoverMovieResponse implements Parcelable {

    // this is used to regenerate your object. All Parcelables must have a CREATOR that implements these two methods
    public static final Parcelable.Creator<DiscoverMovieResponse> CREATOR = new Parcelable.Creator<DiscoverMovieResponse>() {
        public DiscoverMovieResponse createFromParcel(Parcel in) {
            return new DiscoverMovieResponse(in);
        }

        public DiscoverMovieResponse[] newArray(int size) {
            return new DiscoverMovieResponse[size];
        }
    };

    private final String LOG_TAG = Movie.class.getSimpleName();
    private List<Movie> results = new ArrayList<>();
    @SerializedName("total_pages")
    private Integer totalPages;
    @SerializedName("total_results")
    private Integer totalResults;

    private DiscoverMovieResponse(Parcel in) {
        totalPages = AndroidUtil.readIntegerFromParcel(in);
        totalResults = AndroidUtil.readIntegerFromParcel(in);
        boolean hasValue = (in.readByte() != 0);

        if (hasValue) {
            int N = in.readInt();
            results = new ArrayList<>(N);
            for (int i = 0; i < N; i++) {
                results.add((Movie)
                        in.readParcelable(Movie.class.getClassLoader()));
            }
        }
    }

    public List<Movie> getResults() {
        return results;
    }

    public Integer getTotalPages() {
        return totalPages;
    }

    public Integer getTotalResults() {
        return totalResults;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        AndroidUtil.writeToParcel(totalPages, dest);
        AndroidUtil.writeToParcel(totalResults, dest);
        AndroidUtil.writeNullFlag(results, dest);

        if (results != null) {
            dest.writeInt(results.size());
            for (Movie result : results) {
                dest.writeParcelable(result, 0);
            }
        }
    }
}
