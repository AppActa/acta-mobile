package br.com.acta;

import android.content.Intent;
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

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

import br.com.acta.Api.CicloApi;
import br.com.acta.Api.PlanoAcaoApi;
import br.com.acta.Api.TarefaApi;
import br.com.acta.Api.UsuarioApi;
import br.com.acta.Api.UsuarioCicloApi;
import br.com.acta.Auth.FirebaseTokenProvider;
import br.com.acta.Auth.TokenProvider;
import br.com.acta.Client.RepositoryCallback;
import br.com.acta.Client.RetrofitClient;
import br.com.acta.Model.Ciclo;
import br.com.acta.Model.Enum.StatusCiclo;
import br.com.acta.Model.Enum.StatusTarefa;
import br.com.acta.Model.PlanoAcao;
import br.com.acta.Model.Tarefa;
import br.com.acta.Services.CicloService;
import br.com.acta.Services.PlanoAcaoService;
import br.com.acta.Services.TarefaService;
import br.com.acta.Services.UsuarioCicloService;
import br.com.acta.Services.UsuarioService;

public class GeralCicloFragment extends Fragment {
    private final TokenProvider tokenProvider = new FirebaseTokenProvider(FirebaseAuth.getInstance());
    private final UsuarioApi usuarioApi = RetrofitClient.getInstance(tokenProvider).create(UsuarioApi.class);
    private final UsuarioCicloApi usuarioCicloApi = RetrofitClient.getInstance(tokenProvider).create(UsuarioCicloApi.class);
    private final CicloApi cicloApi = RetrofitClient.getInstance(tokenProvider).create(CicloApi.class);
    private final PlanoAcaoApi planoAcaoApi = RetrofitClient.getInstance(tokenProvider).create(PlanoAcaoApi.class);
    private final TarefaApi tarefaApi = RetrofitClient.getInstance(tokenProvider).create(TarefaApi.class);
    private final UsuarioService usuarioService = new UsuarioService(usuarioApi);
    private final UsuarioCicloService usuarioCicloService = new UsuarioCicloService(usuarioCicloApi);
    private final CicloService cicloService = new CicloService(cicloApi);
    private final PlanoAcaoService planoAcaoService = new PlanoAcaoService(planoAcaoApi);
    private final TarefaService tarefaService = new TarefaService(tarefaApi);

    private TextView txtTituloCiclo;
    private TextView txtStatusCiclo;
    private TextView txtDataInicio;
    private TextView txtPrazoFinal;
    private Long idCicloAtual;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_geral_ciclo, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageButton btnVoltar = view.findViewById(R.id.btnVoltarGeralCiclo);
        if (btnVoltar != null) {
            btnVoltar.setOnClickListener(v -> requireActivity().finish());
        }

        txtTituloCiclo = view.findViewById(R.id.txtTituloGeralCiclo);
        txtStatusCiclo = view.findViewById(R.id.txtStatusGeralCiclo);
        txtDataInicio = view.findViewById(R.id.txtDataInicioGeralCiclo);
        txtPrazoFinal = view.findViewById(R.id.txtPrazoFinalGeralCiclo);

