package br.com.acta.Client;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class RetrofitClientGeocoding {
    private static final String BASE_URL = "https://maps.googleapis.com/maps/api/geocode/";
    private static Retrofit retrofit;

    private RetrofitClientGeocoding() {
    }

    public static Retrofit getInstance() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
