package br.com.acta.Services;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

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
        if (encerrandoSessao || sincronizando || !habilitado(contexto) || FirebaseAuth.getInstance().getCurrentUser() == null) return;
        cadastroPendente = RetrofitClientFcm.api().registrar(new NotificacaoApi.Dispositivo(fidRegistrado));
        // se falhar, o próximo acesso tenta sincronizar de novo
        enviar(cadastroPendente, sucesso -> {});
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

    public static void salvar(Context contexto, boolean ativo) {
        contexto.getSharedPreferences("config_app", Context.MODE_PRIVATE).edit().putBoolean(chave(), ativo).apply();
    }

    public static void sincronizar(boolean ativo, Consumer<Boolean> aoConcluir) {
        if (encerrandoSessao || FirebaseAuth.getInstance().getCurrentUser() == null) {
            aoConcluir.accept(false);
            return;
        }

        cancelarCadastro();
        int geracaoAtual = geracaoCadastro;
        sincronizando = true;
        Handler handler = new Handler(Looper.getMainLooper());
        AtomicBoolean terminou = new AtomicBoolean();
        AtomicReference<Call<Void>> chamada = new AtomicReference<>();

        Consumer<Boolean> concluir = ok -> {
            if (terminou.compareAndSet(false, true)) {
                if (geracaoAtual == geracaoCadastro) sincronizando = false;
                aoConcluir.accept(ok);
            }
        };

        Runnable timeout = () -> {
            if (chamada.get() != null) chamada.get().cancel();
            concluir.accept(false);
        };

        handler.postDelayed(timeout, 12000);
        Consumer<Boolean> resposta = ok -> {
            handler.removeCallbacks(timeout);
            concluir.accept(ok);
        };

        if (!ativo) {
            chamada.set(RetrofitClientFcm.api().revogar());
            enviar(chamada.get(), resposta);
            return;
        }

        String uidSessao = FirebaseAuth.getInstance().getUid();
        // um fid existente pode não estar registrado
        FirebaseMessaging.getInstance().register()
            .continueWithTask(task -> {
                if (!task.isSuccessful()) throw task.getException();
                return FirebaseInstallations.getInstance().getId();
            })
            .addOnCompleteListener(task -> {
                if (terminou.get()) return;
                if (encerrandoSessao || geracaoAtual != geracaoCadastro || !task.isSuccessful() || !uidSessao.equals(FirebaseAuth.getInstance().getUid())) {
                    resposta.accept(false);
                    return;
                }

                cadastroPendente = RetrofitClientFcm.api().registrar(new NotificacaoApi.Dispositivo(task.getResult()));
                chamada.set(cadastroPendente);
                enviar(chamada.get(), resposta);
            });
    }

    private static void enviar(Call<Void> chamada, Consumer<Boolean> aoConcluir) {
        chamada.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<Void> chamadaRetornada, Response<Void> resposta) {
                aoConcluir.accept(resposta.isSuccessful());
            }

            @Override
            public void onFailure(Call<Void> chamadaRetornada, Throwable falha) {
                aoConcluir.accept(false);
            }
        });
    }
}