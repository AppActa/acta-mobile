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
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.cloudinary.android.preprocess.BitmapDecoder;
import com.cloudinary.android.preprocess.BitmapEncoder;
import com.cloudinary.android.preprocess.DimensionsValidator;
import com.cloudinary.android.preprocess.ImagePreprocessChain;
import com.cloudinary.android.preprocess.Limit;
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
import br.com.acta.AutoRotation.AutoRotation;
import br.com.acta.Client.RepositoryCallback;
import br.com.acta.Client.RetrofitClient;
import br.com.acta.Model.Colaborador;
import br.com.acta.Model.Me;
import br.com.acta.Model.Usuario;
import br.com.acta.Services.ColaboradorService;
import br.com.acta.Services.MeService;
import br.com.acta.Services.UsuarioService;
import retrofit2.Callback;

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
    private ImageView fotoPerfil;
    ShapeableImageView imgFotoPerfil;
    private ActivityResultLauncher<Uri> cameraLigar =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), tirou -> {
                if (tirou) {
                    imgFotoPerfil.post(() -> {
                        imgFotoPerfil.setImageURI(null);
                        imgFotoPerfil.setImageURI(fotoUri);
                        salvarCloudinary(fotoUri);
                    });
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
                        salvarCloudinary(fotoUri);
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
        carregarMe();
        editarImg.setOnClickListener(v -> {
            AlertDialog dialog = builder.create();
            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            }
            dialog.show();
            Button camera = dialogView.findViewById(R.id.btnOpcaoCamera);
            Button galeria = dialogView.findViewById(R.id.btnOpcaoGaleria);
            camera.setOnClickListener(view -> {
                tirarFoto();
                dialog.dismiss();
            });
            galeria.setOnClickListener(view->{
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
                .unsigned("ml_default")
                .preprocess(new ImagePreprocessChain()
                                .loadWith(new BitmapDecoder(1000, 1000))
                                .addStep(new Limit(1000, 1000))
                                .addStep(new DimensionsValidator(10, 10, 1000, 1000))
//                        .addStep(new Rotate(90))
                                .addStep(new AutoRotation(getApplicationContext(), fotoTirada))
                                .saveWith(new BitmapEncoder(BitmapEncoder.Format.WEBP, 80))
                )
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
                        String url = resultData.get("url").toString();
                        Map<String, Object> camposAtualizados = new HashMap<>();
                        camposAtualizados.put("url_foto", url);
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
                finish();
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

                    // Carrega a imagem da URL remota (Cloudinary) no ImageView usando o Glide
                    Glide.with(Perfil.this)
                            .load(usuario.getFotoUrl())
                            .placeholder(R.drawable.reicon_profile_filled) // foto temporária durante o download
                            .error(R.drawable.reicon_profile_filled)       // foto em caso de falha no carregamento
                            .into(imgFotoPerfil);

                } else {
                    // Caso o usuário não tenha foto cadastrada, exibe a imagem padrão
                    imgFotoPerfil.setImageResource(R.drawable.reicon_profile_filled);
                }
            }

            @Override
            public void onError(int statusCode, String message) {

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