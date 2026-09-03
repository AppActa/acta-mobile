package br.com.acta.Api;

import br.com.acta.Model.Me;
import retrofit2.Call;
import retrofit2.http.GET;

public interface MeApi {
    @GET("")
    Call<Me> getMe();

}
