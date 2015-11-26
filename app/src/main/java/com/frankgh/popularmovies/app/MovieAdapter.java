package com.frankgh.popularmovies.app;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

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
        return super.getView(position, convertView, parent);
    }

    public void swapData(List<DiscoverMovieResult> data) {
        this.clear();

        if (data != null) {
            for (DiscoverMovieResult movie : data) {
                this.add(movie);
            }
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
