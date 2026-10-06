package br.com.acta.Services;

import java.util.List;

import br.com.acta.Api.TarefaApi;
import br.com.acta.Client.RepositoryCallback;
import br.com.acta.Model.Tarefa;

public class TarefaService {
    private final TarefaApi tarefaApi;

    public TarefaService(TarefaApi tarefaApi) {
        this.tarefaApi = tarefaApi;
    }

    public void buscarPorPlanoAcao(Long idPlanoAcao, RepositoryCallback<List<Tarefa>> callback) {
        Enqueue.enqueue(tarefaApi.buscarPorPlanoAcao(idPlanoAcao), callback);
    }

    public void buscarMinhas(RepositoryCallback<List<Tarefa>> callback) {
        Enqueue.enqueue(tarefaApi.buscarMinhas(), callback);
    }

    public void buscarPorId(Long id, RepositoryCallback<Tarefa> callback) {
        Enqueue.enqueue(tarefaApi.buscarPorId(id), callback);
    }
}
