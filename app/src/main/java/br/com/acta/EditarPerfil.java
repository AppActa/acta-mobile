package br.com.acta;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.HashMap;
import java.util.Map;

import br.com.acta.Client.RepositoryCallback;
import br.com.acta.Model.Usuario;
import br.com.acta.Services.UsuarioService;

public class EditarPerfil extends AppCompatActivity {

    private Long idUsuario;
    private String nomeUsuario;
    private String emailUsuario;
    private UsuarioService usuarioService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_editar_perfil);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainEditarPerfil), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Bundle bundle = getIntent().getExtras();
        EditText textNome = findViewById(R.id.edtNomeEditar);
        EditText textEmail = findViewById(R.id.edtEmailEditar);
        if (bundle != null) {
            // Resgatando o Long
            idUsuario = bundle.getLong("USUARIO_ID", -1L);

            // Resgatando as Strings
            nomeUsuario = bundle.getString("USUARIO_NOME");
            emailUsuario = bundle.getString("USUARIO_EMAIL");
        }
        if (nomeUsuario != null) {
            textNome.setText(nomeUsuario);
        }
        if (emailUsuario != null) {
            textEmail.setText(emailUsuario);
        }
        Button btnSalvar = findViewById(R.id.btnConcluidoEditarPerfil);
        btnSalvar.setOnClickListener(v -> {
            Map<String, Object> camposAtualizados = new HashMap<>();
            camposAtualizados.put("nome", textNome.getText().toString());
            camposAtualizados.put("email", textEmail.getText().toString());

            usuarioService.patchUsuario(idUsuario, camposAtualizados, new RepositoryCallback<Usuario>() {
                @Override
                public void onSuccess(Usuario resposta) {
                    Toast.makeText(EditarPerfil.this, "Perfil atualizado com sucesso!", Toast.LENGTH_SHORT).show();
                    finish();
                }

                @Override
                public void onError(int code, String message) {
                    Toast.makeText(EditarPerfil.this, "Erro: " + message, Toast.LENGTH_LONG).show();
                }
            });
        });

    }
}