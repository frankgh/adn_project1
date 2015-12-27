package com.frankgh.popularmovies.data;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * @author Francisco Guerrero <email>me@frankgh.com</email> on 12/21/15.
 */
public class MoviesProvider extends ContentProvider {

    static final int MOVIE = 100;
    static final int MOVIE_EXTRA = 200;
    static final int MOVIE_EXTRA_WITH_MOVIE_ID = 205;
    private static final UriMatcher sUriMatcher = buildUriMatcher();
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
        matcher.addURI(authority, MoviesContract.PATH_MOVIE_EXTRA, MOVIE_EXTRA);
        matcher.addURI(authority, MoviesContract.PATH_MOVIE_EXTRA + "/*", MOVIE_EXTRA_WITH_MOVIE_ID);

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
        Cursor retCursor = mOpenHelper.getReadableDatabase().query(
                getTableName(uri),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );

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
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case MOVIE: {
                final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(MoviesContract.MovieEntry.TABLE_NAME, null, value);
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
            case MOVIE_EXTRA:
                return MoviesContract.MovieExtraEntry.TABLE_NAME;
            case MOVIE_EXTRA_WITH_MOVIE_ID:
                return MoviesContract.MovieExtraEntry.TABLE_NAME;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }
}
