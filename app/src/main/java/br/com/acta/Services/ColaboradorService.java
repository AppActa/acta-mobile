package br.com.acta.Services;

import com.google.gson.Gson;

import br.com.acta.Api.ColaboradorApi;
import br.com.acta.Api.MeApi;
import br.com.acta.Client.RepositoryCallback;
import br.com.acta.Model.Colaborador;
import br.com.acta.Model.Me;

public class ColaboradorService {

    private final ColaboradorApi api;
    private final Gson gson = new Gson();

    public ColaboradorService(ColaboradorApi api){this.api = api;}
    public void getColaborador(Long id,RepositoryCallback<Colaborador> callback) {
        Enqueue.enqueue(api.getColaborador(id), callback);
    }
}
