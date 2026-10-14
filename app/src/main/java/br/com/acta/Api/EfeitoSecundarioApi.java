package br.com.acta.Api;

import java.util.List;

import br.com.acta.Model.EfeitoSecundario;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface EfeitoSecundarioApi {
    @GET("verificacao/{idResultado}/efeito-secundario")
    Call<List<EfeitoSecundario>> buscarEfeitosSecundarios(@Path("idResultado") Long idResultado);
}
