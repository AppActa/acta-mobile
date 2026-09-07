package br.com.acta.Services;

import java.util.Map;

import br.com.acta.Api.UsuarioApi;
import br.com.acta.Client.RepositoryCallback;
import br.com.acta.Model.Usuario;

public class UsuarioService {
    private final UsuarioApi api;

    public UsuarioService(UsuarioApi api) {
        this.api = api;
    }

    public void patchUsuario(Long id, Map<String, Object> campos, RepositoryCallback<Usuario> callback) {
        Enqueue.enqueue(api.updateUsuario(id, campos), callback);
    }
}
