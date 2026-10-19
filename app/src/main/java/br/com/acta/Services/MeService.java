package br.com.acta.Services;

import br.com.acta.Api.MeApi;
import br.com.acta.Client.RepositoryCallback;
import br.com.acta.Model.Me;
import br.com.acta.Model.AtivarRequest;

public class MeService {
    private final MeApi api;
    public MeService(MeApi api){this.api = api;}
    public void getMe(RepositoryCallback<Me> callback) {
        Enqueue.enqueue(api.getMe(), callback);
    }

    public void ativar(String codigo, RepositoryCallback<Me> callback) {
        AtivarRequest request = new AtivarRequest(codigo);
        Enqueue.enqueue(api.ativar(request), callback);
    }
}