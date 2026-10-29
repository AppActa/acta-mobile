package br.com.acta.Api;

import java.util.List;
import java.util.Map;

import br.com.acta.Model.Treinamento;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.Path;

public interface TreinamentoApi {
    @PATCH("treinamento/{id}")
    Call<Treinamento> patch(@Path("id") Long id, @Body Map<String, Object> campos);

    @GET("ciclo/{idCiclo}/treinamento")
    Call<List<Treinamento>> buscarTreinamentos(@Path("idCiclo") Long idCiclo);

    @GET("treinamento/{id}")
    Call<Treinamento> buscar(@Path("id") Long id);
}
