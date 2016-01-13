package com.frankgh.popularmovies.data;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.frankgh.popularmovies.themoviedb.model.Movie;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Francisco Guerrero <email>me@frankgh.com</email> on 12/21/15.
 */
public class MoviesProvider extends ContentProvider {

    static final int MOVIE = 100;
    static final int MOVIE_WITH_ID = 105;
    static final int MOVIE_EXTRA = 200;
    static final int MOVIE_EXTRA_WITH_MOVIE_ID = 205;
    static final int DISPLAYED_MOVIE = 300;
    static final int SAVED_MOVIE = 400;

    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private static final SQLiteQueryBuilder sDisplayedMoviesQueryBuilder;
    private static final SQLiteQueryBuilder sSavedMoviesQueryBuilder;
    private static final SQLiteQueryBuilder sMovieDetailQueryBuilder;

    static {
        sDisplayedMoviesQueryBuilder = new SQLiteQueryBuilder();
        sSavedMoviesQueryBuilder = new SQLiteQueryBuilder();
        sMovieDetailQueryBuilder = new SQLiteQueryBuilder();

        String joinString = "%s INNER JOIN %s ON %s.%s = %s.%s";
        String leftOuterJoinString = "%s LEFT OUTER JOIN %s ON %s.%s = %s.%s";

        //This is an inner join which looks like
        //displayed_movie INNER JOIN movie ON displayed_movie.movie_id = movie._id
        String displayedMoviesJoinString = String.format(joinString,
                MoviesContract.DisplayedMovieEntry.TABLE_NAME,
                MoviesContract.MovieEntry.TABLE_NAME,
                MoviesContract.DisplayedMovieEntry.TABLE_NAME,
                MoviesContract.DisplayedMovieEntry.COLUMN_MOVIE_KEY,
                MoviesContract.MovieEntry.TABLE_NAME,
                MoviesContract.MovieEntry.COLUMN_MOVIE_ID);

        //This is an inner join which looks like
        //saved_movie INNER JOIN movie ON saved_movie.movie_id = movie._id
        String savedMoviesJoinString = String.format(joinString,
                MoviesContract.SavedMovieEntry.TABLE_NAME,
                MoviesContract.MovieEntry.TABLE_NAME,
                MoviesContract.SavedMovieEntry.TABLE_NAME,
                MoviesContract.SavedMovieEntry.COLUMN_MOVIE_KEY,
                MoviesContract.MovieEntry.TABLE_NAME,
                MoviesContract.MovieEntry.COLUMN_MOVIE_ID);

        String movieDetailJoinString = String.format(leftOuterJoinString,
                MoviesContract.MovieEntry.TABLE_NAME,
                MoviesContract.SavedMovieEntry.TABLE_NAME,
                MoviesContract.MovieEntry.TABLE_NAME,
                MoviesContract.MovieEntry.COLUMN_MOVIE_ID,
                MoviesContract.SavedMovieEntry.TABLE_NAME,
                MoviesContract.SavedMovieEntry.COLUMN_MOVIE_KEY);

        sDisplayedMoviesQueryBuilder.setTables(displayedMoviesJoinString);
        sSavedMoviesQueryBuilder.setTables(savedMoviesJoinString);
        sMovieDetailQueryBuilder.setTables(movieDetailJoinString);
    }

    public final String LOG_TAG = MoviesProvider.class.getSimpleName();
    private MoviesDbHelper mOpenHelper;

