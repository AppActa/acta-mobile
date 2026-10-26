package br.com.acta.Api;

import java.util.List;

import br.com.acta.Model.Treinamento;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface TreinamentoApi {
    @GET("ciclo/{idCiclo}/treinamento")
    Call<List<Treinamento>> buscarTreinamentos(@Path("idCiclo") Long idCiclo);

    @GET("treinamento/{id}")
    Call<Treinamento> buscar(@Path("id") Long id);
}
