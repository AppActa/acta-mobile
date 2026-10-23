package br.com.acta;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import br.com.acta.BuildConfig;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import br.com.acta.Api.GeocodingApi;
import br.com.acta.Client.RetrofitClientGeocoding;
import br.com.acta.Model.GeocodingResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TelaMapa extends AppCompatActivity implements OnMapReadyCallback {

    public static final String EXTRA_LATITUDE = "latitude";
    public static final String EXTRA_LONGITUDE = "longitude";
    public static final String EXTRA_ID_PERGUNTA = "idPergunta";
    public static final String EXTRA_CEP = "cep";
    private static final String TAG = "TelaMapa";
    private final ExecutorService executorBusca = Executors.newSingleThreadExecutor();
    private GoogleMap mapa;
    private TextInputLayout campoBusca;
    private TextInputEditText textoBusca;
    private MaterialButton botaoBuscar;
    private MaterialButton botaoMinhaPosicao;
    private MaterialButton botaoUtilizarEndereco;
    private TextView enderecoLugar;
    private View progresso;
    private boolean buscando;
    private LatLng localSelecionado;
    private CancellationTokenSource cancelamentoLocalizacao;
    private Call<GeocodingResponse> chamadaGeocoding;
    private final ActivityResultLauncher<String[]> pedirPermissao = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(), resposta -> {
                if (temPermissao()) {
                    ativarMinhaPosicao();
                } else {
                    campoBusca.setError(getString(R.string.mapa_permissao_negada));
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_tela_mapa);
        WindowInsetsControllerCompat controller = new WindowInsetsControllerCompat(getWindow(), getWindow().getDecorView());
        controller.setAppearanceLightStatusBars(true);
        controller.setAppearanceLightNavigationBars(true);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (view, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        campoBusca = findViewById(R.id.campoBuscaEndereco);
        textoBusca = findViewById(R.id.edtBuscarEndereco);
        botaoBuscar = findViewById(R.id.btnBuscarEndereco);
        botaoMinhaPosicao = findViewById(R.id.btnMinhaPosicao);
        botaoUtilizarEndereco = findViewById(R.id.btnUtilizarEndereco);
        enderecoLugar = findViewById(R.id.txtEnderecoLugar);
        progresso = findViewById(R.id.progresso);
        findViewById(R.id.btnVoltarMapa).setOnClickListener(view -> finish());
        botaoBuscar.setEnabled(false);
        botaoMinhaPosicao.setEnabled(false);
        botaoUtilizarEndereco.setOnClickListener(view -> utilizarEndereco());
        botaoMinhaPosicao.setOnClickListener(view -> {
            if (temPermissao()) {
                ativarMinhaPosicao();
            } else {
                pedirPermissao.launch(new String[] {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION});
            }
        });
        botaoBuscar.setOnClickListener(view -> buscarEndereco());
        textoBusca.setOnEditorActionListener((view, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                buscarEndereco();
                return true;
            }
            return false;
        });

        SupportMapFragment fragmentMapa = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapa);
        if (fragmentMapa != null) {
            fragmentMapa.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mapa = googleMap;
        mapa.getUiSettings().setMapToolbarEnabled(false);
        mapa.getUiSettings().setZoomControlsEnabled(true);
        mapa.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(-14.235, -51.9253), 3.5f));
        botaoBuscar.setEnabled(true);
        botaoMinhaPosicao.setEnabled(true);
    }

    private boolean temPermissao() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void ativarMinhaPosicao() {
        if (mapa == null || buscando || !temPermissao()) {
            return;
        }
        campoBusca.setError(null);
        iniciarBusca();
        cancelamentoLocalizacao = new CancellationTokenSource();
        try {
            LocationServices.getFusedLocationProviderClient(this)
                    .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cancelamentoLocalizacao.getToken())
                    .addOnSuccessListener(localizacao -> {
                        if (isFinishing() || isDestroyed()) {
                            return;
                        }
                        finalizarBusca();
                        if (localizacao == null) {
                            campoBusca.setError(getString(R.string.mapa_localizacao_indisponivel));
                            return;
                        }
                        mostrarLocal(new LatLng(localizacao.getLatitude(), localizacao.getLongitude()), getString(R.string.mapa_minha_posicao));
                    })
                    .addOnFailureListener(exception -> {
                        if (isFinishing() || isDestroyed()) {
                            return;
                        }
                        Log.e(TAG, "Erro ao obter localização atual", exception);
                        finalizarBusca();
                        campoBusca.setError(getString(R.string.mapa_localizacao_indisponivel));
                    });
        } catch (SecurityException exception) {
            Log.e(TAG, "Permissão de localização indisponível", exception);
            finalizarBusca();
            campoBusca.setError(getString(R.string.mapa_permissao_negada));
        }
    }

    private void buscarEndereco() {
        if (mapa == null || buscando) {
            return;
        }
        String consulta = textoBusca.getText() == null ? "" : textoBusca.getText().toString().trim();
        campoBusca.setError(null);
        if (consulta.isEmpty()) {
            campoBusca.setError(getString(R.string.mapa_busca_vazia));
            return;
        }
        if (!Geocoder.isPresent()) {
            campoBusca.setError(getString(R.string.mapa_busca_indisponivel));
            return;
        }

        WindowInsetsControllerCompat controller = ViewCompat.getWindowInsetsController(textoBusca);
        if (controller != null) {
            controller.hide(WindowInsetsCompat.Type.ime());
        }
        iniciarBusca();
        executorBusca.execute(() -> {
            try {
                // a busca bloqueante fica fora da thread da tela para atender também ao Android 11 e 12
                Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                List<Address> enderecos = geocoder.getFromLocationName(consulta, 1);
                Address endereco = enderecos == null || enderecos.isEmpty() ? null : enderecos.get(0);
                runOnUiThread(() -> mostrarEndereco(endereco, consulta));
            } catch (IOException | IllegalArgumentException exception) {
                Log.e(TAG, "Erro ao buscar endereço", exception);
                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed()) {
                        return;
                    }
                    finalizarBusca();
                    campoBusca.setError(getString(R.string.mapa_erro_busca));
                });
            }
        });
    }

    private void mostrarEndereco(Address endereco, String consulta) {
        if (isFinishing() || isDestroyed()) {
            return;
        }
        finalizarBusca();
        if (endereco == null || !endereco.hasLatitude() || !endereco.hasLongitude()) {
            campoBusca.setError(getString(R.string.mapa_sem_resultado));
            return;
        }

        String descricao = endereco.getAddressLine(0);
        if (descricao == null || descricao.trim().isEmpty()) {
            descricao = consulta;
        }
        LatLng local = new LatLng(endereco.getLatitude(), endereco.getLongitude());
        mostrarLocal(local, descricao);
    }

    private void mostrarLocal(LatLng local, String descricao) {
        localSelecionado = local;
        Intent resultado = new Intent();
        resultado.putExtra(EXTRA_LATITUDE, localSelecionado.latitude);
        resultado.putExtra(EXTRA_LONGITUDE, localSelecionado.longitude);
        setResult(RESULT_OK, resultado);

        mapa.addMarker(new MarkerOptions().position(local).title(descricao)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)));
        mapa.animateCamera(CameraUpdateFactory.newLatLngZoom(local, 16f));
        enderecoLugar.setText(getString(R.string.mapa_coordenadas, local.latitude, local.longitude));
        enderecoLugar.setVisibility(View.VISIBLE);
        botaoUtilizarEndereco.setEnabled(true);
    }

    private void iniciarBusca() {
        buscando = true;
        localSelecionado = null;
        setResult(RESULT_CANCELED);
        botaoBuscar.setEnabled(false);
        botaoMinhaPosicao.setEnabled(false);
        progresso.setVisibility(View.VISIBLE);
        enderecoLugar.setVisibility(View.GONE);
        botaoUtilizarEndereco.setEnabled(false);
        mapa.clear();
    }

    private void utilizarEndereco() {
        if (localSelecionado == null || chamadaGeocoding != null || BuildConfig.MAPS_API_KEY.trim().isEmpty()) {
            campoBusca.setError(getString(R.string.mapa_erro_cep));
            return;
        }
        campoBusca.setError(null);
        botaoUtilizarEndereco.setEnabled(false);
        progresso.setVisibility(View.VISIBLE);
        String coordenadas = localSelecionado.latitude + "," + localSelecionado.longitude;
        GeocodingApi api = RetrofitClientGeocoding.getInstance().create(GeocodingApi.class);
        chamadaGeocoding = api.buscarPorCoordenadas(coordenadas, BuildConfig.MAPS_API_KEY);
        chamadaGeocoding.enqueue(new Callback<GeocodingResponse>() {
            @Override
            public void onResponse(Call<GeocodingResponse> chamada, Response<GeocodingResponse> resposta) {
                chamadaGeocoding = null;
                progresso.setVisibility(View.GONE);
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                String cep = extrairCep(resposta);
                if (cep == null) {
                    botaoUtilizarEndereco.setEnabled(true);
                    campoBusca.setError(getString(R.string.mapa_cep_nao_encontrado));
                    return;
                }
                Intent resultado = new Intent();
                resultado.putExtra(EXTRA_ID_PERGUNTA, getIntent().getStringExtra(EXTRA_ID_PERGUNTA));
                resultado.putExtra(EXTRA_CEP, cep);
                setResult(RESULT_OK, resultado);
                finish();
            }

            @Override
            public void onFailure(Call<GeocodingResponse> chamada, Throwable erro) {
                chamadaGeocoding = null;
                if (isFinishing() || isDestroyed() || chamada.isCanceled()) {
                    return;
                }
                progresso.setVisibility(View.GONE);
                botaoUtilizarEndereco.setEnabled(true);
                Log.e(TAG, "Não foi possível consultar o CEP do endereço selecionado", erro);
                campoBusca.setError(getString(R.string.mapa_erro_cep));
            }
        });
    }

    private String extrairCep(Response<GeocodingResponse> resposta) {
        if (!resposta.isSuccessful() || resposta.body() == null || !"OK".equals(resposta.body().getStatus())
                || resposta.body().getResults() == null) {
            return null;
        }
        for (GeocodingResponse.Resultado resultado : resposta.body().getResults()) {
            if (resultado.getAddressComponents() == null) {
                continue;
            }
            for (GeocodingResponse.Componente componente : resultado.getAddressComponents()) {
                if (componente.getTypes() != null && componente.getTypes().contains("postal_code")
                        && componente.getLongName() != null && componente.getLongName().matches("\\d{5}-?\\d{3}")) {
                    return componente.getLongName();
                }
            }
        }
        return null;
    }

    private void finalizarBusca() {
        buscando = false;
        botaoBuscar.setEnabled(true);
        botaoMinhaPosicao.setEnabled(true);
        progresso.setVisibility(View.GONE);
    }

    @Override
    protected void onDestroy() {
        if (chamadaGeocoding != null) {
            chamadaGeocoding.cancel();
        }
        if (cancelamentoLocalizacao != null) {
            cancelamentoLocalizacao.cancel();
        }
        executorBusca.shutdownNow();
        super.onDestroy();
    }
}
