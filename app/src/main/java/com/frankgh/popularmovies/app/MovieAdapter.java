package com.frankgh.popularmovies.app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.frankgh.popularmovies.R;
import com.frankgh.popularmovies.themoviedb.model.DiscoverMovieResult;

import java.util.List;

import butterknife.ButterKnife;

/**
 * Created by francisco on 11/25/15.
 */
public class MovieAdapter extends ArrayAdapter<DiscoverMovieResult> {

    public MovieAdapter(Context context, int resource, List<DiscoverMovieResult> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        DiscoverMovieResult movie = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.grid_item_movie, parent, false);
        }

        return convertView;
    }

    public void swapData(List<DiscoverMovieResult> data) {
        this.clear();
        if (data != null) {
            this.addAll(data);
        }
    }

    public class ViewHolder {

        public ViewHolder(View itemView) {
            ButterKnife.bind(this, itemView);
        }
    }
}