    static UriMatcher buildUriMatcher() {
        // I know what you're thinking.  Why create a UriMatcher when you can use regular
        // expressions instead?  Because you're not crazy, that's why.

        // All paths added to the UriMatcher have a corresponding code to return when a match is
        // found.  The code passed into the constructor represents the code to return for the root
        // URI.  It's common to use NO_MATCH as the code for this case.
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = MoviesContract.CONTENT_AUTHORITY;

        // For each type of URI you want to add, create a corresponding code.
        matcher.addURI(authority, MoviesContract.PATH_MOVIE, MOVIE);
        matcher.addURI(authority, MoviesContract.PATH_MOVIE + "/#", MOVIE_WITH_ID);

        matcher.addURI(authority, MoviesContract.PATH_MOVIE_EXTRA, MOVIE_EXTRA);
        matcher.addURI(authority, MoviesContract.PATH_MOVIE_EXTRA + "/#", MOVIE_EXTRA_WITH_MOVIE_ID);

        matcher.addURI(authority, MoviesContract.PATH_SAVED_MOVIE, SAVED_MOVIE);
        matcher.addURI(authority, MoviesContract.PATH_DISPLAYED_MOVIE, DISPLAYED_MOVIE);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new MoviesDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        // Here's the switch statement that, given a URI, will determine what kind of request it is,
        // and query the database accordingly.
        Cursor retCursor = null;

        final int match = sUriMatcher.match(uri);

        if (match == DISPLAYED_MOVIE) {
            retCursor = sDisplayedMoviesQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                    projection,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    sortOrder
            );
        } else if (match == MOVIE_EXTRA_WITH_MOVIE_ID) {
            retCursor = getMovieExtraById(uri, projection, sortOrder);
        } else if (match == MOVIE_WITH_ID) {
            retCursor = getMovieDetailById(uri, projection, sortOrder);
        } else {
            retCursor = mOpenHelper.getReadableDatabase().query(
                    getTableName(uri),
                    projection,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    sortOrder
            );
        }

        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case MOVIE:
                return MoviesContract.MovieEntry.CONTENT_TYPE;
            case MOVIE_EXTRA_WITH_MOVIE_ID:
                return MoviesContract.MovieExtraEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        long _id = db.insert(getTableName(uri), null, values);
        db.close();
        getContext().getContentResolver().notifyChange(uri, null);

        if (_id > 0) {
            switch (sUriMatcher.match(uri)) {
                case MOVIE:
                    return MoviesContract.MovieEntry.buildMovieUri(_id);
                case MOVIE_EXTRA:
                    return MoviesContract.MovieExtraEntry.buildMovieExtraUri(_id);
            }
        }

        throw new android.database.SQLException("Failed to insert row into " + uri);
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        // this makes delete all rows return the number of rows deleted
        if (null == selection) selection = "1";

        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int rowsDeleted = db.delete(getTableName(uri), selection, selectionArgs);
        db.close(); // close db

