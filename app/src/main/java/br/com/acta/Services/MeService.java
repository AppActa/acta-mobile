package br.com.acta.Services;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.List;

import br.com.acta.Api.MeApi;
import br.com.acta.Client.RepositoryCallback;
import br.com.acta.Model.Me;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MeService {
    private final MeApi api;
    private final Gson gson = new Gson();

    public MeService(MeApi api){this.api = api;}
    public void getMe(RepositoryCallback<Me> callback) {
        Enqueue.enqueue(api.getMe(), callback);
    }



}
