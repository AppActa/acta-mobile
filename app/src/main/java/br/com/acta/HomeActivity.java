package br.com.acta;

import android.content.Intent;
import android.os.Bundle;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.google.firebase.auth.FirebaseAuth;

import java.util.function.Consumer;

import br.com.acta.Api.CicloApi;
import br.com.acta.Api.LicaoAprendidaApi;
import br.com.acta.Api.TarefaApi;
import br.com.acta.Auth.FirebaseTokenProvider;
import br.com.acta.Client.RetrofitClient;
import br.com.acta.Client.RetrofitClientMongo;
import br.com.acta.Services.ActaMessagingService;
import br.com.acta.Services.DispositivoService;
import br.com.acta.utils.DeepLink;
import br.com.acta.utils.FormatoData;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity {

    private FormatoData.Formato formatoData;
    private NavController navController;
    private Call<?> chamadaDestino;
    private boolean entradaConsumida;

    @Override
    protected void onResume() {
        super.onResume();
        if (formatoData != FormatoData.obterFormato(this)) {
            recreate();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        cancelarDestino();
        setIntent(intent);
        entradaConsumida = false;
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        if (navController != null && chamadaDestino == null) abrirEntrada(getIntent());
    }

    private void abrirLogin() {
        Intent intent = new Intent(this, TelaLogin.class);
        intent.setData(getIntent().getData());
        intent.putExtra("idNotificacao", getIntent().getStringExtra("idNotificacao"));

        startActivity(intent);
        finish();
    }

    private void abrirEntrada(Intent intent) {
        if (!intent.hasExtra("idNotificacao") && intent.getData() == null) return;
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            abrirLogin();
            return;
        }

        String idNotificacao = intent.getStringExtra("idNotificacao");
        if (idNotificacao != null) {
            abrirCentral(idNotificacao);
            limparEntrada();
            return;
        }
        intent.removeExtra("idNotificacao");
        if (intent.getData() == null) return;

        DeepLink link = DeepLink.parse(intent.getData().toString());
        if (link == null) {
            limparEntrada();
            abrirCentral(null);
            Toast.makeText(this, "Este link não tem um destino disponível no aplicativo.", Toast.LENGTH_LONG).show();
            return;
        }
        if (!abrirDestino(link)) {
            abrirCentral(null, link.toString());
            limparEntrada();
        }
    }

    private boolean abrirDestino(DeepLink link) {
        switch (link.destino) {
            case NOTIFICACAO:
                abrirCentral(link.id);
                limparEntrada();
                return true;
            case LICAO_APRENDIDA:
                // a consulta individual atual usa UUID
                if (link.id.length() != 36) return false;
                LicaoAprendidaApi licaoApi = RetrofitClientMongo.getInstance(new FirebaseTokenProvider(FirebaseAuth.getInstance())).create(LicaoAprendidaApi.class);
                carregarDestino(licaoApi.buscarPorId(link.id), licao -> {
                    if (licao.getIdCiclo() == null || licao.getIdCiclo() <= 0) {
                        Toast.makeText(this, "Não foi possível encontrar o ciclo desta lição.", Toast.LENGTH_LONG).show();
                        return;
                    }
                    Intent destino = new Intent(this, CicloPdcaActivity.class);
                    destino.putExtra("idCiclo", licao.getIdCiclo().longValue());
                    destino.putExtra("idLicao", link.id);
                    startActivity(destino);
                });
                return true;
            case CICLO:
                CicloApi cicloApi = RetrofitClient.getInstance(new FirebaseTokenProvider(FirebaseAuth.getInstance())).create(CicloApi.class);
                carregarDestino(cicloApi.getCiclo(link.idNumerico()), ciclo -> {
                    Intent destino = new Intent(this, CicloPdcaActivity.class);
                    destino.putExtra("idCiclo", link.idNumerico());
                    startActivity(destino);
                });
                return true;
            case TAREFA:
                TarefaApi tarefaApi = RetrofitClient.getInstance(new FirebaseTokenProvider(FirebaseAuth.getInstance())).create(TarefaApi.class);
                carregarDestino(tarefaApi.buscarPorId(link.idNumerico()), tarefa -> {
                    Bundle argumentos = new Bundle();
                    argumentos.putLong("idTarefa", link.idNumerico());
                    navController.navigate(R.id.minhasTarefasFragment, argumentos);
                });
                return true;
            default:
                return false;
        }
    }

    public boolean abrirDeepLink(String endereco) {
        DeepLink link = DeepLink.parse(endereco);
        if (link == null) return false;

        cancelarDestino();
        getIntent().setData(Uri.parse(endereco));
        getIntent().removeExtra("idNotificacao");
        entradaConsumida = false;
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            abrirLogin();
            return true;
        }

        boolean encaminhado = abrirDestino(link);
        if (!encaminhado) limparEntrada();
        return encaminhado;
    }

    private void abrirCentral(String idNotificacao) {
        abrirCentral(idNotificacao, null);
    }

    private void abrirCentral(String idNotificacao, String deepLink) {
        Bundle argumentos = new Bundle();
        argumentos.putString("idNotificacao", idNotificacao);
        argumentos.putString("deepLink", deepLink);
        navController.navigate(R.id.notificacoesFragment, argumentos);
    }

    private <T> void carregarDestino(Call<T> chamada, Consumer<T> abrirDestino) {
        chamadaDestino = chamada;
        chamada.enqueue(new Callback<T>() {
            @Override
            public void onResponse(Call<T> chamadaRetornada, Response<T> resposta) {
                if (chamadaDestino != chamadaRetornada || chamadaRetornada.isCanceled() || isFinishing() || isDestroyed()) return;
                chamadaDestino = null;
                // se a tela estiver em segundo plano, o link será retomado ao voltar
                if (getSupportFragmentManager().isStateSaved()) return;

                limparEntrada();
                if (resposta.isSuccessful() && resposta.body() != null) {
                    abrirDestino.accept(resposta.body());
                } else {
                    String mensagem = "Não foi possível abrir este item. Tente novamente.";
                    if (resposta.code() == 401) mensagem = "Sua sessão expirou. Entre novamente.";
                    if (resposta.code() == 403 || resposta.code() == 404) mensagem = "Este item não está disponível ou você não tem acesso.";
                    Toast.makeText(HomeActivity.this, mensagem, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<T> chamadaRetornada, Throwable falha) {
                if (chamadaDestino != chamadaRetornada || chamadaRetornada.isCanceled() || isFinishing() || isDestroyed()) return;
                chamadaDestino = null;
                if (getSupportFragmentManager().isStateSaved()) return;

                limparEntrada();
                Toast.makeText(HomeActivity.this, "Não foi possível conectar. Abra o link novamente para tentar.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void limparEntrada() {
        entradaConsumida = true;
        getIntent().setData(null);
        getIntent().removeExtra("idNotificacao");
    }

    private void cancelarDestino() {
        if (chamadaDestino != null) {
            chamadaDestino.cancel();
            chamadaDestino = null;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle estadoSalvo) {
        estadoSalvo.putBoolean("entradaConsumida", entradaConsumida);
        super.onSaveInstanceState(estadoSalvo);
    }

    @Override
    protected void onDestroy() {
        cancelarDestino();
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        formatoData = FormatoData.obterFormato(this);
        if (savedInstanceState != null && savedInstanceState.getBoolean("entradaConsumida")) limparEntrada();
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            abrirLogin();
            return;
        }

        DispositivoService.iniciarSessao();
        ActaMessagingService.criarCanal(this);

        if (DispositivoService.habilitado(this)) DispositivoService.sincronizar(true, mensagemErro -> {});

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainHome), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

            View menu = findViewById(R.id.menuInferior);
            if (menu != null) {
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) menu.getLayoutParams();
                params.bottomMargin = systemBars.bottom;
                menu.setLayoutParams(params);
            }

            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragmentContainer);
        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
        }

        findViewById(R.id.btnMenuOpcao1).setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, Perfil.class)));
        findViewById(R.id.btnMenuOpcao2).setOnClickListener(v -> {
            if (navController != null && navController.getCurrentDestination() != null
                    && navController.getCurrentDestination().getId() != R.id.minhasTarefasFragment) {
                navController.navigate(R.id.minhasTarefasFragment);
            }
        });
        findViewById(R.id.btnMenuLogo).setOnClickListener(v -> {
            if (navController != null && navController.getCurrentDestination() != null
                    && navController.getCurrentDestination().getId() != R.id.inicioFragment) {
                navController.navigate(R.id.inicioFragment);
            }
        });
        findViewById(R.id.btnMenuOpcao5).setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, ChatActivity.class)));
    }
}
