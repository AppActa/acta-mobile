package br.com.acta;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.IOException;
import java.util.List;

import br.com.acta.Adapter.ListaConversasAdapter;
import br.com.acta.Model.ChatItem;
import br.com.acta.R;
import br.com.acta.Services.AudioRecorder;

public class ChatActivity extends AppCompatActivity implements ChatFragment.Host {
    private DrawerLayout drawer;
    private ListaConversasAdapter listaConversasAdapter;
    private final AudioRecorder audioRecorder = new AudioRecorder();
    private ChatFragment chatFragment;
    private View chatInputContainer;
    private final ActivityResultLauncher<String> permissaoMicrofone = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), concedida -> {
                if (concedida) iniciarGravacao();
                else Toast.makeText(this, "Permita o acesso ao microfone para enviar áudio.", Toast.LENGTH_LONG).show();
            });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chat);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.chatContent), (content, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets ime = insets.getInsets(WindowInsetsCompat.Type.ime());
            content.setPadding(bars.left, 0, bars.right, Math.max(bars.bottom, ime.bottom));
            View header = findViewById(R.id.chatHeader);
            ViewGroup.LayoutParams params = header.getLayoutParams();
            float density = getResources().getDisplayMetrics().density;
            int altura = Math.round(61 * density)
                    + Math.max(Math.round(37 * density), bars.top + Math.round(8 * density));
            if (params.height != altura) {
                params.height = altura;
                header.setLayoutParams(params);
            }
            return insets;
        });

        drawer = findViewById(R.id.chatDrawer);
        chatFragment = (ChatFragment) getSupportFragmentManager().findFragmentById(R.id.chatFragmentContainer);
        EditText chatInput = findViewById(R.id.chatInput);
        chatInputContainer = findViewById(R.id.chatInputContainer);
        findViewById(R.id.buttonChatBack).setOnClickListener(v -> finish());
        findViewById(R.id.buttonChatHistory).setOnClickListener(v -> {
            if (chatFragment != null) chatFragment.carregarHistorico();
            drawer.openDrawer(GravityCompat.END);
        });
        findViewById(R.id.buttonCloseHistory).setOnClickListener(v -> drawer.closeDrawer(GravityCompat.END));
        findViewById(R.id.buttonNewConversation).setOnClickListener(v -> {
            drawer.closeDrawer(GravityCompat.END);
            if (chatFragment != null) chatFragment.iniciarNovaConversa();
        });
        findViewById(R.id.chatProfileAction).setOnClickListener(v -> {
            drawer.closeDrawer(GravityCompat.END);
            startActivity(new Intent(ChatActivity.this, Perfil.class));
        });

        findViewById(R.id.buttonChatSend).setOnClickListener(v -> enviar(chatInput));
        findViewById(R.id.buttonChatAudio).setOnClickListener(v -> alternarGravacao());
        chatInput.setOnEditorActionListener((textView, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                enviar(chatInput);
                return true;
            }
            return false;
        });

        RecyclerView historyList = findViewById(R.id.historyList);
        historyList.setLayoutManager(new LinearLayoutManager(this));
        listaConversasAdapter = new ListaConversasAdapter(conversa -> {
            drawer.closeDrawer(GravityCompat.END);
            if (chatFragment != null) chatFragment.abrirConversa(conversa.getSessionId());
        });
        historyList.setAdapter(listaConversasAdapter);
        EditText historySearch = findViewById(R.id.historySearch);
        historySearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                listaConversasAdapter.filtrar(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void enviar(EditText input) {
        String texto = input.getText().toString().trim();
        if (texto.isEmpty() || chatFragment == null) return;
        input.setText("");
        chatFragment.enviarMensagem(texto);
        InputMethodManager teclado = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (teclado != null) teclado.hideSoftInputFromWindow(input.getWindowToken(), 0);
        input.clearFocus();
    }

    private void alternarGravacao() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED) {
            iniciarGravacao();
        } else {
            permissaoMicrofone.launch(Manifest.permission.RECORD_AUDIO);
        }
    }

    private void iniciarGravacao() {
        try {
            audioRecorder.iniciar(this, getCacheDir(), this::finalizarGravacao);
            atualizarCardGravacao(true);
            EditText chatInput = findViewById(R.id.chatInput);
            if (chatInput != null) {
                InputMethodManager teclado = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (teclado != null) teclado.hideSoftInputFromWindow(chatInput.getWindowToken(), 0);
            }
        } catch (IOException erro) {
            audioRecorder.cancelar();
            Toast.makeText(this, "Não foi possível iniciar a gravação de áudio.", Toast.LENGTH_LONG).show();
        }
    }

    private void finalizarGravacao() {
        if (!audioRecorder.estaGravando()) return;
        File arquivo = audioRecorder.parar();
        atualizarCardGravacao(false);
        if (arquivo == null) {
            Toast.makeText(this, "A gravação ficou vazia. Tente novamente.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (chatFragment != null) {
            chatFragment.enviarAudio(arquivo);
            Toast.makeText(this, "Enviando áudio…", Toast.LENGTH_SHORT).show();
        } else {
            arquivo.delete();
        }
    }

    private void atualizarCardGravacao(boolean visivel) {
        if (chatInputContainer != null) {
            chatInputContainer.setVisibility(visivel ? View.GONE : View.VISIBLE);
        }
        if (chatFragment != null) chatFragment.exibirCardGravacao(visivel);
    }

    private void cancelarGravacao() {
        audioRecorder.cancelar();
        atualizarCardGravacao(false);
    }

    @Override
    public void onAudioCancelRequested() {
        cancelarGravacao();
    }

    @Override
    public void onAudioFinishRequested() {
        finalizarGravacao();
    }

    @Override
    protected void onDestroy() {
        audioRecorder.cancelar();
        super.onDestroy();
    }

    @Override
    public void onHistoricoAtualizado(List<ChatItem> conversas) {
        if (listaConversasAdapter != null) listaConversasAdapter.setConversas(conversas);
    }

    @Override
    public void onBackPressed() {
        if (drawer != null && drawer.isDrawerOpen(GravityCompat.END)) drawer.closeDrawer(GravityCompat.END);
        else super.onBackPressed();
    }
}
