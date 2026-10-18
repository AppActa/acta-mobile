package br.com.acta.Api;

import java.util.List;

import br.com.acta.Model.Notificacao;

import retrofit2.Call;
import retrofit2.http.*;

public interface NotificacaoApi {
    @GET("api/v1/me/notificacoes")
    Call<List<Notificacao>> listar();

    @GET("api/v1/me/notificacoes/nao-lidas/quantidade")
    Call<Integer> quantidade();

    @PATCH("api/v1/me/notificacoes/{id}/lida")
    Call<Notificacao> ler(@Path("id") long idNotificacao);

    @POST("api/v1/me/notificacoes/lidas")
    Call<Integer> lerTodas();

    @PUT("api/v1/me/dispositivo")
    Call<Void> registrar(@Body Dispositivo dispositivo);

    @DELETE("api/v1/me/dispositivo")
    Call<Void> revogar();

    class Dispositivo {
        public final String fidFirebase;

        public Dispositivo(String fidFirebase) {
            this.fidFirebase = fidFirebase;
        }
    }
}