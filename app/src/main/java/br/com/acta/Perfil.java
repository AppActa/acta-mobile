package br.com.acta;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;

import br.com.acta.Api.ColaboradorApi;
import br.com.acta.Api.MeApi;
import br.com.acta.Auth.FirebaseTokenProvider;
import br.com.acta.Auth.TokenProvider;
import br.com.acta.Client.RepositoryCallback;
import br.com.acta.Client.RetrofitClient;
import br.com.acta.Model.Colaborador;
import br.com.acta.Model.Me;
import br.com.acta.Services.ColaboradorService;
import br.com.acta.Services.MeService;

public class Perfil extends AppCompatActivity {

    private SwitchMaterial switchModoClaro;
    private SharedPreferences preferences;
    private LinearLayout btnEditarPerfil;
    TokenProvider tokenProvider = new FirebaseTokenProvider(FirebaseAuth.getInstance());
    private MeApi meApi = RetrofitClient.getInstance(tokenProvider).create(MeApi.class);
    private ColaboradorApi colaboradorApi = RetrofitClient.getInstance(tokenProvider).create(ColaboradorApi.class);
    private MeService meService = new MeService(meApi);
    private ColaboradorService colaboradorService = new ColaboradorService(colaboradorApi);
    private Long id = null;
    private TextView nome;
    private TextView email;
    private TextView cargo;

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

        nome = findViewById(R.id.txtNome);
        email = findViewById(R.id.txtEmail);
        cargo = findViewById(R.id.txtCargo);
        switchModoClaro = findViewById(R.id.switchModoClaro);
        preferences = getSharedPreferences("config_app", MODE_PRIVATE);
        btnEditarPerfil = findViewById(R.id.btnEditarPerfil);
        carregarMe();
        btnEditarPerfil.setOnClickListener(v -> {
            if (id == null) {
                Toast.makeText(Perfil.this, "Aguarde o carregamento dos dados...", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(Perfil.this, EditarPerfil.class);
            Bundle bundle = new Bundle();
            bundle.putLong("USUARIO_ID", id);
            bundle.putString("USUARIO_NOME", nome.getText().toString());
            bundle.putString("USUARIO_EMAIL", email.getText().toString());
            intent.putExtras(bundle);
            startActivity(intent);
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
}