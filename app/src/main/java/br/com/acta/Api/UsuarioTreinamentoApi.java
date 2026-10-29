package br.com.acta.Api;

import java.util.List;

import br.com.acta.Model.Enum.StatusTreinamento;
import br.com.acta.Model.UsuarioTreinamento;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface UsuarioTreinamentoApi {
    @GET("treinamento/{id}/usuario")
    Call<List<UsuarioTreinamento>> buscar(@Path("id") Long id);

    @PATCH("treinamento/{id}/usuario/{idUsuario}")
    Call<UsuarioTreinamento> patchStatus(
            @Path("id") Long idTreinamento,
            @Path("idUsuario") Long idUsuario,
            @Query("status") StatusTreinamento status);
}
