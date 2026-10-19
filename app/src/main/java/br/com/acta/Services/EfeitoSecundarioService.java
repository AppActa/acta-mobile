package br.com.acta.Services;

import java.util.List;

import br.com.acta.Api.EfeitoSecundarioApi;
import br.com.acta.Client.RepositoryCallback;
import br.com.acta.Model.EfeitoSecundario;

public class EfeitoSecundarioService {
    private final EfeitoSecundarioApi api;

    public EfeitoSecundarioService(EfeitoSecundarioApi api) {
        this.api = api;
    }

    public void buscarEfeitosSecundarios(Long idResultado, RepositoryCallback<List<EfeitoSecundario>> callback) {
        Enqueue.enqueue(api.buscarEfeitosSecundarios(idResultado), callback);
    }
}
