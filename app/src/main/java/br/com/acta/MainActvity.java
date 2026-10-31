package br.com.acta;

import android.content.Intent;
import android.content.SharedPreferences;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.constraintlayout.motion.widget.MotionLayout;
import com.cloudinary.android.MediaManager;
import com.google.firebase.auth.FirebaseAuth;
import br.com.acta.Api.MeApi;
import br.com.acta.Auth.FirebaseTokenProvider;
import br.com.acta.Client.RepositoryCallback;
import br.com.acta.Client.RetrofitClientAuth;
import br.com.acta.Model.Me;
import br.com.acta.Services.MeService;
import java.util.HashMap;
import java.util.Map;

public class MainActvity extends AppCompatActivity {
    private static final long DURACAO_INDICADOR_LOGIN_MS = 650L;
    private MotionLayout btnComecar;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private ValueAnimator animacaoSkeleton;
    private EstadoSessao estadoSessao = EstadoSessao.SEM_PERFIL_ATIVO;
    private boolean loginSolicitado;
    private boolean navegacaoAgendada;

    private enum EstadoSessao {
        VERIFICANDO,
        PERFIL_ATIVO,
        SEM_PERFIL_ATIVO
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String idNotificacao = getIntent().getStringExtra("idNotificacao");
        if (idNotificacao == null) idNotificacao = getIntent().getStringExtra("notificationId");
        if (idNotificacao != null || (Intent.ACTION_VIEW.equals(getIntent().getAction()) && getIntent().getData() != null)) {
            Intent intent = new Intent(this, HomeActivity.class);
            intent.setData(getIntent().getData());
            intent.putExtra("idNotificacao", idNotificacao);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

            startActivity(intent);
            finish();
            return;
        }

        inicializarCloudinary();
        aplicarTemaSalvo();
        mostrarBoasVindas();

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            estadoSessao = EstadoSessao.VERIFICANDO;
            verificarSessaoExistente();
        }
    }

    private void inicializarCloudinary() {
        try {
            Map<String, String> config = new HashMap<>();
            config.put("cloud_name", BuildConfig.CLOUDINARY_CLOUD_NAME);
            config.put("api_key", BuildConfig.CLOUDINARY_API_KEY);
            config.put("api_secret", BuildConfig.CLOUDINARY_API_SECRET);

            MediaManager.init(this, config);
        } catch (IllegalStateException e) {
            // Evita crash caso inicialize duas vezes
        }
    }

    private void aplicarTemaSalvo() {
        SharedPreferences preferences = getSharedPreferences("config_app", MODE_PRIVATE);
        boolean isModoClaro = preferences.getBoolean("is_modo_claro", true);
        AppCompatDelegate.setDefaultNightMode(isModoClaro ? AppCompatDelegate.MODE_NIGHT_NO : AppCompatDelegate.MODE_NIGHT_YES);
    }

    private void verificarSessaoExistente() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        MeApi api = RetrofitClientAuth.getInstance(new FirebaseTokenProvider(auth)).create(MeApi.class);
        new MeService(api).getMe(new RepositoryCallback<>() {
            @Override
            public void onSuccess(Me value) {
                if (isFinishing() || isDestroyed()) return;
                if (value != null && value.temPerfilActa()) {
                    estadoSessao = EstadoSessao.PERFIL_ATIVO;
                    if (loginSolicitado) agendarNavegacao(MainActvity.this::abrirHome, DURACAO_INDICADOR_LOGIN_MS);
                } else {
                    estadoSessao = EstadoSessao.SEM_PERFIL_ATIVO;
                    if (loginSolicitado) agendarNavegacao(MainActvity.this::abrirLogin, DURACAO_INDICADOR_LOGIN_MS);
                }
            }

            @Override
            public void onError(int statusCode, String error) {
                if (isFinishing() || isDestroyed()) return;
                if (statusCode == 401) auth.signOut();
                estadoSessao = EstadoSessao.SEM_PERFIL_ATIVO;
                if (loginSolicitado) agendarNavegacao(MainActvity.this::abrirLogin, DURACAO_INDICADOR_LOGIN_MS);
            }
        });
    }

    private void agendarNavegacao(Runnable acao, long atraso) {
        if (navegacaoAgendada) return;
        navegacaoAgendada = true;
        handler.postDelayed(() -> {
            if (!isFinishing() && !isDestroyed()) acao.run();
        }, atraso);
    }

    private void solicitarLoginOuHome() {
        if (loginSolicitado) return;
        loginSolicitado = true;
        findViewById(R.id.imgArrow).setVisibility(android.view.View.GONE);
        findViewById(R.id.txtSetas).setVisibility(android.view.View.GONE);
        android.view.View skeleton = findViewById(R.id.skeletonSlider);
        skeleton.setVisibility(android.view.View.VISIBLE);
        animacaoSkeleton = ValueAnimator.ofFloat(0.3f, 0.8f);
        animacaoSkeleton.setDuration(700L);
        animacaoSkeleton.setRepeatMode(ValueAnimator.REVERSE);
        animacaoSkeleton.setRepeatCount(ValueAnimator.INFINITE);
        animacaoSkeleton.addUpdateListener(animator -> skeleton.setAlpha((float) animator.getAnimatedValue()));
        animacaoSkeleton.start();
        if (estadoSessao == EstadoSessao.VERIFICANDO) return;
        if (estadoSessao == EstadoSessao.PERFIL_ATIVO) agendarNavegacao(this::abrirHome, DURACAO_INDICADOR_LOGIN_MS);
        else agendarNavegacao(this::abrirLogin, DURACAO_INDICADOR_LOGIN_MS);
    }

    private void abrirHome() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    private void abrirLogin() {
        startActivity(new Intent(this, TelaLogin.class));
        finish();
    }

    private void mostrarBoasVindas() {
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        KeyboardInsets.apply(findViewById(R.id.main));

        MotionLayout motionLayoutButton = findViewById(R.id.btnComecar);
        motionLayoutButton.setTransitionListener(new MotionLayout.TransitionListener() {
            @Override
            public void onTransitionStarted(MotionLayout motionLayout, int startId, int endId) {
            }
            @Override
            public void onTransitionChange(MotionLayout motionLayout, int startId, int endId, float progress) {
            }
            public void onTransitionCompleted(MotionLayout motionLayout, int currentId) {
                if (currentId == R.id.end) {
                    solicitarLoginOuHome();
                }
            }
            @Override
            public void onTransitionTrigger(MotionLayout motionLayout, int triggerId, boolean positive, float progress) {
            }
        });
    }
    @Override
    protected void onStart() {

        MotionLayout motionLayoutButton = findViewById(R.id.btnComecar);
        super.onStart();

        if (motionLayoutButton != null) {
            motionLayoutButton.setProgress(0f);      // Reseta o progresso da animação para o início
            motionLayoutButton.jumpToState(R.id.start); // Força a transição de volta para o estado inicial imediatamente
        }
    }

    @Override
    protected void onDestroy() {
        if (animacaoSkeleton != null) animacaoSkeleton.cancel();
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}