package br.com.acta.Services;

import java.util.List;

import br.com.acta.Api.PlanoAcaoApi;
import br.com.acta.Client.RepositoryCallback;
import br.com.acta.Model.PlanoAcao;

public class PlanoAcaoService {
    private final PlanoAcaoApi api;

    public PlanoAcaoService(PlanoAcaoApi api) {
        this.api = api;
    }

    public void buscarPorId(Long id, RepositoryCallback<PlanoAcao> callback) {
        Enqueue.enqueue(api.buscarPorId(id), callback);
    }

    public void buscarPorCiclo(Long idCiclo, RepositoryCallback<List<PlanoAcao>> callback) {
        Enqueue.enqueue(api.buscarPorCiclo(idCiclo), callback);
    }
}
