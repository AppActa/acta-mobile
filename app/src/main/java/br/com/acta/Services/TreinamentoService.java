package br.com.acta.Services;

import java.util.List;
import java.util.Map;

import br.com.acta.Api.TreinamentoApi;
import br.com.acta.Client.RepositoryCallback;
import br.com.acta.Model.Treinamento;

public class TreinamentoService {
    private final TreinamentoApi api;

    public TreinamentoService(TreinamentoApi api) {
        this.api = api;
    }

    public void buscarTreinamentos(Long idCiclo, RepositoryCallback<List<Treinamento>> callback) {
        Enqueue.enqueue(api.buscarTreinamentos(idCiclo), callback);
    }

    public void buscar(Long id, RepositoryCallback<Treinamento> callback) {
        Enqueue.enqueue(api.buscar(id), callback);
    }

    public void patch(Long id, Map<String, Object> campos, RepositoryCallback<Treinamento> callback) {
        Enqueue.enqueue(api.patch(id, campos), callback);
    }
}
