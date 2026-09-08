package br.com.acta.Services;

import com.google.gson.Gson;

import br.com.acta.Api.MeApi;
import br.com.acta.Client.RepositoryCallback;
import br.com.acta.Model.Me;

public class MeService {
    private final MeApi api;
    private final Gson gson = new Gson();

    public MeService(MeApi api){this.api = api;}
    public void getMe(RepositoryCallback<Me> callback) {
        Enqueue.enqueue(api.getMe(), callback);
    }



}
