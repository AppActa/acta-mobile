package br.com.acta;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.graphics.Typeface;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;

import java.util.Locale;

import br.com.acta.Api.MeApi;
import br.com.acta.Auth.FirebaseTokenProvider;
import br.com.acta.Client.RepositoryCallback;
import br.com.acta.Client.RetrofitClient;
import br.com.acta.Model.Me;
import br.com.acta.Services.MeService;

public class TelaLogin extends AppCompatActivity {
    private static final int MODO_LOGIN = 0;
    private static final int MODO_CRIAR_CONTA = 1;
    private static final int MODO_ATIVAR = 2;

    private FirebaseAuth firebaseAuth;
    private MeService service;
    private EditText email;
    private EditText senha;
    private EditText codigo;
    private TextView mensagem;
    private Button botao;
    private int modo = MODO_LOGIN;
    private boolean primeiroAcessoEscolhido;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_tela_login);
        KeyboardInsets.apply(findViewById(R.id.mainLogin));

        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseTokenProvider provider = new FirebaseTokenProvider(firebaseAuth);
        MeApi api = RetrofitClient.getInstance(provider).create(MeApi.class);

        service = new MeService(api);
        email = findViewById(R.id.edtEmail);
        senha = findViewById(R.id.edtSenha);
        codigo = findViewById(R.id.edtCodigoConvite);
        codigo.setFilters(new InputFilter[]{new InputFilter.AllCaps(), new InputFilter.LengthFilter(6)});
        mensagem = findViewById(R.id.txtMensagemErro);
        botao = findViewById(R.id.btnConcluido);

        findViewById(R.id.txtEsqueceuSenha).setOnClickListener(view -> mostrarDialogRecuperacao());
        findViewById(R.id.txtPrimeiroAcesso).setOnClickListener(view -> {
            if (modo == MODO_LOGIN) {
                primeiroAcessoEscolhido = true;
                mudarModo(MODO_CRIAR_CONTA);
            } else {
                primeiroAcessoEscolhido = false;
                mudarModo(MODO_LOGIN);
            }
        });
        botao.setOnClickListener(view -> enviar());

        FirebaseUser usuarioAtual = firebaseAuth.getCurrentUser();
        if (usuarioAtual != null) verificarCadastro(usuarioAtual);
    }

    private void enviar() {
        String txtEmail = email.getText().toString().trim();
        String txtSenha = senha.getText().toString();
        if (modo == MODO_ATIVAR) {
            if (!validarCodigo()) return;
            ativarConvite();
            return;
        }

        if (modo == MODO_CRIAR_CONTA && !validarCodigo()) return;
        if (txtEmail.isEmpty() || txtSenha.isEmpty()) {
            mostrarMensagem("Preencha e-mail e senha.");
            return;
        }

        if (modo == MODO_CRIAR_CONTA) criarConta(txtEmail, txtSenha);
        else {
            firebaseAuth.signInWithEmailAndPassword(txtEmail, txtSenha)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful() && firebaseAuth.getCurrentUser() != null) verificarCadastro(firebaseAuth.getCurrentUser());
                        else {
                            mostrarMensagem("Não foi possível entrar. Confira o e-mail e a senha.");
                        }
            });
        }
    }

    private boolean validarCodigo() {
        if (!codigo.getText().toString().trim().matches("(?i)[a-z0-9]{6}")) {
            codigo.setError("Informe o código de 6 caracteres");
            return false;
        }
        return true;
    }

    private void criarConta(String txtEmail, String txtSenha) {
        firebaseAuth.createUserWithEmailAndPassword(txtEmail, txtSenha)
                .addOnCompleteListener(this, task -> {
                    if (!task.isSuccessful() || firebaseAuth.getCurrentUser() == null) {
                        if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                            entrarNaContaExistente(txtEmail, txtSenha);
                            return;
                        }
                        mostrarMensagem("Não foi possível criar a conta. Se ela já existir, entre com seu e-mail e senha para ativar o convite.");
                        return;
                    }

            firebaseAuth.getCurrentUser().sendEmailVerification()
                    .addOnCompleteListener(this, verifyTask -> {
                        mudarModo(MODO_ATIVAR);
                        if (verifyTask.isSuccessful()) mostrarMensagem("Enviamos um e-mail de verificação. Verifique o endereço antes de ativar o convite.");
                        else {
                            mostrarMensagem("A conta foi criada, mas não foi possível enviar o e-mail de verificação (Firebase: "
                                    + codigoErroFirebase(verifyTask.getException()) + "). Tente novamente mais tarde.");
                        }
            });
        });
    }

    private void entrarNaContaExistente(String txtEmail, String txtSenha) {
        firebaseAuth.signInWithEmailAndPassword(txtEmail, txtSenha)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful() && firebaseAuth.getCurrentUser() != null) {
                        verificarCadastro(firebaseAuth.getCurrentUser());
                    } else {
                        mostrarMensagem("Esta conta já existe. Confira o e-mail e a senha para ativar o convite.");
                    }
                });
    }

    private void verificarCadastro(FirebaseUser usuario) {
        service.getMe(new RepositoryCallback<Me>() {
            @Override
            public void onSuccess(Me value) {
                abrirPerfil();
            }

            @Override
            public void onError(int statusCode, String error) {
                if (statusCode == 401 || statusCode == 403) {
                    mudarModo(MODO_ATIVAR);
                    mostrarMensagem("Sua conta ainda não está ativada. Informe o código recebido no convite.");
                } else mostrarMensagem("Não foi possível validar sua sessão. Verifique a conexão e tente novamente.");
            }
        });
    }

    private void ativarConvite() {
        String token = codigo.getText().toString().trim().toUpperCase(Locale.ROOT);
        FirebaseUser usuario = firebaseAuth.getCurrentUser();
        if (usuario == null) {
            mostrarMensagem("Entre ou crie a conta Firebase correspondente ao e-mail do convite.");
            mudarModo(MODO_LOGIN);
            return;
        }

        usuario.reload().addOnCompleteListener(this, reloadTask -> {
            FirebaseUser atualizado = firebaseAuth.getCurrentUser();
            if (!reloadTask.isSuccessful() || atualizado == null) {
                mostrarMensagem("Não foi possível atualizar a sessão. Tente entrar novamente.");
                return;
            }

            if (!atualizado.isEmailVerified()) {
                atualizado.sendEmailVerification().addOnCompleteListener(this, emailTask -> {
                    if (emailTask.isSuccessful()) mostrarMensagem("O e-mail ainda não está verificado. Enviamos uma nova mensagem de verificação.");
                    else {
                        mostrarMensagem("O e-mail ainda não está verificado. Não foi possível reenviar a verificação (Firebase: "
                                + codigoErroFirebase(emailTask.getException()) + "); tente novamente mais tarde.");
                    }
                });
                return;
            }

            atualizado.getIdToken(true).addOnCompleteListener(this, tokenTask -> {
                if (!tokenTask.isSuccessful()) {
                    mostrarMensagem("Não foi possível obter a sessão Firebase. Entre novamente e tente.");
                    return;
                }

                service.ativar(token, new RepositoryCallback<Me>() {
                    @Override
                    public void onSuccess(Me value) {
                        abrirPerfil();
                    }

                    @Override
                    public void onError(int statusCode, String error) {
                        if (statusCode == 401 || statusCode == 403) mostrarMensagem("Ativação não autorizada. Confira se o código está válido e se o e-mail da conta corresponde ao convite; se a conta já foi ativada, entre novamente.");
                        else if (statusCode == 0) mostrarMensagem("Falha de conexão ao ativar. Verifique a internet e tente novamente.");
                        else mostrarMensagem("Não foi possível ativar o convite (HTTP " + statusCode + "). Tente novamente.");
                    }
                });
            });
        });
    }

    private String codigoErroFirebase(Exception erro) {
        if (erro instanceof FirebaseAuthException) {
            return ((FirebaseAuthException) erro).getErrorCode();
        }
        return erro == null ? "desconhecido" : erro.getClass().getSimpleName();
    }

    private void mudarModo(int novoModo) {
        modo = novoModo;
        mensagem.setVisibility(View.GONE);
        codigo.setError(null);
        boolean primeiroAcesso = modo != MODO_LOGIN;
        codigo.setVisibility(primeiroAcesso ? View.VISIBLE : View.GONE);
        findViewById(R.id.lblCodigoConvite).setVisibility(primeiroAcesso ? View.VISIBLE : View.GONE);
        findViewById(R.id.txtCodigoAjuda).setVisibility(primeiroAcesso ? View.VISIBLE : View.GONE);
        findViewById(R.id.txtEsqueceuSenha).setVisibility(primeiroAcesso ? View.GONE : View.VISIBLE);

        TextView labelEmail = findViewById(R.id.lblEmail);
        String textoEmail = primeiroAcessoEscolhido ? "E-mail (use o mesmo do convite)" : "E-mail";
        SpannableString labelComDica = new SpannableString(textoEmail);
        int inicioParenteses = textoEmail.indexOf('(');
        if (inicioParenteses >= 0) {
            labelComDica.setSpan(new StyleSpan(Typeface.ITALIC), inicioParenteses,
                    textoEmail.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        labelEmail.setText(labelComDica);

        ((TextView) findViewById(R.id.txtTituloLogin)).setText(
                modo == MODO_LOGIN ? "Bem-vindo de volta" : "Ative seu convite");
        ((TextView) findViewById(R.id.txtSubtituloLogin)).setText(
                modo == MODO_LOGIN
                        ? "Entre para continuar sua jornada no ACTA."
                        : "Informe o código de seis caracteres enviado para o e-mail do convite.");
        botao.setText(modo == MODO_CRIAR_CONTA ? "Continuar" : modo == MODO_ATIVAR ? "Ativar convite" : "Entrar");
        ((TextView) findViewById(R.id.txtPrimeiroAcesso)).setText(
                modo == MODO_LOGIN ? "Primeiro acesso? Ativar convite" : "Já tem uma conta? Entrar");
    }

    private void abrirPerfil() {
        startActivity(new Intent(this, Perfil.class));
        finish();
    }

    private void mostrarMensagem(String texto) {
        mensagem.setText(texto);
        mensagem.setVisibility(View.VISIBLE);
    }

    private void mostrarDialogRecuperacao() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_esqueceu_senha, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }

        EditText emailRecuperacao = dialogView.findViewById(R.id.edtEmailRecuperacao);
        Button concluido = dialogView.findViewById(R.id.btnConcluidoRecuperacao);
        concluido.setOnClickListener(view -> {
            String endereco = emailRecuperacao.getText().toString().trim();
            if (endereco.isEmpty()) {
                emailRecuperacao.setError("Informe o e-mail");
                return;
            }

            firebaseAuth.sendPasswordResetEmail(endereco).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(this, "E-mail de redefinição enviado!", Toast.LENGTH_LONG).show();
                    dialog.dismiss();
                } else {
                    mostrarMensagem("Não foi possível enviar o e-mail de redefinição.");
                }
            });
        });
        dialog.show();
    }
}