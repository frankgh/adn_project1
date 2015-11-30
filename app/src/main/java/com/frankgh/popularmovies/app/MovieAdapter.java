package com.frankgh.popularmovies.app;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.frankgh.popularmovies.R;
import com.frankgh.popularmovies.themoviedb.model.DiscoverMovieResult;
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

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        DiscoverMovieResult movie = getItem(position);

        Log.d(LOG_TAG, "inflating " + movie.getTitle());

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.grid_item_movie, parent, false);
        }


        ImageView posterImageView = (ImageView) convertView.findViewById(R.id.posterImageView);
        TextView textView = (TextView) convertView.findViewById(R.id.movieTitle);

        textView.setText(movie.getTitle());
        Picasso.with(getContext())
                .load(movie.getPosterAbsolutePath())
                .placeholder(android.R.drawable.ic_media_play)
                .into(posterImageView);

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
