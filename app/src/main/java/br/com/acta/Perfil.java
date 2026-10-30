package br.com.acta;

import static android.R.id.content;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import br.com.acta.Api.ColaboradorApi;
import br.com.acta.Api.MeApi;
import br.com.acta.Api.UsuarioApi;
import br.com.acta.Auth.FirebaseTokenProvider;
import br.com.acta.Auth.TokenProvider;
import br.com.acta.Client.RepositoryCallback;
import br.com.acta.Client.RetrofitClient;
import br.com.acta.Client.RetrofitClientAuth;
import br.com.acta.Client.RetrofitClientFcm;
import br.com.acta.DAO.AppDatabase;
import br.com.acta.Model.Colaborador;
import br.com.acta.Model.Me;
import br.com.acta.Model.Usuario;
import br.com.acta.Services.ColaboradorService;
import br.com.acta.Services.ActaMessagingService;
import br.com.acta.Services.DispositivoService;
import br.com.acta.Services.MeService;
import br.com.acta.Services.UsuarioService;
import br.com.acta.utils.FormatoData;
import br.com.acta.utils.SkeletonLoading;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Perfil extends AppCompatActivity {

    private boolean ajustandoNotificacoes;
    private boolean revogacaoNotificacoesPendente;
    private SharedPreferences preferences;
    TokenProvider tokenProvider = new FirebaseTokenProvider(FirebaseAuth.getInstance());
    private final MeApi meApi = RetrofitClientAuth.getInstance(tokenProvider).create(MeApi.class);
    private final ColaboradorApi colaboradorApi = RetrofitClient.getInstance(tokenProvider).create(ColaboradorApi.class);
    private final UsuarioApi usuarioApi = RetrofitClient.getInstance(tokenProvider).create(UsuarioApi.class);
    private final MeService meService = new MeService(meApi);
    private final ColaboradorService colaboradorService = new ColaboradorService(colaboradorApi);

    private final UsuarioService usuarioService = new UsuarioService(usuarioApi);
    private Long id = null;
    private TextView nome;
    private TextView email;
    private TextView cargo;
    private ImageView editarImg;
    private Uri fotoUri;
    ShapeableImageView imgFotoPerfil;
    private final ActivityResultLauncher<Uri> cameraLigar =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), tirou -> {
                if (tirou) {
                    imgFotoPerfil.post(() -> {
                        imgFotoPerfil.setImageURI(null);
                        imgFotoPerfil.setImageURI(fotoUri);
                    });

                    // Delay de 5 segundos antes de enviar ao Cloudinary,0
                    imgFotoPerfil.postDelayed(() -> {
                        salvarCloudinary(fotoUri);
                    }, 7000);
                }
            });
    private final ActivityResultLauncher<Intent> galeriaAbrir =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult o) {
                    if (o.getData() != null){
                        // obter a URI
                        fotoUri = o.getData().getData();
                        imgFotoPerfil.setImageURI(null);
                        imgFotoPerfil.setImageURI(fotoUri);

                        // Delay de 5 segundos antes de enviar ao Cloudinary
                        imgFotoPerfil.postDelayed(() -> {
                            salvarCloudinary(fotoUri);
                        }, 5000);
                    }
                }
            });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_perfil);
        KeyboardInsets.apply(findViewById(R.id.perfil), false);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        WindowInsetsControllerCompat controller = new WindowInsetsControllerCompat(getWindow(), getWindow().getDecorView());
        controller.setAppearanceLightStatusBars(false); // 'false' deixa o relógio e bateria brancos
        // O cabeçalho continua sob a barra de status; a utilidade aplica apenas o inset inferior no perfil.
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.btnVoltar), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            params.topMargin = systemBars.top + 16;
            v.setLayoutParams(params);
            return insets;
        });

        try {
            Map<String, String> config = new HashMap<>();
            config.put("cloud_name", BuildConfig.CLOUDINARY_CLOUD_NAME);
            config.put("api_key", BuildConfig.CLOUDINARY_API_KEY);
            config.put("api_secret", BuildConfig.CLOUDINARY_API_SECRET);

            MediaManager.init(this, config);
        } catch (IllegalStateException e) {
            // Evita crash caso inicialize duas vezes
        }


        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_selecionar_foto, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        nome = findViewById(R.id.txtNome);
        email = findViewById(R.id.txtEmail);
        cargo = findViewById(R.id.txtCargo);
        SwitchMaterial switchModoClaro = findViewById(R.id.switchModoClaro);
        preferences = getSharedPreferences("config_app", MODE_PRIVATE);
        Spinner formatoData = findViewById(R.id.selectFormatoData);
        FormatoData.Formato[] formatos = FormatoData.Formato.values();
        String[] opcoes = new String[formatos.length];
        for (int i = 0; i < formatos.length; i++) {
            opcoes[i] = formatos[i].getPadrao();
        }
        ArrayAdapter<String> adapterFormatos = new ArrayAdapter<>(this, R.layout.item_formato_data, opcoes);
        adapterFormatos.setDropDownViewResource(R.layout.item_formato_data);
        formatoData.setAdapter(adapterFormatos);
        formatoData.setSelection(FormatoData.obterFormato(this).ordinal());
        formatoData.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int posicao, long idItem) {
                FormatoData.salvarFormato(Perfil.this, formatos[posicao]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // a seleção atual continua salva
            }
        });
        editarImg = findViewById(R.id.btnAlterarFotoPerfil);
        imgFotoPerfil = findViewById(R.id.imgFotoPerfil);
        ImageView volta = findViewById(R.id.btnVoltar);
        carregarMe();
        LinearLayout sair = findViewById(R.id.btnSair);
        sair.setOnClickListener(view -> {
            DispositivoService.prepararLogout();
            sair.setEnabled(false);
            findViewById(R.id.switchNotificacoes).setEnabled(false);
            AtomicBoolean encerrado = new AtomicBoolean();
            Handler handler = new Handler(Looper.getMainLooper());

            Consumer<Boolean> concluir = ok -> {
                if (!encerrado.compareAndSet(false, true)) return;
                if (!ok) Toast.makeText(this, "Não foi possível revogar o dispositivo. A sessão será encerrada.", Toast.LENGTH_LONG).show();
                new Thread(() -> AppDatabase.getInstance(Perfil.this).clearAllTables()).start();
                FirebaseAuth.getInstance().signOut();

                Intent intent = new Intent(Perfil.this, TelaLogin.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                startActivity(intent);
                finish();
            };

            Call<Void> revogacao = RetrofitClientFcm.api().revogar();
            handler.postDelayed(() -> {
                revogacao.cancel();
                concluir.accept(false);
            }, 5000);

            revogacao.enqueue(new Callback<Void>() {
                public void onResponse(Call<Void> c, Response<Void> response) {concluir.accept(response.isSuccessful());}
                public void onFailure(Call<Void> c, Throwable t) {concluir.accept(false);}
            });
        });

        SwitchMaterial notificacoes = findViewById(R.id.switchNotificacoes);
        findViewById(R.id.btnConfigurarAvisos).setOnClickListener(view -> {
            Intent intent = new Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS);
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
            intent.putExtra(Settings.EXTRA_CHANNEL_ID, ActaMessagingService.CHANNEL);
            startActivity(intent);
        });
        notificacoes.setChecked(DispositivoService.habilitado(this) && DispositivoService.permissaoNotificacoesConcedida(this));
        notificacoes.setOnCheckedChangeListener((button, ativo) -> {
            if (ajustandoNotificacoes) return;
            sincronizarNotificacoes(ativo);
        });
        editarImg.setOnClickListener(v -> {
            // 1. Infla o layout SEMPRE dentro do clique para criar uma nova instância de view
            View viewDialog = LayoutInflater.from(Perfil.this).inflate(R.layout.dialog_selecionar_foto, null);

            AlertDialog dialog = new AlertDialog.Builder(Perfil.this)
                    .setView(viewDialog)
                    .create();

            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            }

            dialog.show();

            // 2. Busca os botões dentro da view recém-inflada do diálogo atual
            Button camera = viewDialog.findViewById(R.id.btnOpcaoCamera);
            Button galeria = viewDialog.findViewById(R.id.btnOpcaoGaleria);

            camera.setOnClickListener(view -> {
                tirarFoto();
                dialog.dismiss();
            });

            galeria.setOnClickListener(view -> {
                abrirGaleria();
                dialog.dismiss();
            });
        });


        boolean isModoClaro = preferences.getBoolean("is_modo_claro", true);
        switchModoClaro.setChecked(isModoClaro);
        switchModoClaro.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                salvarPreferenciaTema(true);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                salvarPreferenciaTema(false);
            }
        });
        volta.setOnClickListener(view -> {
            Intent intent = new Intent(this, HomeActivity.class);
            startActivity(intent);
        });

    }
    @Override
    protected void onResume() {
        super.onResume();
        if (DispositivoService.habilitado(this) && !DispositivoService.permissaoNotificacoesConcedida(this)) {
            atualizarSwitchNotificacoes(false);
            revogarNotificacoes();
        }
    }

    private void revogarNotificacoes() {
        if (revogacaoNotificacoesPendente) return;
        revogacaoNotificacoesPendente = true;
        DispositivoService.sincronizar(this, false, mensagem -> {
            revogacaoNotificacoesPendente = false;
            if (mensagem == null) {
                DispositivoService.salvar(this, false);
            } else if (!isFinishing() && !isDestroyed()) {
                Toast.makeText(this, mensagem, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void sincronizarNotificacoes(boolean ativo) {
        SwitchMaterial notificacoes = findViewById(R.id.switchNotificacoes);
        View sair = findViewById(R.id.btnSair);
        if (notificacoes == null || sair == null) return;

        notificacoes.setEnabled(false);
        sair.setEnabled(false);

        DispositivoService.sincronizar(this, ativo, mensagem -> {
            boolean ok = mensagem == null;
            if (ok) {
                DispositivoService.salvar(this, ativo);
                atualizarSwitchNotificacoes(ativo);
            }
            if (isFinishing() || isDestroyed()) return;

            notificacoes.setEnabled(true);
            sair.setEnabled(true);

            if (!ok) {
                atualizarSwitchNotificacoes(!ativo);
                Toast.makeText(this, mensagem, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void atualizarSwitchNotificacoes(boolean ativo) {
        SwitchMaterial notificacoes = findViewById(R.id.switchNotificacoes);
        if (notificacoes == null) return;

        ajustandoNotificacoes = true;
        notificacoes.setChecked(ativo);
        ajustandoNotificacoes = false;
    }

    private void salvarPreferenciaTema(boolean isModoClaro) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("is_modo_claro", isModoClaro);
        editor.apply();
    }
    public void carregarMe(){
        meService.getMe(SkeletonLoading.wrap(findViewById(content), new RepositoryCallback<Me>(){
            @Override
            public void onSuccess(Me me) {
                if (me == null || !me.temPerfilActa()) {
                    Toast.makeText(Perfil.this, "Seu perfil ACTA não está disponível. Entre novamente ou peça acesso ao administrador da empresa.", Toast.LENGTH_LONG).show();
                    finish();
                    return;
                }

                nome.setText(me.getNome());
                email.setText(me.getEmail());
                id = me.getIdUsuario();
                if (me.getIdColaborador() != null) carregarColaborador(me.getIdColaborador());
                buscarFotoDoBanco(id);
            }

            @Override
            public void onError(int code, String message) {
                Toast.makeText(Perfil.this, "Erro " + code + ": " + message, Toast.LENGTH_LONG).show();
            }
        }));
    }
    public void carregarColaborador(Long idUsuario){
        colaboradorService.getColaborador(idUsuario,SkeletonLoading.wrap(findViewById(content), new RepositoryCallback<Colaborador>(){
            @Override
            public void onSuccess(Colaborador colaborador) {
                cargo.setText(colaborador.getCargo());
            }

            @Override
            public void onError(int code, String message) {
                Toast.makeText(Perfil.this, "Erro " + code + ": " + message, Toast.LENGTH_LONG).show();
            }
        }));
    }
    private void salvarCloudinary(Uri fotoTirada){
        MediaManager.get()
                .upload(fotoTirada)
                .option("folder", "fotos")
                .unsigned("novoteste")
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) {

                    }

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {

                    }

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        //Obter a URL da imagem
                        String url = resultData.get("secure_url").toString();
                        Map<String, Object> camposAtualizados = new HashMap<>();
                        camposAtualizados.put("fotoUrl", url);
                        salvarUrlBanco(id,camposAtualizados);

                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {

                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {

                    }
                })
                .dispatch();
    }
    private void salvarUrlBanco(Long idUsuario,Map<String, Object>camposAtualizados) {
        usuarioService.patchUsuario(idUsuario, camposAtualizados, SkeletonLoading.wrap(findViewById(content), new RepositoryCallback<Usuario>() {
            @Override
            public void onSuccess(Usuario resposta) {
                Toast.makeText(Perfil.this, "Foto atualizada com sucesso!", Toast.LENGTH_SHORT).show();
                buscarFotoDoBanco(id);
            }

            @Override
            public void onError(int code, String message) {
                Toast.makeText(Perfil.this, "Erro: " + message, Toast.LENGTH_LONG).show();
            }
        }));
    }
    private void buscarFotoDoBanco(Long idUsuario){
        usuarioService.buscarUsuario(idUsuario, SkeletonLoading.wrap(findViewById(content), new RepositoryCallback<Usuario>() {
            @Override
            public void onSuccess(Usuario usuario) {
                imgFotoPerfil = findViewById(R.id.imgFotoPerfil);
                // Verifica se o usuário e a URL da foto não são nulos
                if (usuario != null && usuario.getFotoUrl() != null && !usuario.getFotoUrl().isEmpty()) {
                    Glide.with(Perfil.this)
                            .load(usuario.getFotoUrl())
                            .skipMemoryCache(true) // Ignora cache em memória
                            .diskCacheStrategy(DiskCacheStrategy.NONE) // Força baixar a imagem atualizada
                            .placeholder(R.drawable.reicon_profile_filled)
                            .error(R.drawable.reicon_profile_filled)
                            .into(imgFotoPerfil);

                } else {
                    // Caso o usuário não tenha foto cadastrada, exibe a imagem padrão
                    imgFotoPerfil.setImageResource(R.drawable.reicon_profile_filled);
                }
            }

            @Override
            public void onError(int statusCode, String message) {
                imgFotoPerfil.setImageResource(R.drawable.ic_arrow_forward);
                    Toast.makeText(Perfil.this, "Erro foto (" + statusCode + "): " + message, Toast.LENGTH_LONG).show();
            }
        }));
    }
    private void tirarFoto(){
        File arquivo = new File(getExternalFilesDir(null), "foto_"+System.currentTimeMillis()+".jpg");

        fotoUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", arquivo);

        cameraLigar.launch(fotoUri);
    }
    private void abrirGaleria(){
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galeriaAbrir.launch(intent);
    }

}