package br.com.acta.Services;

import java.util.List;

import br.com.acta.Api.LicaoAprendidaApi;
import br.com.acta.Client.RepositoryCallback;
import br.com.acta.Model.LicaoAprendida;

public class LicaoAprendidaService {
    private final LicaoAprendidaApi api;

    public LicaoAprendidaService(LicaoAprendidaApi api) {
        this.api = api;
    }

    public void buscarPorCiclo(Long idCiclo, RepositoryCallback<List<LicaoAprendida>> callback) {
        Enqueue.enqueue(api.buscarPorCiclo(idCiclo), callback);
    }

    public void salvarPorCiclo(Long idCiclo, LicaoAprendida licao, RepositoryCallback<LicaoAprendida> callback) {
        Enqueue.enqueue(api.salvarPorCiclo(idCiclo, licao), callback);
    }
}
