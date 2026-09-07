package br.com.acta.Api;

import java.util.Map;

import br.com.acta.Model.Usuario;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.PATCH;
import retrofit2.http.Path;

public interface UsuarioApi {

    @PATCH("/usuario/{id}")
    Call<Usuario> updateUsuario(@Path("id") Long id,
                                @Body Map<String, Object> campos);
}
