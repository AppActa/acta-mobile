package br.com.acta.Services;

import java.util.List;

import br.com.acta.Api.UsuarioTreinamentoApi;
import br.com.acta.Client.RepositoryCallback;
import br.com.acta.Model.Enum.StatusTreinamento;
import br.com.acta.Model.UsuarioTreinamento;

public class UsuarioTreinamentoService {
    private final UsuarioTreinamentoApi api;

    public UsuarioTreinamentoService(UsuarioTreinamentoApi api) {
        this.api = api;
    }

    public void buscar(Long idTreinamento, RepositoryCallback<List<UsuarioTreinamento>> callback) {
        Enqueue.enqueue(api.buscar(idTreinamento), callback);
    }

    public void patchStatus(Long idTreinamento, Long idUsuario, StatusTreinamento status, RepositoryCallback<UsuarioTreinamento> callback) {
        Enqueue.enqueue(api.patchStatus(idTreinamento, idUsuario, status), callback);
    }
}
