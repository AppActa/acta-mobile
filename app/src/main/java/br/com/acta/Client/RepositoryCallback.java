package br.com.acta.Client;

public interface RepositoryCallback<T> {
    void onSuccess(T value);

    void onError(int statusCode, String message);
}
