package br.com.acta;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

import br.com.acta.Adapter.TarefaAdapter;
import br.com.acta.Api.TarefaApi;
import br.com.acta.Auth.FirebaseTokenProvider;
import br.com.acta.Auth.TokenProvider;
import br.com.acta.Client.RepositoryCallback;
import br.com.acta.Client.RetrofitClient;
import br.com.acta.DAO.AppDatabase;
import br.com.acta.Model.Tarefa;
import br.com.acta.Services.TarefaService;

public class MinhasTarefasFragment extends Fragment {

    private final TokenProvider tokenProvider = new FirebaseTokenProvider(FirebaseAuth.getInstance());
    private final TarefaApi tarefaApi = RetrofitClient.getInstance(tokenProvider).create(TarefaApi.class);
    private final TarefaService tarefaService = new TarefaService(tarefaApi);

    private RecyclerView rvTodasTarefas;
    private TarefaAdapter tarefaAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_minhas_tarefas, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageButton btnVoltar = view.findViewById(R.id.btnVoltarMinhasTarefas);
        if (btnVoltar != null) {
            btnVoltar.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());
        }

        rvTodasTarefas = view.findViewById(R.id.rvTodasTarefas);
        if (rvTodasTarefas != null) {
            rvTodasTarefas.setLayoutManager(new LinearLayoutManager(requireContext()));
        }

        EditText edtBuscarTarefa = view.findViewById(R.id.edtBuscarTarefa);
        if (edtBuscarTarefa != null) {
            edtBuscarTarefa.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (tarefaAdapter != null) {
                        tarefaAdapter.filtrar(s.toString());
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        // 1. Carregar do SQLite local
        try {
            List<Tarefa> tarefasLocais = AppDatabase.getInstance(requireContext()).tarefaDao().listarTodas();
            if (tarefasLocais != null && !tarefasLocais.isEmpty() && rvTodasTarefas != null) {
                tarefaAdapter = new TarefaAdapter(tarefasLocais);
                rvTodasTarefas.setAdapter(tarefaAdapter);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 2. Buscar da API em segundo plano
        carregarTarefasApi();
    }

    private void carregarTarefasApi() {
        tarefaService.buscarMinhas(new RepositoryCallback<List<Tarefa>>() {
            @Override
            public void onSuccess(List<Tarefa> tarefas) {
                if (!isAdded() || tarefas == null) return;

                try {
                    AppDatabase.getInstance(requireContext()).tarefaDao().salvarTodos(tarefas);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (rvTodasTarefas != null) {
                    tarefaAdapter = new TarefaAdapter(tarefas);
                    rvTodasTarefas.setAdapter(tarefaAdapter);
                }
            }

            @Override
            public void onError(int code, String message) {
            }
        });
    }
}
