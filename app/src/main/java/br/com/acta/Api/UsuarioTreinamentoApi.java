package br.com.acta.Api;

import java.util.List;

import br.com.acta.Model.UsuarioTreinamento;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface UsuarioTreinamentoApi {
    @GET("api/v1/treinamento/{id}/usuario")
    Call<List<UsuarioTreinamento>> buscar(@Path("id") Long id);
}