        // Because a null deletes all rows
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int rowsUpdated = db.update(getTableName(uri), values, selection, selectionArgs);
        db.close(); // close db

        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
        final int match = sUriMatcher.match(uri);
        switch (match) {

            case MOVIE: {
                final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
                db.beginTransaction();
                int returnCount = 0;
                List<Integer> ids = getListOfMovieIds(values);
                List<Movie> existingMovieList = getExistingMoviesByIds(db, ids);
                try {
                    for (ContentValues value : values) {
                        int movieId = value.getAsInteger(MoviesContract.MovieEntry.COLUMN_MOVIE_ID);
                        Movie existingMovie = getMovieById(existingMovieList, movieId);

                        if (existingMovie == null) {
                            long _id = db.insert(MoviesContract.MovieEntry.TABLE_NAME, null, value);
                            if (_id != -1) {
                                returnCount++;
                            }
                        } else if (existingMovie.hasUpdates(value)) {
                            value.put(MoviesContract.MovieEntry._ID, existingMovie.getInternalId());
                            value.put(MoviesContract.MovieEntry.COLUMN_MOVIE_ID, movieId);

                            int rowsUpdated = db.update(
                                    MoviesContract.MovieEntry.TABLE_NAME,
                                    value,
                                    MoviesContract.MovieEntry.COLUMN_MOVIE_ID + " = ?",
                                    new String[]{Integer.toString(movieId)});

                            Log.d(LOG_TAG, "Movie with id " + movieId + " was updated");

                            if (rowsUpdated > 0) {
                                returnCount++;
                            }
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                if (returnCount > 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                return returnCount;
            }

            case DISPLAYED_MOVIE: {
                final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(MoviesContract.DisplayedMovieEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            }

            case MOVIE_EXTRA: {
                final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(MoviesContract.MovieExtraEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            }

            default:
                return super.bulkInsert(uri, values);
        }
    }

    private Cursor getMovieDetailById(Uri uri, String[] projection, String sortOrder) {
        long _id = MoviesContract.MovieEntry.getIdFromUri(uri);
        return sMovieDetailQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                MoviesContract.MovieEntry.TABLE_NAME + "." + MoviesContract.MovieEntry._ID + " = ?",
                new String[]{Long.toString(_id)},
                null,
                null,
                sortOrder
        );
    }

    private Cursor getMovieExtraById(Uri uri, String[] projection, String sortOrder) {
        long movieId = MoviesContract.MovieExtraEntry.getMovieIdFromUri(uri);
        return mOpenHelper.getReadableDatabase().query(
                getTableName(uri),
                projection,
                MoviesContract.MovieExtraEntry.COLUMN_MOVIE_KEY + " = ?",
                new String[]{Long.toString(movieId)},
                null,
                null,
                sortOrder
        );
    }

    private Movie getMovieById(List<Movie> movieList, int movieId) {
        if (movieList != null && !movieList.isEmpty()) {
            for (Movie movie : movieList) {
                if (movie.getMovieId() == movieId) {
                    return movie;
                }
            }
        }
        return null;
    }

    private List<Movie> getExistingMoviesByIds(final SQLiteDatabase db, List<Integer> ids) {
        String selection = MoviesContract.MovieEntry.COLUMN_MOVIE_ID + " IN (" +
                TextUtils.join(",", Collections.nCopies(ids.size(), "?")) + ")";
        String[] selectionArgs = TextUtils.join(",", ids).split(",");
        String[] columns = {
                MoviesContract.MovieEntry.COLUMN_BACKDROP_PATH,
                MoviesContract.MovieEntry.COLUMN_ADULT,
                MoviesContract.MovieEntry.COLUMN_GENRE_IDS,
                MoviesContract.MovieEntry.COLUMN_MOVIE_ID,
                MoviesContract.MovieEntry.COLUMN_ORIGINAL_LANGUAGE,
                MoviesContract.MovieEntry.COLUMN_ORIGINAL_TITLE,
                MoviesContract.MovieEntry.COLUMN_OVERVIEW,
                MoviesContract.MovieEntry.COLUMN_RELEASE_DATE,
                MoviesContract.MovieEntry.COLUMN_POSTER_PATH,
                MoviesContract.MovieEntry.COLUMN_POPULARITY,
                MoviesContract.MovieEntry.COLUMN_TITLE,
                MoviesContract.MovieEntry.COLUMN_VIDEO,
                MoviesContract.MovieEntry.COLUMN_VOTE_AVERAGE,
                MoviesContract.MovieEntry.COLUMN_VOTE_COUNT,
                MoviesContract.MovieEntry._ID
        };

        // Get existing movies by id
        Cursor cursor = db.query(MoviesContract.MovieEntry.TABLE_NAME, columns,
                //        null, null, null, null, null);
                selection, selectionArgs, null, null, null);
        List<Movie> movieList = new ArrayList<>();
        while (cursor.moveToNext()) {
            movieList.add(new Movie(
                    cursor.getString(0),
                    cursor.getInt(1) != 0,
                    cursor.getString(2),
                    cursor.getInt(3),
                    cursor.getString(4),
                    cursor.getString(5),
                    cursor.getString(6),
                    cursor.getString(7),
                    cursor.getString(8),
                    cursor.getDouble(9),
                    cursor.getString(10),
                    cursor.getInt(11) != 0,
                    cursor.getDouble(12),
                    cursor.getInt(13),
                    cursor.getInt(14)
            ));
        }
        cursor.close();
        return movieList;
    }

    @Override
    @TargetApi(11)
    public void shutdown() {
        mOpenHelper.close();
        super.shutdown();
    }

    private String getTableName(Uri uri) {
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case MOVIE:
                return MoviesContract.MovieEntry.TABLE_NAME;
            case MOVIE_WITH_ID:
                return MoviesContract.MovieEntry.TABLE_NAME;
            case MOVIE_EXTRA:
                return MoviesContract.MovieExtraEntry.TABLE_NAME;
            case MOVIE_EXTRA_WITH_MOVIE_ID:
                return MoviesContract.MovieExtraEntry.TABLE_NAME;
            case SAVED_MOVIE:
                return MoviesContract.SavedMovieEntry.TABLE_NAME;
            case DISPLAYED_MOVIE:
                return MoviesContract.DisplayedMovieEntry.TABLE_NAME;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    private List<Integer> getListOfMovieIds(ContentValues[] values) {
        if (values == null || values.length == 0) {
            return null;
        }

        List<Integer> ids = new ArrayList<>(values.length);
        for (ContentValues value : values) {
            ids.add(value.getAsInteger(MoviesContract.MovieEntry.COLUMN_MOVIE_ID));
        }
        return ids;
    }
}
