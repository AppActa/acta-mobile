package br.com.acta.Api;

import br.com.acta.Model.Colaborador;
import br.com.acta.Model.Me;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface ColaboradorApi {
    @GET("colaborador/{id}")
    Call<Colaborador> getColaborador(@Path("id") Long id);
}