        if (requireActivity().getIntent() != null && requireActivity().getIntent().hasExtra("idCiclo")) {
            idCicloAtual = requireActivity().getIntent().getLongExtra("idCiclo", 0L);
            if (idCicloAtual != 0L) {
                buscarCiclo(idCicloAtual);
                buscarPlanosETarefas(idCicloAtual);
            }
        }
    }

    private void buscarCiclo(Long idCiclo) {
        cicloService.getCiclo(idCiclo, new RepositoryCallback<Ciclo>() {
            @Override
            public void onSuccess(Ciclo ciclo) {
                if (!isAdded() || ciclo == null) return;

                if (txtTituloCiclo != null && ciclo.getTitulo() != null) {
                    txtTituloCiclo.setText(ciclo.getTitulo());
                }

                if (txtStatusCiclo != null && ciclo.getStatus() != null) {
                    txtStatusCiclo.setText("Status: " + ciclo.getStatus().name());
                }

                if (txtDataInicio != null && ciclo.getDataInicio() != null) {
                    txtDataInicio.setText("Iniciado em: " + ciclo.getDataInicio());
                }

                if (txtPrazoFinal != null && ciclo.getDataEstimadaFim() != null) {
                    txtPrazoFinal.setText("Prazo final: " + ciclo.getDataEstimadaFim());
                }

                atualizarStatusPdca(ciclo.getStatus());
            }

            @Override
            public void onError(int code, String message) {
            }
        });
    }

    private void buscarPlanosETarefas(Long idCiclo) {
        planoAcaoService.buscarPorCiclo(idCiclo, new RepositoryCallback<List<PlanoAcao>>() {
            @Override
            public void onSuccess(List<PlanoAcao> planos) {
                if (!isAdded() || planos == null || planos.isEmpty()) {
                    calcularEExibirProgresso(new ArrayList<>());
                    return;
                }

                List<Tarefa> todasTarefas = new ArrayList<>();
                int[] contador = {0};

                for (PlanoAcao plano : planos) {
                    if (plano.getId() != null) {
                        tarefaService.buscarPorPlanoAcao(plano.getId(), new RepositoryCallback<List<Tarefa>>() {
                            @Override
                            public void onSuccess(List<Tarefa> tarefas) {
                                if (!isAdded()) return;
                                if (tarefas != null) {
                                    todasTarefas.addAll(tarefas);
                                }
                                contador[0]++;
                                if (contador[0] == planos.size()) {
                                    calcularEExibirProgresso(todasTarefas);
                                }
                            }

                            @Override
                            public void onError(int code, String message) {
                                contador[0]++;
                                if (contador[0] == planos.size()) {
                                    calcularEExibirProgresso(todasTarefas);
                                }
                            }
                        });
                    } else {
                        contador[0]++;
                        if (contador[0] == planos.size()) {
                            calcularEExibirProgresso(todasTarefas);
                        }
                    }
                }
            }

            @Override
            public void onError(int code, String message) {
            }
        });
    }

    private void calcularEExibirProgresso(List<Tarefa> tarefas) {
        View view = getView();
        if (view == null) return;

        View cardProgresso = view.findViewById(R.id.cardProgressoInclude);
        if (cardProgresso == null) return;

        TextView txtTotal = cardProgresso.findViewById(R.id.txtTotalTarefas);
        TextView txtPorcentagem = cardProgresso.findViewById(R.id.txtPorcentagemProgresso);
        ProgressBar progressBar = cardProgresso.findViewById(R.id.progressBarCiclo);
        View btnAbrir = cardProgresso.findViewById(R.id.btnAbrirPlanosAcao);

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

        if (txtTotal != null) {
            txtTotal.setText("Você tem " + totalValidas + " " + (totalValidas == 1 ? "tarefa" : "tarefas"));
        }
        if (txtPorcentagem != null) {
            txtPorcentagem.setText(porcentagem + "%");
        }
        if (progressBar != null) {
            progressBar.setProgress(porcentagem);
        }

        if (btnAbrir != null) {
            btnAbrir.setOnClickListener(v -> {
                Bundle args = new Bundle();
                if (idCicloAtual != null) {
                    args.putLong("idCiclo", idCicloAtual);
                }
                try {
                    Navigation.findNavController(v).navigate(R.id.doCicloFragment, args);
                } catch (Exception e) {
                    Intent intent = new Intent(v.getContext(), CicloPdcaActivity.class);
                    if (idCicloAtual != null) {
                        intent.putExtra("idCiclo", idCicloAtual);
                    }
                    v.getContext().startActivity(intent);
                }
            });
        }
    }

    private int obterIndiceStatus(StatusCiclo statusCiclo) {
        if (statusCiclo == null) return 0;
        String statusStr = statusCiclo.name().toUpperCase();
        switch (statusStr) {
            case "EXECUCAO":
            case "DO":
            case "DOING":
                return 1;
            case "VERIFICACAO":
            case "CHECK":
            case "ACOMPANHAR":
                return 2;
            case "PADRONIZACAO":
            case "ACT":
            case "CONCLUIDO":
            case "DONE":
                return 3;
            case "PLANEJAMENTO":
            case "PLAN":
            default:
                return 0;
        }
    }

    private void atualizarStatusPdca(StatusCiclo statusCiclo) {
        View view = getView();
        if (view == null) return;

        View statusCard = view.findViewById(R.id.cardStatusCicloInclude);
        if (statusCard == null) return;

        TextView txtPlan = statusCard.findViewById(R.id.txtStatusPlan);
        TextView txtDo = statusCard.findViewById(R.id.txtStatusDo);
        TextView txtCheck = statusCard.findViewById(R.id.txtStatusCheck);
        TextView txtAct = statusCard.findViewById(R.id.txtStatusAct);

        if (txtPlan == null || txtDo == null || txtCheck == null || txtAct == null) return;

        int cicloIndex = obterIndiceStatus(statusCiclo);

        configurarTextoFase(txtPlan, 0, cicloIndex);
        configurarTextoFase(txtDo, 1, cicloIndex);
        configurarTextoFase(txtCheck, 2, cicloIndex);
        configurarTextoFase(txtAct, 3, cicloIndex);
    }

    private void configurarTextoFase(TextView textView, int faseIndex, int cicloIndex) {
        if (faseIndex < cicloIndex) {
            textView.setText("Concluído");
        } else if (faseIndex == cicloIndex) {
            textView.setText("Em andamento");
        } else {
            textView.setText("Aguardando");
        }
    }
}
