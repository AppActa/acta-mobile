package br.com.acta.Api;

import java.util.List;

import br.com.acta.Model.LicaoAprendida;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface LicaoAprendidaApi {

    @GET("licoes-aprendidas")
    Call<List<LicaoAprendida>> buscarTodas(@Query("apenasDoUsuarioAutenticado") boolean apenasDoUsuarioAutenticado);

    @GET("ciclos/{idCiclo}/licoes-aprendidas")
    Call<List<LicaoAprendida>> buscarPorCiclo(@Path("idCiclo") Long idCiclo);

    @GET("licoes-aprendidas/{idLicaoAprendida}")
    Call<LicaoAprendida> buscarPorId(@Path("idLicaoAprendida") String idLicaoAprendida);

    @POST("ciclos/{idCiclo}/licoes-aprendidas")
    Call<LicaoAprendida> salvarPorCiclo(@Path("idCiclo") Long idCiclo, @Body LicaoAprendida licao);
}
