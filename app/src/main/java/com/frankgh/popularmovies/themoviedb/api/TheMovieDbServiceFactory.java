package com.frankgh.popularmovies.themoviedb.api;

import com.squareup.okhttp.OkHttpClient;

import retrofit.GsonConverterFactory;
import retrofit.Retrofit;

/**
 * Created by Francisco on 12/13/2015.
 */
public class TheMovieDbServiceFactory {
    private static TheMovieDbService service;

    static {
        OkHttpClient client = new OkHttpClient();
        client.networkInterceptors().add(new StethoInterceptor());
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(PciConstants.PCI_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        service = retrofit.create(TheMovieDbService.class);
    }

    public static TheMovieDbService getService() {
        return service;
    }
}
