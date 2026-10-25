package br.com.acta;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import br.com.acta.Adapter.ListaConversasAdapter;
import br.com.acta.Model.ChatItem;
import br.com.acta.R;

public class ChatActivity extends AppCompatActivity implements ChatFragment.Host {
    private DrawerLayout drawer;
    private ListaConversasAdapter listaConversasAdapter;

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
        ChatFragment fragment = (ChatFragment) getSupportFragmentManager().findFragmentById(R.id.chatFragmentContainer);
        EditText chatInput = findViewById(R.id.chatInput);
        findViewById(R.id.buttonChatBack).setOnClickListener(v -> finish());
        findViewById(R.id.buttonChatHistory).setOnClickListener(v -> {
            if (fragment != null) fragment.carregarHistorico();
            drawer.openDrawer(GravityCompat.END);
        });
        findViewById(R.id.buttonCloseHistory).setOnClickListener(v -> drawer.closeDrawer(GravityCompat.END));
        findViewById(R.id.buttonNewConversation).setOnClickListener(v -> {
            drawer.closeDrawer(GravityCompat.END);
            if (fragment != null) fragment.iniciarNovaConversa();
        });
        findViewById(R.id.chatProfileAction).setOnClickListener(v -> {
            drawer.closeDrawer(GravityCompat.END);
            startActivity(new Intent(ChatActivity.this, Perfil.class));
        });

        findViewById(R.id.buttonChatSend).setOnClickListener(v -> enviar(chatInput, fragment));
        chatInput.setOnEditorActionListener((textView, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                enviar(chatInput, fragment);
                return true;
            }
            return false;
        });

        RecyclerView historyList = findViewById(R.id.historyList);
        historyList.setLayoutManager(new LinearLayoutManager(this));
        listaConversasAdapter = new ListaConversasAdapter(conversa -> {
            drawer.closeDrawer(GravityCompat.END);
            if (fragment != null) fragment.abrirConversa(conversa.getSessionId());
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

    private void enviar(EditText input, ChatFragment fragment) {
        String texto = input.getText().toString().trim();
        if (texto.isEmpty() || fragment == null) return;
        input.setText("");
        fragment.enviarMensagem(texto);
        InputMethodManager teclado = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (teclado != null) teclado.hideSoftInputFromWindow(input.getWindowToken(), 0);
        input.clearFocus();
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
