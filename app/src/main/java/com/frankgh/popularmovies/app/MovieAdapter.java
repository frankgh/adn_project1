package com.frankgh.popularmovies.app;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.frankgh.popularmovies.R;
import com.frankgh.popularmovies.themoviedb.model.DiscoverMovieResult;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.ButterKnife;

/**
 * Created by francisco on 11/25/15.
 */
public class MovieAdapter extends ArrayAdapter<DiscoverMovieResult> {

    private final String LOG_TAG = MovieAdapter.class.getSimpleName();

    public MovieAdapter(Context context, int resource, List<DiscoverMovieResult> objects) {
        super(context, resource, objects);
    }

    /**
     * Binds movie data to the view. For the image, we try to load from cache first. If that fails
     * we load from the network
     *
     * @param itemView  the view
     * @param movieData movie data
     */
    private void bindMovieDataToView(View itemView, final DiscoverMovieResult movieData) {
        final ImageView posterImageView = (ImageView) itemView.findViewById(R.id.posterImageView);
        posterImageView.setContentDescription(movieData.getTitle());

        if (movieData.getPosterAbsolutePath() != null) {
            Picasso.with(getContext())
                    .load(movieData.getPosterAbsolutePath())
                    .placeholder(R.drawable.ic_movie_icon)
                    .networkPolicy(NetworkPolicy.OFFLINE)
                    .into(posterImageView, new Callback() {
                        @Override
                        public void onSuccess() {
                            posterImageView.setScaleType(ImageView.ScaleType.FIT_XY);
                        }

                        @Override
                        public void onError() {
                            posterImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                            //Try again online if cache failed
                            Picasso.with(getContext())
                                    .load(movieData.getPosterAbsolutePath())
                                    .error(R.drawable.ic_error_24dp)
                                    .into(posterImageView, new Callback() {
                                        @Override
                                        public void onSuccess() {
                                            posterImageView.setScaleType(ImageView.ScaleType.FIT_XY);
                                        }

                                        @Override
                                        public void onError() {
                                            Log.v(LOG_TAG, "Could not fetch image from " + movieData.getPosterAbsolutePath());
                                            posterImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                                        }
                                    });
                        }
                    });
        } else {
            posterImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            posterImageView.setImageResource(R.drawable.ic_movie_icon);
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.grid_item_movie, parent, false);
        }

        bindMovieDataToView(convertView, getItem(position));

        return convertView;
    }

    public void swapData(List<DiscoverMovieResult> data) {
        clear();
        if (data != null) {
            addAll(data);
        }
    }

    public class ViewHolder {

        public ViewHolder(View itemView) {
            ButterKnife.bind(this, itemView);
        }
    }
}
