package br.com.acta.Api;

import br.com.acta.Model.Me;
import br.com.acta.Model.AtivarRequest;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface MeApi {
    @GET("me")
    Call<Me> getMe();

    @POST("auth/ativar")
    Call<Me> ativar(@Body AtivarRequest request);
}