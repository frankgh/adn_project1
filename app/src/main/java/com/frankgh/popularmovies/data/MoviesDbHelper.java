package com.frankgh.popularmovies.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * @author Francisco Guerrero <email>me@frankgh.com</email> on 12/21/15.
 */
public class MoviesDbHelper extends SQLiteOpenHelper {

    static final String DATABASE_NAME = "frankgh-popmovies.db";
    private static final int DATABASE_VERSION = 1;

    public MoviesDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create a table to hold movies
        final String SQL_CREATE_MOVIE_TABLE =
                "CREATE TABLE " + MoviesContract.MovieEntry.TABLE_NAME + " (" +
                        MoviesContract.MovieEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                        MoviesContract.MovieEntry.COLUMN_BACKDROP_PATH + " TEXT, " +
                        MoviesContract.MovieEntry.COLUMN_ADULT + " INTEGER," +
                        MoviesContract.MovieEntry.COLUMN_GENRE_IDS + " TEXT, " +
                        MoviesContract.MovieEntry.COLUMN_MOVIE_ID + " INTEGER NOT NULL UNIQUE," +
                        MoviesContract.MovieEntry.COLUMN_ORIGINAL_LANGUAGE + " TEXT,  " +
                        MoviesContract.MovieEntry.COLUMN_ORIGINAL_TITLE + " TEXT,  " +
                        MoviesContract.MovieEntry.COLUMN_OVERVIEW + " TEXT,  " +
                        MoviesContract.MovieEntry.COLUMN_RELEASE_DATE + " TEXT,  " +
                        MoviesContract.MovieEntry.COLUMN_POSTER_PATH + " TEXT,  " +
                        MoviesContract.MovieEntry.COLUMN_POPULARITY + " DECIMAL (15, 3), " +
                        MoviesContract.MovieEntry.COLUMN_TITLE + " TEXT NOT NULL, " +
                        MoviesContract.MovieEntry.COLUMN_VIDEO + " INTEGER," +
                        MoviesContract.MovieEntry.COLUMN_VOTE_AVERAGE + " REAL, " +
                        MoviesContract.MovieEntry.COLUMN_VOTE_COUNT + " INTEGER" +
                        " );";

        final String SQL_CREATE_MOVIE_EXTRA_TABLE =
                "CREATE TABLE " + MoviesContract.MovieExtraEntry.TABLE_NAME + " (" +
                        MoviesContract.MovieExtraEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                        // the ID of the movie entry associated with this movie data
                        MoviesContract.MovieExtraEntry.COLUMN_MOVIE_KEY + " INTEGER NOT NULL, " +
                        MoviesContract.MovieExtraEntry.COLUMN_EXTRA_NAME + " TEXT NOT NULL, " +
                        MoviesContract.MovieExtraEntry.COLUMN_EXTRA_VALUE + " TEXT NOT NULL, " +
                        MoviesContract.MovieExtraEntry.COLUMN_ADDED_DATE + " TEXT NOT NULL, " +

                        // Set up the movie column as a foreign key to account table.
                        " FOREIGN KEY (" + MoviesContract.MovieExtraEntry.COLUMN_MOVIE_KEY + ") REFERENCES " +
                        MoviesContract.MovieEntry.TABLE_NAME + " (" + MoviesContract.MovieEntry.COLUMN_MOVIE_ID + ")" +
                        " );";

        // Create a table to hold the saved movies
        final String SQL_CREATE_SAVED_MOVIE_TABLE =
                "CREATE TABLE " + MoviesContract.SavedMovieEntry.TABLE_NAME + " (" +
                        MoviesContract.SavedMovieEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                        // the ID of the movie entry associated with this movie data
                        MoviesContract.SavedMovieEntry.COLUMN_MOVIE_KEY + " INTEGER NOT NULL UNIQUE," +
                        MoviesContract.SavedMovieEntry.COLUMN_IS_SAVED + " INTEGER NOT NULL," +
                        MoviesContract.SavedMovieEntry.COLUMN_DATE + " INTEGER NOT NULL, " +
                        MoviesContract.SavedMovieEntry.COLUMN_UPDATED_DATE + " INTEGER," +

                        // Set up the movie column as a foreign key to movie table.
                        " FOREIGN KEY (" + MoviesContract.SavedMovieEntry.COLUMN_MOVIE_KEY + ") REFERENCES " +
                        MoviesContract.MovieEntry.TABLE_NAME + " (" + MoviesContract.MovieEntry.COLUMN_MOVIE_ID + ")" +
                        " );";

        // Create a table to hold a list of movies displayed in Main Activity
        final String SQL_CREATE_DISPLAYED_MOVIE_TABLE =
                "CREATE TABLE " + MoviesContract.DisplayedMovieEntry.TABLE_NAME + " (" +
                        MoviesContract.DisplayedMovieEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                        // the ID of the movie entry associated with this movie data
                        MoviesContract.DisplayedMovieEntry.COLUMN_MOVIE_KEY + " INTEGER NOT NULL UNIQUE," +
                        MoviesContract.DisplayedMovieEntry.COLUMN_DATE + " INTEGER NOT NULL, " +

                        // Set up the movie column as a foreign key to movie table.
                        " FOREIGN KEY (" + MoviesContract.DisplayedMovieEntry.COLUMN_MOVIE_KEY + ") REFERENCES " +
                        MoviesContract.MovieEntry.TABLE_NAME + " (" + MoviesContract.MovieEntry.COLUMN_MOVIE_ID + ")" +
                        " );";

        db.execSQL(SQL_CREATE_MOVIE_TABLE);
        db.execSQL(SQL_CREATE_MOVIE_EXTRA_TABLE);
        db.execSQL(SQL_CREATE_SAVED_MOVIE_TABLE);
        db.execSQL(SQL_CREATE_DISPLAYED_MOVIE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
