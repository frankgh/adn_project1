package com.frankgh.popularmovies.themoviedb.api;

import com.facebook.stetho.okhttp.StethoInterceptor;
import com.frankgh.popularmovies.BuildConfig;
import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;

import retrofit.GsonConverterFactory;
import retrofit.Retrofit;

/**
 * Created by Francisco on 12/13/2015.
 */
public class TheMovieDbServiceFactory {
    private static TheMovieDbService service;

    static {

        Interceptor interceptor = new Interceptor() {
            final String API_KEY_PARAM = "api_key";

            @Override
            public Response intercept(Chain chain) throws IOException {
                HttpUrl url = chain.request().httpUrl()
                        .newBuilder()
                        .addQueryParameter(API_KEY_PARAM, BuildConfig.THE_MOVIE_DB_API_KEY)
                        .build();
                Request request = chain.request().newBuilder().url(url).build();
                return chain.proceed(request);
            }
        };

        // Add the interceptor to OkHttpClient
        OkHttpClient okHttpClient = new OkHttpClient();
        okHttpClient.interceptors().add(new StethoInterceptor());
        okHttpClient.interceptors().add(interceptor);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BuildConfig.THE_MOVIE_DB_URL_API_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        service = retrofit.create(TheMovieDbService.class);
    }

    public static TheMovieDbService getService() {
        return service;
    }
}
