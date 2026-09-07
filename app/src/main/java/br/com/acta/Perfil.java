package br.com.acta;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;

import br.com.acta.Api.MeApi;
import br.com.acta.Auth.FirebaseTokenProvider;
import br.com.acta.Auth.TokenProvider;
import br.com.acta.Client.RepositoryCallback;
import br.com.acta.Client.RetrofitClient;
import br.com.acta.Model.Me;
import br.com.acta.Services.MeService;

public class Perfil extends AppCompatActivity {

    private SwitchMaterial switchModoClaro;
    private SharedPreferences preferences;
    private LinearLayout btnEditarPerfil;
    TokenProvider tokenProvider = new FirebaseTokenProvider(FirebaseAuth.getInstance());
    private MeApi meApi = RetrofitClient.getInstance(tokenProvider).create(MeApi.class);
    private MeService meService = new MeService(meApi);
    Long id;
    TextView nome;
    TextView email;

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
        carregarMe();

        switchModoClaro = findViewById(R.id.switchModoClaro);
        preferences = getSharedPreferences("config_app", MODE_PRIVATE);
        btnEditarPerfil = findViewById(R.id.btnEditarPerfil);
        Bundle bundle = new Bundle();
        btnEditarPerfil.setOnClickListener(v -> {
            Intent intent = new Intent(Perfil.this, EditarPerfil.class);
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
                 nome = findViewById(R.id.txtNome);
                 email = findViewById(R.id.txtEmail);
                nome.setText(me.getNome());
                email.setText(me.getEmail());
                id = me.getIdUsuario();
            }

            @Override
            public void onError(int code, String message) {

            }
        });
    }
}