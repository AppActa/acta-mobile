package br.com.acta.Services;

import java.util.List;
import java.util.Map;

import br.com.acta.Api.UsuarioApi;
import br.com.acta.Client.RepositoryCallback;
import br.com.acta.Model.Usuario;
import br.com.acta.Model.UsuarioCiclo;

public class UsuarioService {
    public interface NomeGestorCallback {
        void onNomeRecebido(String nome);
    }

    private final UsuarioApi api;

    public UsuarioService(UsuarioApi api) {
        this.api = api;
    }

    public void patchUsuario(Long id, Map<String, Object> campos, RepositoryCallback<Usuario> callback) {
        Enqueue.enqueue(api.updateUsuario(id, campos), callback);
    }

    public void buscarUsuario(Long id, RepositoryCallback<Usuario> callback) {
        Enqueue.enqueue(api.buscarUsuario(id), callback);
    }

    public void buscarNomeGestor(Long idGestor, NomeGestorCallback callback) {
        if (callback == null) return;
        if (idGestor == null) {
            callback.onNomeRecebido("Não informado");
            return;
        }

        buscarUsuario(idGestor, new RepositoryCallback<Usuario>() {
            @Override
            public void onSuccess(Usuario usuario) {
                String nome = usuario == null || usuario.getNome() == null || usuario.getNome().trim().isEmpty()
                        ? "Não informado"
                        : usuario.getNome().trim();
                callback.onNomeRecebido(nome);
            }

            @Override
            public void onError(int statusCode, String message) {
                callback.onNomeRecebido("Não informado");
            }
        });
    }

    public void buscarCiclosUsuario(Long idUsuario, RepositoryCallback<List<UsuarioCiclo>> callback) {
        Enqueue.enqueue(api.buscarCiclosUsuario(idUsuario), callback);
    }
}
