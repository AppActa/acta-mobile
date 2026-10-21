package br.com.acta.Services;

import java.util.List;

import br.com.acta.Api.RespostaFormularioApi;
import br.com.acta.Client.RepositoryCallback;
import br.com.acta.Model.RespostaFormulario;

public class RespostaFormularioService {
    private final RespostaFormularioApi api;

    public RespostaFormularioService(RespostaFormularioApi api) {
        this.api = api;
    }

    public void buscarPorFormulario(String idFormulario, Long idUsuario, RepositoryCallback<List<RespostaFormulario>> callback) {
        Enqueue.enqueue(api.buscarPorFormulario(idFormulario, idUsuario), callback);
    }

    public void buscarPorId(String idRespostaFormulario, RepositoryCallback<RespostaFormulario> callback) {
        Enqueue.enqueue(api.buscarPorId(idRespostaFormulario), callback);
    }

    public void inserir(String idFormulario, RespostaFormulario respostaFormulario, RepositoryCallback<RespostaFormulario> callback) {
        Enqueue.enqueue(api.inserir(idFormulario, respostaFormulario), callback);
    }
}
