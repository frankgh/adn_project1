package com.frankgh.popularmovies.app;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.v4.graphics.ColorUtils;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.graphics.Palette;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.frankgh.popularmovies.R;
import com.frankgh.popularmovies.util.Utility;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Movie Adapter that is based on CursorAdapter
 * <p/>
 * Created by Francisco on 12/27/2015.
 */
public class MovieAdapter extends CursorAdapter {

    private final String LOG_TAG = MovieAdapter.class.getSimpleName();

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
        viewHolder.movieTitleView.setText(movieTitle);

        // Read vote average from cursor
        final String voteAvgText = Utility.getFormattedVoteAverage(context, cursor.getDouble(MovieListFragment.COL_MOVIE_VOTE_AVERAGE));
        viewHolder.voteAvgView.setText(voteAvgText);

        // Read the poster absolute path from cursor
        final String posterAbsolutePath = Utility.getPosterAbsolutePath(cursor.getString(MovieListFragment.COL_MOVIE_POSTER_PATH));

        if (posterAbsolutePath != null) {
            Picasso.with(context)
                    .load(posterAbsolutePath)
                    .networkPolicy(NetworkPolicy.OFFLINE)
                    .into(viewHolder.posterImageView, new Callback() {
                        @Override
                        public void onSuccess() {
                            setPillPalette(viewHolder);
                        }

                        @Override
                        public void onError() {
                            //Try again online if cache failed
                            Picasso.with(context)
                                    .load(posterAbsolutePath)
                                    .error(R.drawable.ic_error_24dp)
                                    .into(viewHolder.posterImageView, new Callback() {
                                        @Override
                                        public void onSuccess() {
                                            setPillPalette(viewHolder);
                                        }

                                        @Override
                                        public void onError() {
                                            Log.v(LOG_TAG, "Could not fetch image from " + posterAbsolutePath);
                                            showErrorImage(viewHolder);
                                        }
                                    });
                        }
                    });
        } else {
            viewHolder.posterImageView.setImageResource(R.drawable.ic_movie_placeholder); // set default placeholder
            showErrorImage(viewHolder);
        }
    }

    private void showErrorImage(ViewHolder viewHolder) {
        viewHolder.posterImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        viewHolder.posterImageView.setPadding(100, 100, 100, 100); // add padding
    }

    private void setPillPalette(final ViewHolder holder) {
        holder.moviePillView.setVisibility(View.VISIBLE);
        holder.posterImageView.setScaleType(ImageView.ScaleType.FIT_XY);
        holder.posterImageView.setPadding(0, 0, 0, 0);

        Bitmap bitmap = ((BitmapDrawable) holder.posterImageView.getDrawable()).getBitmap();
        Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
            @Override
            public void onGenerated(Palette palette) {
                Palette.Swatch vibrant = palette.getVibrantSwatch();

                if (vibrant != null) {
                    holder.movieTitleView.setTextColor(vibrant.getTitleTextColor());
                    holder.voteAvgView.setTextColor(vibrant.getTitleTextColor());
                    holder.starImageView.setColorFilter(vibrant.getTitleTextColor());
                    holder.moviePillView.setBackgroundColor(ColorUtils
                            .setAlphaComponent(palette.getMutedColor(vibrant.getRgb()), 220));
                }
            }
        });
    }

    public int pxToDp(final Context context, int px) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int dp = Math.round(px / (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return dp;
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    /**
     * Cache of the children views for a movie list item.
     */
    public static class ViewHolder {
        @Bind(R.id.grid_item_movie_title)
        TextView movieTitleView;
        @Bind(R.id.grid_item_movie_vote_average)
        TextView voteAvgView;
        @Bind(R.id.posterImageView)
        ImageView posterImageView;
        @Bind(R.id.grid_item_movie_pill)
        View moviePillView;
        @Bind(R.id.starImageView)
        ImageView starImageView;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}