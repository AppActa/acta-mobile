package br.com.acta.Api;

import java.util.List;

import br.com.acta.Model.RespostaFormulario;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface RespostaFormularioApi {

    @GET("formularios/{idFormulario}/respostas")
    Call<List<RespostaFormulario>> buscarPorFormulario(@Path("idFormulario") String idFormulario, @Query("idUsuario") Long idUsuario);

    @GET("respostas-formulario/{idRespostaFormulario}")
    Call<RespostaFormulario> buscarPorId(@Path("idRespostaFormulario") String idRespostaFormulario);

    @POST("formularios/{idFormulario}/respostas")
    Call<RespostaFormulario> inserir(@Path("idFormulario") String idFormulario, @Body RespostaFormulario respostaFormulario);
}
