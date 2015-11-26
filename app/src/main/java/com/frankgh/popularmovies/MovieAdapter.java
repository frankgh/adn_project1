package com.frankgh.popularmovies;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.frankgh.popularmovies.themoviedb.model.DiscoverMovieResult;

/**
 * Created by francisco on 11/25/15.
 */
public class MovieAdapter extends ArrayAdapter<DiscoverMovieResult> {

    public MovieAdapter(Context context, int resource) {
        super(context, resource);
    }

    public MovieAdapter(Context context, int resource, DiscoverMovieResult[] objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return super.getView(position, convertView, parent);
    }
}
