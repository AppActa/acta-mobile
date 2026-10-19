package br.com.acta.Services;

import com.google.gson.Gson;

import br.com.acta.Api.CicloApi;
import br.com.acta.Api.ColaboradorApi;
import br.com.acta.Client.RepositoryCallback;
import br.com.acta.Model.Ciclo;
import br.com.acta.Model.Colaborador;

public class CicloService {

    private final CicloApi api;
    private final Gson gson = new Gson();

    public CicloService(CicloApi api){this.api = api;}
    public void getCiclo(Long id, RepositoryCallback<Ciclo> callback) {
        Enqueue.enqueue(api.getCiclo(id), callback);
    }
}
