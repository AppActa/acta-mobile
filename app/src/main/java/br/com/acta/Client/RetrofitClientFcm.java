package br.com.acta.Client;

import java.util.concurrent.TimeUnit;

import br.com.acta.Api.NotificacaoApi;
import br.com.acta.Auth.AuthInterceptor;
import br.com.acta.Auth.FirebaseTokenProvider;
import br.com.acta.BuildConfig;

import com.google.firebase.auth.FirebaseAuth;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class RetrofitClientFcm {
    private static final NotificacaoApi API = criarApi();

    private RetrofitClientFcm() {}

    public static NotificacaoApi api() { return API; }

    private static NotificacaoApi criarApi() {
        OkHttpClient client = new OkHttpClient.Builder()
            .addInterceptor(new AuthInterceptor(new FirebaseTokenProvider(FirebaseAuth.getInstance())))
            .callTimeout(12, TimeUnit.SECONDS)
            .build();

        return new Retrofit.Builder()
            .baseUrl(BuildConfig.FCM_API_BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NotificacaoApi.class);
    }
}