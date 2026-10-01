package br.com.acta.Api;

import java.util.List;
import java.util.Map;

import br.com.acta.Model.Usuario;
import br.com.acta.Model.UsuarioCiclo;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.Path;

public interface UsuarioApi {

    @PATCH("usuario/{id}")
    Call<Usuario> updateUsuario(@Path("id") Long id,
                                @Body Map<String, Object> campos);

    @GET("usuario/{id}")
    Call<Usuario> buscarUsuario(@Path("id") Long id);

    @GET("usuario/ciclos-usuario/{idUsuario}")
    Call<List<UsuarioCiclo>> buscarCiclosUsuario(@Path("idUsuario") Long idUsuario);
}
