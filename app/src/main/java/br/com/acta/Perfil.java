package br.com.acta;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

import br.com.acta.Api.ColaboradorApi;
import br.com.acta.Api.MeApi;
import br.com.acta.Api.UsuarioApi;
import br.com.acta.Auth.FirebaseTokenProvider;
import br.com.acta.Auth.TokenProvider;
import br.com.acta.Client.RepositoryCallback;
import br.com.acta.Client.RetrofitClient;
import br.com.acta.Model.Colaborador;
import br.com.acta.Model.Me;
import br.com.acta.Model.Usuario;
import br.com.acta.Services.ColaboradorService;
import br.com.acta.Services.MeService;
import br.com.acta.Services.UsuarioService;

public class Perfil extends AppCompatActivity {

    private SwitchMaterial switchModoClaro;
    private SharedPreferences preferences;
    private LinearLayout btnEditarPerfil;
    TokenProvider tokenProvider = new FirebaseTokenProvider(FirebaseAuth.getInstance());
    private MeApi meApi = RetrofitClient.getInstance(tokenProvider).create(MeApi.class);
    private ColaboradorApi colaboradorApi = RetrofitClient.getInstance(tokenProvider).create(ColaboradorApi.class);
    private UsuarioApi usuarioApi = RetrofitClient.getInstance(tokenProvider).create(UsuarioApi.class);
    private MeService meService = new MeService(meApi);
    private ColaboradorService colaboradorService = new ColaboradorService(colaboradorApi);

    private UsuarioService usuarioService = new UsuarioService(usuarioApi);
    private Long id = null;
    private TextView nome;
    private TextView email;
    private TextView cargo;
    private ImageView editarImg;
    private Uri fotoUri;
    ShapeableImageView imgFotoPerfil;
    private ActivityResultLauncher<Uri> cameraLigar =
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
    private ActivityResultLauncher<Intent> galeriaAbrir =
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
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.perfil), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
            androidx.core.view.WindowInsetsControllerCompat controller =
                    new androidx.core.view.WindowInsetsControllerCompat(getWindow(), getWindow().getDecorView());
            controller.setAppearanceLightStatusBars(false); // 'false' deixa o relógio e bateria brancos
        }
        // 2. Zera o padding superior do ScrollView para o azul do topo ir até a borda da tela
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.perfil), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            // top = 0 permite que o cabeçalho azul ocupe a barra de status
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom);
            return insets;
        });
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.btnVoltar), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            android.view.ViewGroup.MarginLayoutParams params =
                    (android.view.ViewGroup.MarginLayoutParams) v.getLayoutParams();
            params.topMargin = systemBars.top + 16;
            v.setLayoutParams(params);
            return insets;
        });

        try {
            java.util.Map<String, String> config = new java.util.HashMap<>();
            config.put("cloud_name", br.com.acta.BuildConfig.CLOUDINARY_CLOUD_NAME);
            config.put("api_key", br.com.acta.BuildConfig.CLOUDINARY_API_KEY);
            config.put("api_secret", br.com.acta.BuildConfig.CLOUDINARY_API_SECRET);

            com.cloudinary.android.MediaManager.init(this, config);
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
        switchModoClaro = findViewById(R.id.switchModoClaro);
        preferences = getSharedPreferences("config_app", MODE_PRIVATE);
        editarImg = findViewById(R.id.btnAlterarFotoPerfil);
        imgFotoPerfil = findViewById(R.id.imgFotoPerfil);
        ImageView volta = findViewById(R.id.btnVoltar);
        carregarMe();
        LinearLayout sair = findViewById(R.id.btnSair);
        sair.setOnClickListener(view -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(Perfil.this, TelaLogin.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
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
    private void salvarPreferenciaTema(boolean isModoClaro) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("is_modo_claro", isModoClaro);
        editor.apply();
    }
    public void carregarMe(){
        meService.getMe(new RepositoryCallback<Me>(){
            @Override
            public void onSuccess(Me me) {
                nome.setText(me.getNome());
                email.setText(me.getEmail());
                id = me.getIdUsuario();
                carregarColaborador(me.getIdColaborador());
                buscarFotoDoBanco(id);
            }

            @Override
            public void onError(int code, String message) {
                Toast.makeText(Perfil.this, "Erro " + code + ": " + message, Toast.LENGTH_LONG).show();
            }
        });
    }
    public void carregarColaborador(Long idUsuario){
        colaboradorService.getColaborador(idUsuario,new RepositoryCallback<Colaborador>(){
            @Override
            public void onSuccess(Colaborador colaborador) {
                cargo.setText(colaborador.getCargo());
            }

            @Override
            public void onError(int code, String message) {
                Toast.makeText(Perfil.this, "Erro " + code + ": " + message, Toast.LENGTH_LONG).show();
            }
        });
    }
    private void salvarCloudinary(Uri fotoTirada){
        MediaManager.get()
                .upload(fotoTirada)
                .option("folder", "fotos")
                .unsigned("novoteste")
//                .preprocess(new ImagePreprocessChain()
//                                .loadWith(new BitmapDecoder(1000, 1000))
//                                .addStep(new Limit(1000, 1000))
//                                .addStep(new DimensionsValidator(10, 10, 1000, 1000))
//                        .addStep(new Rotate(90))
//                                .addStep(new AutoRotation(getApplicationContext(), fotoTirada))
//                                .saveWith(new BitmapEncoder(BitmapEncoder.Format.WEBP, 80))
//                )
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
        usuarioService.patchUsuario(idUsuario, camposAtualizados, new RepositoryCallback<Usuario>() {
            @Override
            public void onSuccess(Usuario resposta) {
                Toast.makeText(Perfil.this, "Foto atualizada com sucesso!", Toast.LENGTH_SHORT).show();
                buscarFotoDoBanco(id);
            }

            @Override
            public void onError(int code, String message) {
                Toast.makeText(Perfil.this, "Erro: " + message, Toast.LENGTH_LONG).show();
            }
        });
    }
    private void buscarFotoDoBanco(Long idUsuario){
        usuarioService.buscarUsuario(idUsuario, new RepositoryCallback<Usuario>() {
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
        });
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