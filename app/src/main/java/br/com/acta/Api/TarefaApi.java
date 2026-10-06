package br.com.acta.Api;

import java.util.List;

import br.com.acta.Model.Tarefa;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface TarefaApi {

    @GET("plano-acao/{idPlanoAcao}/tarefa")
    Call<List<Tarefa>> buscarPorPlanoAcao(@Path("idPlanoAcao") Long idPlanoAcao);

    @GET("tarefa/minhas")
    Call<List<Tarefa>> buscarMinhas();

    @GET("tarefa/{id}")
    Call<Tarefa> buscarPorId(@Path("id") Long id);
}
