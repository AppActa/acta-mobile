package br.com.acta;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

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
import br.com.acta.Model.Enum.StatusTarefa;
import br.com.acta.Model.Tarefa;
import br.com.acta.Services.TarefaService;

public class TarefasPlanoAcaoFragment extends Fragment {

    private final TokenProvider tokenProvider = new FirebaseTokenProvider(FirebaseAuth.getInstance());
    private final TarefaApi tarefaApi = RetrofitClient.getInstance(tokenProvider).create(TarefaApi.class);
    private final TarefaService tarefaService = new TarefaService(tarefaApi);

    private RecyclerView rvTarefas;
    private TextView txtTotalTarefas;
    private TextView txtPorcentagem;
    private ProgressBar progressBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tarefas_plano_acao, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageButton btnVoltar = view.findViewById(R.id.btnVoltarTarefasPlano);
        if (btnVoltar != null) {
            btnVoltar.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());
        }

        rvTarefas = view.findViewById(R.id.rvTarefasPlanoAcao);
        if (rvTarefas != null) {
            rvTarefas.setLayoutManager(new LinearLayoutManager(requireContext()));
        }

        View cardProgresso = view.findViewById(R.id.cardProgressoTarefasInclude);
        if (cardProgresso != null) {
            txtTotalTarefas = cardProgresso.findViewById(R.id.txtTituloTarefasCiclo);
            txtPorcentagem = cardProgresso.findViewById(R.id.txtPorcentagemTarefasCiclo);
            progressBar = cardProgresso.findViewById(R.id.progressBarTarefasCiclo);
        }

        Long idPlanoAcao = 0L;
        if (getArguments() != null && getArguments().containsKey("idPlanoAcao")) {
            idPlanoAcao = getArguments().getLong("idPlanoAcao", 0L);
        } else if (requireActivity().getIntent() != null && requireActivity().getIntent().hasExtra("idPlanoAcao")) {
            idPlanoAcao = requireActivity().getIntent().getLongExtra("idPlanoAcao", 0L);
        }

        if (idPlanoAcao != 0L) {
            buscarTarefasDoPlano(idPlanoAcao);
        }
    }

    private void buscarTarefasDoPlano(Long idPlanoAcao) {
        tarefaService.buscarPorPlanoAcao(idPlanoAcao, new RepositoryCallback<List<Tarefa>>() {
            @Override
            public void onSuccess(List<Tarefa> tarefas) {
                if (!isAdded()) return;

                if (tarefas != null && !tarefas.isEmpty()) {
                    if (rvTarefas != null) {
                        rvTarefas.setAdapter(new TarefaAdapter(tarefas, false));
                    }
                    calcularEExibirProgresso(tarefas);
                }
            }

            @Override
            public void onError(int code, String message) {
            }
        });
    }

    private void calcularEExibirProgresso(List<Tarefa> tarefas) {
        int totalValidas = 0;
        int concluidas = 0;

        if (tarefas != null) {
            for (Tarefa t : tarefas) {
                if (t.getStatus() != StatusTarefa.CANCELADA) {
                    totalValidas++;
                    if (t.getStatus() == StatusTarefa.CONCLUIDA) {
                        concluidas++;
                    }
                }
            }
        }

        int porcentagem = 0;
        if (totalValidas > 0) {
            porcentagem = (concluidas * 100) / totalValidas;
        }

        if (txtTotalTarefas != null) {
            txtTotalTarefas.setText("Você tem " + totalValidas + " " + (totalValidas == 1 ? "tarefa" : "tarefas"));
        }
        if (txtPorcentagem != null) {
            txtPorcentagem.setText(porcentagem + "%");
        }
        if (progressBar != null) {
            progressBar.setProgress(porcentagem);
        }
    }
}
