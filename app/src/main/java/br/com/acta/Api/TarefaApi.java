package br.com.acta.Api;

import java.util.List;

import br.com.acta.Model.Tarefa;
import br.com.acta.Model.TarefaStatusUpdateRequest;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.Path;

public interface TarefaApi {

    @GET("plano-acao/{idPlanoAcao}/tarefa")
    Call<List<Tarefa>> buscarPorPlanoAcao(@Path("idPlanoAcao") Long idPlanoAcao);

    @GET("tarefa/minhas")
    Call<List<Tarefa>> buscarMinhas();

    @GET("tarefa/{id}")
    Call<Tarefa> buscarPorId(@Path("id") Long id);

    @PATCH("tarefa/{id}/status")
    Call<Tarefa> patchStatus(@Path("id") Long id, @Body TarefaStatusUpdateRequest request);
}
