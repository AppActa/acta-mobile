package br.com.acta.Api;

import java.util.List;

import br.com.acta.Model.Meta;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface MetaApi {

    @GET("plano-acao/{idPlanoAcao}/meta")
    Call<List<Meta>> buscarPorPlanoAcao(@Path("idPlanoAcao") Long idPlanoAcao);
}
