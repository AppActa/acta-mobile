package br.com.acta.Services;

import java.util.List;

import br.com.acta.Api.PlanoAcaoApi;
import br.com.acta.Client.RepositoryCallback;
import br.com.acta.Model.PlanoAcao;

public class PlanoAcaoService {
    private final PlanoAcaoApi planoAcaoApi;

    public PlanoAcaoService(PlanoAcaoApi planoAcaoApi) {
        this.planoAcaoApi = planoAcaoApi;
    }

    public void buscarPorId(Long id, RepositoryCallback<PlanoAcao> callback) {
        Enqueue.enqueue(planoAcaoApi.buscarPorId(id), callback);
    }

    public void buscarPorCiclo(Long idCiclo, RepositoryCallback<List<PlanoAcao>> callback) {
        Enqueue.enqueue(planoAcaoApi.buscarPorCiclo(idCiclo), callback);
    }
}
