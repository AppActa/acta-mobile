package br.com.acta.Services;

import java.io.IOException;

import br.com.acta.Api.MeApi;
import br.com.acta.Client.RepositoryCallback;
import br.com.acta.Model.AtivarRequest;
import br.com.acta.Model.Me;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MeService {
    private final MeApi api;
    public MeService(MeApi api){this.api = api;}
    public void getMe(RepositoryCallback<Me> callback) {
        executar(api.getMe(), callback);
    }

    public void ativar(String codigo, RepositoryCallback<Me> callback) {
        AtivarRequest request = new AtivarRequest(codigo);
        executar(api.ativar(request), callback);
    }

    private <T> void executar(Call<T> call, RepositoryCallback<T> callback) {
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<T> call, Response<T> response) {
                if (!response.isSuccessful()) {
                    callback.onError(response.code(), "A Auth API não aceitou a solicitação.");
                    return;
                }

                T body = response.body();
                if (body == null) {
                    callback.onError(response.code(), "A Auth API retornou uma resposta vazia.");
                    return;
                }

                callback.onSuccess(body);
            }

            @Override
            public void onFailure(Call<T> call, Throwable error) {
                if (call.isCanceled()) return;
                if (erroDeToken(error)) {
                    callback.onError(-1, "Não foi possível obter uma sessão Firebase.");
                    return;
                }
                callback.onError(0, "Não foi possível conectar à Auth API.");
            }
        });
    }

    private boolean erroDeToken(Throwable error) {
        Throwable causa = error;
        while (causa != null) {
            if (causa instanceof IOException) return true;
            causa = causa.getCause();
        }
        return false;
    }
}
