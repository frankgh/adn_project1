package com.frankgh.popularmovies.app;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.frankgh.popularmovies.R;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Francisco on 12/27/2015.
 */
public class MovieAdapter extends CursorAdapter {

    private final String LOG_TAG = MovieAdapter.class.getSimpleName();

    /**
     * Cache of the children views for a movie list item.
     */
    public static class ViewHolder {
        @Bind(R.id.grid_item_movie_title)
        TextView movieTitleText;
        @Bind(R.id.grid_item_movie_vote_average)
        TextView voteAvgText;
        @Bind(R.id.posterImageView)
        ImageView posterImageView;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

    public MovieAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.grid_item_movie, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        return view;
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {

        final ViewHolder viewHolder = (ViewHolder) view.getTag();

        // Read movie title from cursor
        final String movieTitle = cursor.getString(MovieListFragment.COL_MOVIE_TITLE);
        viewHolder.movieTitleText.setText(movieTitle);

        // Read vote average from cursor
        final String voteAvgText = cursor.getString(MovieListFragment.COL_MOVIE_VOTE_AVERAGE);
        viewHolder.voteAvgText.setText(voteAvgText);

        // Read the poster absolute path from cursor
        final String posterAbsolutePath = cursor.getString(MovieListFragment.COL_MOVIE_POSTER_PATH);

        if (posterAbsolutePath != null) {
            Picasso.with(context)
                    .load(posterAbsolutePath)
                    .networkPolicy(NetworkPolicy.OFFLINE)
                    .into(viewHolder.posterImageView, new Callback() {
                        @Override
                        public void onSuccess() {
                            viewHolder.posterImageView.setScaleType(ImageView.ScaleType.FIT_XY);
                        }

                        @Override
                        public void onError() {
                            viewHolder.posterImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                            //Try again online if cache failed
                            Picasso.with(context)
                                    .load(posterAbsolutePath)
                                    .error(R.drawable.ic_error_24dp)
                                    .into(viewHolder.posterImageView, new Callback() {
                                        @Override
                                        public void onSuccess() {
                                            viewHolder.posterImageView.setScaleType(ImageView.ScaleType.FIT_XY);
                                        }

                                        @Override
                                        public void onError() {
                                            Log.v(LOG_TAG, "Could not fetch image from " + posterAbsolutePath);
                                            viewHolder.posterImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                                        }
                                    });
                        }
                    });
        } else {
            viewHolder.posterImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            viewHolder.posterImageView.setImageResource(R.drawable.ic_movie_placeholder); // set default placeholder
            viewHolder.posterImageView.setPadding(100, 100, 100, 100); // add padding
        }
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }
}