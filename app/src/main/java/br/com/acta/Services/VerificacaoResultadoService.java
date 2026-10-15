package br.com.acta.Services;

import java.util.List;

import br.com.acta.Api.VerificacaoResultadoApi;
import br.com.acta.Client.RepositoryCallback;
import br.com.acta.Model.VerificacaoResultado;

public class VerificacaoResultadoService {
    private final VerificacaoResultadoApi api;

    public VerificacaoResultadoService(VerificacaoResultadoApi api) {
        this.api = api;
    }

    public void buscarPorCiclo(Long idCiclo, RepositoryCallback<List<VerificacaoResultado>> callback) {
        Enqueue.enqueue(api.buscarPorCiclo(idCiclo), callback);
    }

    public void buscarPorId(Long id, RepositoryCallback<VerificacaoResultado> callback) {
        Enqueue.enqueue(api.buscarPorId(id), callback);
    }
}
