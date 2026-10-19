package br.com.acta.Api;

import java.util.List;

import br.com.acta.Model.PlanoAcao;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface PlanoAcaoApi {

    @GET("plano-acao/{id}")
    Call<PlanoAcao> buscarPorId(@Path("id") Long id);

    @GET("ciclo/{idCiclo}/plano-acao")
    Call<List<PlanoAcao>> buscarPorCiclo(@Path("idCiclo") Long idCiclo);
}
