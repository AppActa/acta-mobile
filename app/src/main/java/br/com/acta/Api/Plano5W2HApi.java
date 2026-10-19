package br.com.acta.Api;

import br.com.acta.Model.Plano5W2H;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface Plano5W2HApi {
    @GET("plano-acao/{idPlanoAcao}/5w2h")
    Call<Plano5W2H> buscarPorPlanoAcao(@Path("idPlanoAcao") Long idPlanoAcao);
}
