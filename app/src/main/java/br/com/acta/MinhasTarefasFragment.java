package br.com.acta;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import java.util.Collections;
import java.util.List;
import br.com.acta.Adapter.TarefaAdapter;
import br.com.acta.Api.TarefaApi;
import br.com.acta.Auth.FirebaseTokenProvider;
import br.com.acta.Auth.TokenProvider;
import br.com.acta.Client.RepositoryCallback;
import br.com.acta.Client.RetrofitClient;
import br.com.acta.DAO.AppDatabase;
import br.com.acta.Model.Enum.StatusTarefa;
import br.com.acta.Model.Tarefa;
import br.com.acta.Services.TarefaService;
import br.com.acta.utils.SkeletonLoading;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MinhasTarefasFragment extends Fragment {
    private static final String TAG = "MinhasTarefasFragment";

    private final TokenProvider tokenProvider = new FirebaseTokenProvider(FirebaseAuth.getInstance());
    private final TarefaApi tarefaApi = RetrofitClient.getInstance(tokenProvider).create(TarefaApi.class);
    private final TarefaService tarefaService = new TarefaService(tarefaApi);

    private RecyclerView rvTodasTarefas;
    private TarefaAdapter tarefaAdapter;
    private String textoFiltro = "";
    private StatusTarefa statusFiltro;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_minhas_tarefas, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (savedInstanceState != null) {
            textoFiltro = savedInstanceState.getString("texto_tarefa", "");
            String statusSalvo = savedInstanceState.getString("status_tarefa");
            if (statusSalvo != null) {
                statusFiltro = StatusTarefa.valueOf(statusSalvo);
            }
        }

        TextView btnFiltrarStatus = view.findViewById(R.id.btnFiltrarStatusTarefas);
        atualizarTextoFiltro(btnFiltrarStatus);
        btnFiltrarStatus.setOnClickListener(v -> {
            PopupMenu menu = new PopupMenu(requireContext(), btnFiltrarStatus);
            menu.getMenu().add("Todos os status").setOnMenuItemClickListener(item -> {
                selecionarStatus(null, btnFiltrarStatus);
                return true;
            });
            for (StatusTarefa status : StatusTarefa.values()) {
                menu.getMenu().add(TarefaAdapter.nomeStatus(status)).setOnMenuItemClickListener(item -> {
                    selecionarStatus(status, btnFiltrarStatus);
                    return true;
                });
            }
            menu.show();
        });

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
            edtBuscarTarefa.setText(textoFiltro);
            edtBuscarTarefa.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    textoFiltro = s.toString();
                    if (tarefaAdapter != null) {
                        tarefaAdapter.filtrar(textoFiltro);
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        if (getArguments() != null && getArguments().containsKey("idTarefa")) {
            tarefaApi.buscarPorId(getArguments().getLong("idTarefa")).enqueue(new Callback<Tarefa>() {
                public void onResponse(Call<Tarefa> c, Response<Tarefa> response) {
                    if (getView() == null) return;
                    if (response.isSuccessful() && response.body() != null) {
                        tarefaAdapter = new TarefaAdapter(Collections.singletonList(response.body()));
                        aplicarFiltros();
                        rvTodasTarefas.setAdapter(tarefaAdapter);
                    } else Toast.makeText(requireContext(), "Tarefa indisponível ou sem acesso.", Toast.LENGTH_LONG).show();
                }
                public void onFailure(Call<Tarefa> c, Throwable t) {
                    if (getView() != null) Toast.makeText(requireContext(), "Não foi possível carregar a tarefa.", Toast.LENGTH_LONG).show();
                }
            });
            return;
        }

        // 1. Carregar do SQLite local
        try {
            List<Tarefa> tarefasLocais = AppDatabase.getInstance(requireContext()).tarefaDao().listarTodas();
            if (tarefasLocais != null && !tarefasLocais.isEmpty() && rvTodasTarefas != null) {
                tarefaAdapter = new TarefaAdapter(tarefasLocais);
                aplicarFiltros();
                rvTodasTarefas.setAdapter(tarefaAdapter);
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro ao carregar tarefas locais", e);
        }

        // 2. Buscar da API em segundo plano
        carregarTarefasApi();
    }

    private void carregarTarefasApi() {
        tarefaService.buscarMinhas(SkeletonLoading.wrap(getView(), new RepositoryCallback<List<Tarefa>>() {
            @Override
            public void onSuccess(List<Tarefa> tarefas) {
                if (!isAdded() || tarefas == null) return;

                try {
                    AppDatabase.getInstance(requireContext()).tarefaDao().salvarTodos(tarefas);
                } catch (Exception e) {
                    Log.e(TAG, "Erro ao salvar tarefas locais", e);
                }

                if (rvTodasTarefas != null) {
                    tarefaAdapter = new TarefaAdapter(tarefas);
                    aplicarFiltros();
                    rvTodasTarefas.setAdapter(tarefaAdapter);
                }
            }

            @Override
            public void onError(int code, String message) {
            }
        }));
    }

    private void selecionarStatus(StatusTarefa status, TextView botao) {
        statusFiltro = status;
        atualizarTextoFiltro(botao);
        aplicarFiltros();
    }

    private void atualizarTextoFiltro(TextView botao) {
        botao.setText(statusFiltro == null ? "Filtrar por status" : "Status: " + TarefaAdapter.nomeStatus(statusFiltro));
    }

    private void aplicarFiltros() {
        if (tarefaAdapter != null) {
            tarefaAdapter.filtrar(textoFiltro);
            tarefaAdapter.filtrarPorStatus(statusFiltro);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("texto_tarefa", textoFiltro);
        if (statusFiltro != null) {
            outState.putString("status_tarefa", statusFiltro.name());
        }
    }
}
