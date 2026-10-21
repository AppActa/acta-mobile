package br.com.acta.Api;

import org.junit.Test;

import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static org.junit.Assert.assertEquals;

public class FormularioApiTest {
    @Test
    public void buscaFormularioPorIdComGet() {
        FormularioApi api = new Retrofit.Builder()
                .baseUrl("https://example.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(FormularioApi.class);
        String idFormulario = "123e4567-e89b-12d3-a456-426614174000";
        Request requisicao = api.buscarPorId(idFormulario).request();
        assertEquals("GET", requisicao.method());
        assertEquals("/formularios/" + idFormulario, requisicao.url().encodedPath());
    }
}
