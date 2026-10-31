package br.com.acta.Services;

import android.app.NotificationManager;
import android.app.NotificationChannel;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.core.app.NotificationManagerCompat;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import br.com.acta.Api.NotificacaoApi;
import br.com.acta.Client.RetrofitClientFcm;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.installations.FirebaseInstallations;
import com.google.firebase.messaging.FirebaseMessaging;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public final class DispositivoService {
    private static final String TAG = "DispositivoService";
    private static int geracaoCadastro;
    private static Call<Void> cadastroPendente;
    private static boolean encerrandoSessao;
    private static boolean sincronizando;

    private DispositivoService() {}

    public static void iniciarSessao() { encerrandoSessao = false; }

    public static void prepararLogout() {
        encerrandoSessao = true;
        cancelarCadastro();
    }

    public static void atualizarFidRegistrado(Context contexto, String fidRegistrado) {
        if (encerrandoSessao || sincronizando || !habilitado(contexto) || !notificacoesDisponiveis(contexto) || FirebaseAuth.getInstance().getCurrentUser() == null) return;
        cadastroPendente = RetrofitClientFcm.api().registrar(new NotificacaoApi.Dispositivo(fidRegistrado));
        // se falhar, o próximo acesso tenta sincronizar de novo
        enviar(cadastroPendente, mensagemErro -> {});
    }

    // o cadastro é atualizado na thread principal
    public static void cancelarCadastro() {
        geracaoCadastro++;
        if (cadastroPendente != null) {
            cadastroPendente.cancel();
            cadastroPendente = null;
        }
    }

    private static String chave() {
        return "push_" + FirebaseAuth.getInstance().getUid();
    }

    public static boolean habilitado(Context contexto) {
        return contexto.getSharedPreferences("config_app", Context.MODE_PRIVATE).getBoolean(chave(), false);
    }

    public static boolean permissaoNotificacoesConcedida(Context contexto) {
        return NotificationManagerCompat.from(contexto).areNotificationsEnabled();
    }

    public static boolean canalNotificacoesHabilitado(Context contexto) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return true;
        NotificationManager gerenciador = contexto.getSystemService(NotificationManager.class);
        if (gerenciador == null) return false;
        NotificationChannel canal = gerenciador.getNotificationChannel(ActaMessagingService.CHANNEL);
        return canal != null && canal.getImportance() != NotificationManager.IMPORTANCE_NONE;
    }

    public static boolean notificacoesDisponiveis(Context contexto) {
        return permissaoNotificacoesConcedida(contexto) && canalNotificacoesHabilitado(contexto);
    }

    public static void salvar(Context contexto, boolean ativo) {
        contexto.getSharedPreferences("config_app", Context.MODE_PRIVATE).edit().putBoolean(chave(), ativo).apply();
    }

    // null indica sucesso, uma mensagem indica falha
    public static void sincronizar(Context contexto, boolean ativo, Consumer<String> aoConcluir) {
        if (encerrandoSessao || FirebaseAuth.getInstance().getCurrentUser() == null) {
            aoConcluir.accept("Sua sessão expirou. Entre novamente.");
            return;
        }
        if (ativo && !notificacoesDisponiveis(contexto)) {
            aoConcluir.accept("Ative as notificações e o canal Avisos ACTA nas configurações do Android para ligar o push.");
            return;
        }

        cancelarCadastro();
        int geracaoAtual = geracaoCadastro;
        sincronizando = true;
        Handler handler = new Handler(Looper.getMainLooper());
        AtomicBoolean terminou = new AtomicBoolean();
        AtomicReference<Call<Void>> chamada = new AtomicReference<>();

        Consumer<String> concluir = mensagemErro -> {
            if (terminou.compareAndSet(false, true)) {
                if (geracaoAtual == geracaoCadastro) sincronizando = false;
                aoConcluir.accept(mensagemErro);
            }
        };

        Runnable timeout = () -> {
            Log.w(TAG, "Tempo limite ao sincronizar dispositivo");
            if (chamada.get() != null) chamada.get().cancel();
            concluir.accept("A atualização demorou demais. Tente novamente.");
        };

        handler.postDelayed(timeout, 12000);
        Consumer<String> resposta = mensagemErro -> {
            handler.removeCallbacks(timeout);
            concluir.accept(mensagemErro);
        };

        if (!ativo) {
            chamada.set(RetrofitClientFcm.api().revogar());
            enviar(chamada.get(), resposta);
            return;
        }

        String uidSessao = FirebaseAuth.getInstance().getUid();
        // um fid existente pode não estar registrado
        Log.d(TAG, "Iniciando registro de notificações no Firebase");
        FirebaseMessaging.getInstance().register()
            .continueWithTask(task -> {
                if (!task.isSuccessful()) throw task.getException();
                return FirebaseInstallations.getInstance().getId();
            })
            .addOnCompleteListener(task -> {
                if (terminou.get()) return;
                if (encerrandoSessao || geracaoAtual != geracaoCadastro || !task.isSuccessful() || !uidSessao.equals(FirebaseAuth.getInstance().getUid())) {
                    if (!task.isSuccessful()) {
                        Exception erro = task.getException();
                        Log.w(TAG, "Falha no registro Firebase: " + (erro == null ? "sem detalhe" : erro.getClass().getSimpleName()));
                    }
                    resposta.accept("Não foi possível registrar este aparelho. Tente novamente.");
                    return;
                }

                Log.d(TAG, "Enviando cadastro do dispositivo");
                cadastroPendente = RetrofitClientFcm.api().registrar(new NotificacaoApi.Dispositivo(task.getResult()));
                chamada.set(cadastroPendente);
                enviar(chamada.get(), resposta);
            });
    }

    private static void enviar(Call<Void> chamada, Consumer<String> aoConcluir) {
        chamada.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<Void> chamadaRetornada, Response<Void> resposta) {
                Log.d(TAG, "Sincronização do dispositivo: HTTP " + resposta.code());
                aoConcluir.accept(resposta.isSuccessful() ? null : mensagemErro(resposta.code()));
            }

            @Override
            public void onFailure(Call<Void> chamadaRetornada, Throwable falha) {
                if (!chamadaRetornada.isCanceled()) Log.w(TAG, "Falha de conexão ao sincronizar dispositivo: " + falha.getClass().getSimpleName());
                aoConcluir.accept("Não foi possível conectar. Verifique sua conexão e tente novamente.");
            }
        });
    }

    private static String mensagemErro(int status) {
        if (status == 401) return "Sua sessão expirou. Entre novamente.";
        if (status == 403) return "Sua conta não tem permissão para atualizar as notificações.";
        if (status == 400 || status == 422) return "Não foi possível cadastrar este aparelho. Tente novamente mais tarde.";
        return "O serviço de notificações está indisponível. Tente novamente mais tarde.";
    }
}