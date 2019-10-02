package com.zacneubert.echo.api.itunes;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * Created by zac on 2/1/18.
 */

public class ItunesApiBuilder {
    private static ItunesApi itunesApi;

    public static ItunesApi getItunesApi() {
        return getItunesApi(false);
    }

    public static ItunesApi getItunesApi(boolean rebuild) {
        if(itunesApi == null || rebuild) {
            itunesApi = buildItunesApi();
        }
        return itunesApi;
    }

    private static ItunesApi buildItunesApi() {
        Gson gson = new GsonBuilder().create();

        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(okHttpClient)
                .baseUrl("https://itunes.apple.com/")
                .build();
        return retrofit.create(ItunesApi.class);
    }
}
