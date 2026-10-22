package br.com.acta.Api;

import br.com.acta.Model.GeocodingResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface GeocodingApi {
    @GET("json")
    Call<GeocodingResponse> buscarPorCoordenadas(@Query("latlng") String coordenadas, @Query("key") String chave);
}
