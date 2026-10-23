package br.com.acta.Api;

import java.util.List;

import br.com.acta.Model.Formulario;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface FormularioApi {

    @GET("formularios/{idFormulario}")
    Call<Formulario> buscarPorId(@Path("idFormulario") String idFormulario);

    @GET("ciclos/{idCiclo}/formularios")
    Call<List<Formulario>> buscarPorCiclo(@Path("idCiclo") Long idCiclo, @Query("apenasDoUsuarioAutenticado") boolean apenasDoUsuarioAutenticado);
}
