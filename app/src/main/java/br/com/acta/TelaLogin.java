package br.com.acta;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class TelaLogin extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_tela_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainLogin), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            Log.d("Auth", "Usuário já está logado, pular tela de login.");
            Intent intent = new Intent(this, MainActvity.class);
            startActivity(intent);
        }

        ((TextView) findViewById(R.id.txtEsqueceuSenha)).setOnClickListener(view -> {
            Intent intent = new Intent(this, MainActvity.class);
            startActivity(intent);
        });

        ((Button) findViewById(R.id.btnConcluido)).setOnClickListener(view -> {
            String txtEmail = ((TextView) findViewById(R.id.edtEmail)).getText().toString();
            String txtSenha = ((TextView) findViewById(R.id.edtSenha)).getText().toString();
            if (txtEmail.isEmpty() || txtSenha.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show();
            } else {
                autenticarUsuario(txtEmail, txtSenha);
            }
        });


    }

    private void autenticarUsuario(String txtEmail, String txtSenha) {
        FirebaseAuth autenticar = FirebaseAuth.getInstance();

        //fazer login

        autenticar.signInWithEmailAndPassword(txtEmail, txtSenha).addOnCompleteListener(task -> {
            TextView erro = findViewById(R.id.txtMensagemErro);
            if (task.isSuccessful()) {
                Intent intent = new Intent(this, MainActvity.class);
                startActivity(intent);
                Toast.makeText(this, "Logado com sucesso!", Toast.LENGTH_SHORT).show();
                erro.setVisibility(View.INVISIBLE);
            } else {
                erro.setVisibility(View.VISIBLE);
            }

        });
    }


    private void firebaseAuthWithGoogle(String idToken) {
        //Enviando o token para o firebase
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                        startActivity(new Intent(this, MainActvity.class));
                    }
                    else{
                        Toast.makeText(this, "Falha no login", Toast.LENGTH_SHORT).show();
                    }
                });

    }
    private final ActivityResultLauncher<Intent> signInLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        Task<GoogleSignInAccount> task =
                                GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                        try {
                            GoogleSignInAccount account = task.getResult(ApiException.class);
                            firebaseAuthWithGoogle(account.getIdToken());
                        } catch (ApiException e) {
                            Toast.makeText(this, "Falha no login: " + e.getStatusCode(), Toast.LENGTH_SHORT).show();
                        }

                    });


//    public void loginGoogle(){
//        //Abrindo a conexção com o FireBase
//        FirebaseAuth autenticar = FirebaseAuth.getInstance();
//
//        //Configurar Google Sign In, para receber o token do arquivo que é gerado automaticamente
//        GoogleSignInOptions gso = new GoogleSignInOptions
//                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//                .requestIdToken(getString(R.string.default_web_client_id))
//                .requestEmail()
//                .build();
//
//        GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
//
//        //Activity for result, nós não temos controle dela, esperamos somente o resultado
//        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
//        signInLauncher.launch(signInIntent);
//
//    }
}