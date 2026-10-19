package br.com.acta.Services;

import java.util.List;

import br.com.acta.Api.MetaApi;
import br.com.acta.Client.RepositoryCallback;
import br.com.acta.Model.Meta;

public class MetaService {
    private final MetaApi api;

    public MetaService(MetaApi api) {
        this.api = api;
    }

    public void buscarPorPlanoAcao(Long idPlanoAcao, RepositoryCallback<List<Meta>> callback) {
        Enqueue.enqueue(api.buscarPorPlanoAcao(idPlanoAcao), callback);
    }
}
