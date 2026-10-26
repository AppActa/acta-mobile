package br.com.acta.Services;

import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import br.com.acta.Client.RepositoryCallback;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Enqueue {
    private static final String TAG = "Enqueue";
    public static <T> void enqueue(Call<T> call, RepositoryCallback<T> callback) {
        call.enqueue(new Callback<>() {
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
                if (call.isCanceled()) return;
                callback.onError(0, "Não foi possível conectar à API ACTA.");
                Log.e(TAG, "Falha ao acessar a API ACTA.", throwable);
            }
        });
    }

    private static String readError(Response<?> response) {
        ResponseBody errorBody = response.errorBody();
        if (errorBody != null) {
            try (ResponseBody body = errorBody) {
                String json = body.string();
                JsonElement raiz = JsonParser.parseString(json);
                if (!raiz.isJsonObject()) return json;
                JsonObject erro = raiz.getAsJsonObject();
                String mensagens = lerMensagens(erro.get("mensagens"));
                if (!mensagens.isEmpty()) return mensagens;
                JsonElement detalhe = erro.get("detail");
                if (detalhe != null && !detalhe.isJsonNull()) {
                    if (detalhe.isJsonPrimitive()) return detalhe.getAsString();
                    if (detalhe.isJsonArray()) {
                        mensagens = lerMensagens(detalhe);
                        if (!mensagens.isEmpty()) return mensagens;
                    }
                    return detalhe.toString();
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

    private static String lerMensagens(JsonElement elemento) {
        if (elemento == null || !elemento.isJsonArray()) return "";
        JsonArray lista = elemento.getAsJsonArray();
        List<String> mensagens = new ArrayList<>();
        for (JsonElement item : lista) {
            if (item == null || item.isJsonNull()) continue;
            if (item.isJsonPrimitive()) {
                mensagens.add(item.getAsString());
            } else if (item.isJsonObject()) {
                JsonElement mensagem = item.getAsJsonObject().get("msg");
                mensagens.add(mensagem == null ? item.toString() : mensagem.getAsString());
            } else {
                mensagens.add(item.toString());
            }
        }
        return String.join("\n", mensagens);
    }
}
