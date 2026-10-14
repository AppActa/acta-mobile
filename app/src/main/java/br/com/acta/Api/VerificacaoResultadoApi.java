package br.com.acta.Api;

import java.util.List;

import br.com.acta.Model.VerificacaoResultado;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface VerificacaoResultadoApi {

    @GET("ciclos/{idCiclo}/verificacoes")
    Call<List<VerificacaoResultado>> buscarPorCiclo(@Path("idCiclo") Long idCiclo);

    @GET("verificacoes/{id}")
    Call<VerificacaoResultado> buscarPorId(@Path("id") Long id);
}
