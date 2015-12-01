package com.frankgh.popularmovies.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.frankgh.popularmovies.R;
import com.frankgh.popularmovies.themoviedb.model.DiscoverMovieResult;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * A placeholder fragment containing a simple view.
 */
public class MovieDetailActivityFragment extends Fragment {

    @Bind(R.id.posterImageView)
    ImageView mPosterImageView;

    @Bind(R.id.backdrop_image_view)
    ImageView mBackdropImageView;

    @Bind(R.id.movieTitleTextView)
    TextView mMovieTitleTextView;

    @Bind(R.id.movieReleaseDateTextView)
    TextView mMovieReleaseDateTextView;

    @Bind(R.id.grid_item_movie_vote_average)
    TextView mMovieVoteAverageTextView;

    @Bind(R.id.movieOverviewTextView)
    TextView mMovieOverviewTextView;

    private DiscoverMovieResult mMovieData;

    private final String LOG_TAG = MovieDetailActivityFragment.class.getSimpleName();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_movie_detail, container, false);
        Intent intent = getActivity().getIntent();

        if (intent != null && intent.hasExtra(MovieDetailActivity.MOVIE_DETAIL_KEY)) {
            mMovieData = intent.getParcelableExtra(MovieDetailActivity.MOVIE_DETAIL_KEY);
        }

        if (mMovieData == null) {
            getActivity().onBackPressed(); // Invalid movie
            return rootView;
        }

        ButterKnife.bind(this, rootView);
        bindMovieDataToView(rootView);
        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    private void bindMovieDataToView(View view) {
        getActivity().setTitle(mMovieData.getTitle());
        mMovieTitleTextView.setText(mMovieData.getTitle());
        mMovieReleaseDateTextView.setText(mMovieData.getFormattedReleaseDate());
        mMovieVoteAverageTextView.setText(mMovieData.getFormattedVoteAverage());
        mMovieOverviewTextView.setText(mMovieData.getOverview());

        if (mMovieData.getPosterAbsolutePath() != null) {
            Picasso.with(getContext())
                    .load(mMovieData.getPosterAbsolutePath())
                    .networkPolicy(NetworkPolicy.OFFLINE)
                    .into(mPosterImageView, new Callback() {
                        @Override
                        public void onSuccess() {
                        }

                        @Override
                        public void onError() {
                            //Try again online if cache failed
                            Picasso.with(getContext())
                                    .load(mMovieData.getPosterAbsolutePath())
                                    .error(R.drawable.ic_error_24dp)
                                    .into(mPosterImageView, new Callback() {
                                        @Override
                                        public void onSuccess() {
                                        }

                                        @Override
                                        public void onError() {
                                            Log.v(LOG_TAG, "Could not fetch image from " + mMovieData.getPosterAbsolutePath());
                                        }
                                    });
                        }
                    });
        }

        if (mMovieData.getBackDropAbsolutePath() != null) {
            Picasso.with(getContext())
                    .load(mMovieData.getBackDropAbsolutePath())
                    .networkPolicy(NetworkPolicy.OFFLINE)
                    .into(mBackdropImageView, new Callback() {
                        @Override
                        public void onSuccess() {
                        }

                        @Override
                        public void onError() {
                            //Try again online if cache failed
                            Picasso.with(getContext())
                                    .load(mMovieData.getBackDropAbsolutePath())
                                    .error(R.drawable.ic_error_24dp)
                                    .into(mBackdropImageView, new Callback() {
                                        @Override
                                        public void onSuccess() {
                                        }

                                        @Override
                                        public void onError() {
                                            Log.v(LOG_TAG, "Could not fetch image from " +
                                                    mMovieData.getBackDropAbsolutePath());
                                        }
                                    });
                        }
                    });
        }
    }
}
