package br.com.acta.Api;

import br.com.acta.Model.Usuario;
import br.com.acta.Model.UsuarioCiclo;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface UsuarioCicloApi {
    @GET("ciclo/{idCiclo}/usuario")
    Call<UsuarioCiclo> buscarUsuarioCiclo(@Path("idCiclo") Long id);
}
