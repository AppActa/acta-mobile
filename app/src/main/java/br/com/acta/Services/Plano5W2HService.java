package br.com.acta.Services;

import br.com.acta.Api.Plano5W2HApi;
import br.com.acta.Client.RepositoryCallback;
import br.com.acta.Model.Plano5W2H;

public class Plano5W2HService {
    private final Plano5W2HApi api;

    public Plano5W2HService(Plano5W2HApi api) {
        this.api = api;
    }

    public void buscarPorPlanoAcao(Long idPlanoAcao, RepositoryCallback<Plano5W2H> callback) {
        Enqueue.enqueue(api.buscarPorPlanoAcao(idPlanoAcao), callback);
    }
}
