package com.frankgh.popularmovies.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;

/**
 * @author Francisco Guerrero <email>me@frankgh.com</email> on 12/21/15.
 */
public class MoviesContract {

    // The "Content authority" is a name for the entire content provider, similar to the
    // relationship between a domain name and its website.  A convenient string to use for the
    // content authority is the package name for the app, which is guaranteed to be unique on the
    // device.
    public static final String CONTENT_AUTHORITY = "com.frankgh.popularmovies.app";

    // Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
    // the content provider.
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    // Possible paths (appended to base content URI for possible URI's)
    public static final String PATH_MOVIE = "movie";
    public static final String PATH_MOVIE_EXTRA = "movie_extra";
    public static final String PATH_SAVED_MOVIE = "saved_movie";
    public static final String PATH_DISPLAYED_MOVIE = "displayed_movie";

    public static final class DisplayedMovieEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_DISPLAYED_MOVIE).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_DISPLAYED_MOVIE;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_DISPLAYED_MOVIE;

        // Table name
        public static final String TABLE_NAME = "displayed_movie";

        // Columns

        // Movie ID as returned by API
        public static final String COLUMN_MOVIE_KEY = "movie_id";
        // Added date, stored as long in milliseconds since the epoch
        public static final String COLUMN_DATE = "date";

        public static Uri buildMovieUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    public static final class SavedMovieEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_SAVED_MOVIE).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_SAVED_MOVIE;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_SAVED_MOVIE;

        // Table name
        public static final String TABLE_NAME = "saved_movie";

        // Columns

        // Movie ID as returned by API
        public static final String COLUMN_MOVIE_KEY = "movie_id";
        // Is Saved set to true or false
        public static final String COLUMN_IS_SAVED = "is_saved";
        // Added date, stored as long in milliseconds since the epoch
        public static final String COLUMN_DATE = "date";
        // Updated date, stored as long in milliseconds since the epoch
        public static final String COLUMN_UPDATED_DATE = "updated_date";

        public static Uri buildMovieUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static long getIdFromUri(@NonNull Uri uri) {
            return Long.parseLong(uri.getPathSegments().get(1));
        }
    }

    public static final class MovieEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIE).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIE;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIE;

        // Table name
        public static final String TABLE_NAME = "movie";

        // Columns
        public static final String COLUMN_BACKDROP_PATH = "backdrop_path";
        public static final String COLUMN_ADULT = "adult";
        public static final String COLUMN_GENRE_IDS = "genre_ids";
        public static final String COLUMN_MOVIE_ID = "movie_id";
        public static final String COLUMN_ORIGINAL_LANGUAGE = "original_language";
        public static final String COLUMN_ORIGINAL_TITLE = "original_title";
        public static final String COLUMN_OVERVIEW = "overview";
        public static final String COLUMN_RELEASE_DATE = "release_date";
        public static final String COLUMN_POSTER_PATH = "poster_path";
        public static final String COLUMN_POPULARITY = "popularity";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_VIDEO = "video";
        public static final String COLUMN_VOTE_AVERAGE = "vote_average";
        public static final String COLUMN_VOTE_COUNT = "vote_count";

        public static Uri buildMovieUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static long getIdFromUri(@NonNull Uri uri) {
            return Long.parseLong(uri.getPathSegments().get(1));
        }
    }

    public static final class MovieExtraEntry implements BaseColumns {

        public static final String NAME_VIDEOS = "VIDEOS";
        public static final String NAME_REVIEWS = "REVIEWS";

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIE_EXTRA).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIE_EXTRA;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIE_EXTRA;

        // Table name
        public static final String TABLE_NAME = "movie_extra";

        // Columns
        public static final String COLUMN_MOVIE_KEY = "movie_id";
        public static final String COLUMN_EXTRA_NAME = "extra_name";
        public static final String COLUMN_EXTRA_VALUE = "extra_value";
        public static final String COLUMN_ADDED_DATE = "added_date";

        public static Uri buildMovieExtraUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static long getMovieIdFromUri(@NonNull Uri uri) {
            return Long.parseLong(uri.getPathSegments().get(1));
        }
    }
}
