package br.com.acta.Services;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.List;

import br.com.acta.Client.RepositoryCallback;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Enqueue {
    private static Gson gson = new Gson();
    public static <T> void enqueue(Call<T> call, RepositoryCallback<T> callback) {
        call.enqueue(new Callback<T>() {
            @Override
            public void onResponse(Call<T> call, Response<T> response) {
                T body = response.body();
                if (response.isSuccessful() && body != null) {
                    callback.onSuccess(body);
                    return;
                }
                callback.onError(response.code(), readError(response));
            }

            @Override
            public void onFailure(Call<T> call, Throwable throwable) {
                callback.onError(0, "Não foi possível acessar a API ACTA.");
            }
        });
    }

    private static String readError(Response<?> response) {
        ResponseBody errorBody = response.errorBody();
        if (errorBody != null) {
            try {
                ApiError error = gson.fromJson(errorBody.string(), ApiError.class);
                if (error != null && error.mensagens != null && !error.mensagens.isEmpty()) {
                    return String.join("\n", error.mensagens);
                }
            } catch (IOException | RuntimeException ignored) {
            }
        }

        if (response.code() == 401) {
            return "A sessão Firebase não foi aceita pela API.";
        }
        if (response.code() == 403) {
            return "Sua conta não tem acesso a este recurso.";
        }
        return "A API respondeu com HTTP " + response.code() + ".";
    }

    private static final class ApiError {
        private List<String> mensagens;
    }
}
