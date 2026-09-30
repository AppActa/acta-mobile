package br.com.acta.Api;

import br.com.acta.Model.Ciclo;
import br.com.acta.Model.Colaborador;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface CicloApi {
    @GET("ciclo/{id}")
    Call<Ciclo> getCiclo(@Path("id") Long id);
}
