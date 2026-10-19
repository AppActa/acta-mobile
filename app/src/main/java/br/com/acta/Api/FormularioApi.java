package br.com.acta.Api;

import java.util.List;

import br.com.acta.Model.Formulario;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface FormularioApi {

    @GET("ciclos/{idCiclo}/formularios")
    Call<List<Formulario>> buscarPorCiclo(@Path("idCiclo") Long idCiclo);
}
