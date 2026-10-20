package br.com.acta.Services;

import java.util.List;

import br.com.acta.Api.FormularioApi;
import br.com.acta.Client.RepositoryCallback;
import br.com.acta.Model.Formulario;

public class FormularioService {
    private final FormularioApi api;

    public FormularioService(FormularioApi api) {
        this.api = api;
    }

    public void buscarPorCiclo(Long idCiclo, RepositoryCallback<List<Formulario>> callback) {
        Enqueue.enqueue(api.buscarPorCiclo(idCiclo), callback);
    }

    public void buscarPorId(String idFormulario, RepositoryCallback<Formulario> callback) {
        Enqueue.enqueue(api.buscarPorId(idFormulario), callback);
    }
}
