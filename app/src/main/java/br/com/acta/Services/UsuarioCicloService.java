package br.com.acta.Services;

import br.com.acta.Api.UsuarioApi;
import br.com.acta.Api.UsuarioCicloApi;
import br.com.acta.Client.RepositoryCallback;
import br.com.acta.Model.Usuario;
import br.com.acta.Model.UsuarioCiclo;

public class UsuarioCicloService {
    private final UsuarioCicloApi api;

    public UsuarioCicloService(UsuarioCicloApi api) {
        this.api = api;
    }
    public void buscarUsuarioCiclo(Long idCiclo, RepositoryCallback<UsuarioCiclo> callback){
        Enqueue.enqueue(api.buscarUsuarioCiclo(idCiclo), callback);
    }
}
