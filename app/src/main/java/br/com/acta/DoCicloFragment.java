package br.com.acta;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

import br.com.acta.Adapter.PlanoAcaoAdapter;
import br.com.acta.Api.CicloApi;
import br.com.acta.Api.PlanoAcaoApi;
import br.com.acta.Auth.FirebaseTokenProvider;
import br.com.acta.Auth.TokenProvider;
import br.com.acta.Client.RepositoryCallback;
import br.com.acta.Client.RetrofitClient;
import br.com.acta.Model.Ciclo;
import br.com.acta.Model.Enum.StatusCiclo;
import br.com.acta.Model.PlanoAcao;
import br.com.acta.Services.CicloService;
import br.com.acta.Services.PlanoAcaoService;
import br.com.acta.utils.SkeletonLoading;
import br.com.acta.utils.FormatoData;

public class DoCicloFragment extends Fragment {
    private final TokenProvider tokenProvider = new FirebaseTokenProvider(FirebaseAuth.getInstance());
    private final CicloApi cicloApi = RetrofitClient.getInstance(tokenProvider).create(CicloApi.class);
    private final PlanoAcaoApi planoAcaoApi = RetrofitClient.getInstance(tokenProvider).create(PlanoAcaoApi.class);
    private final CicloService cicloService = new CicloService(cicloApi);
    private final PlanoAcaoService planoAcaoService = new PlanoAcaoService(planoAcaoApi);

    private RecyclerView rvPlanosAcaoDo;
    private TextView txtSemInformacoesDo;
    private TextView txtTituloDo;
    private TextView txtStatusDo;
    private TextView txtDataInicio;
    private TextView txtPrazoFinal;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_do_ciclo, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageButton btnVoltar = view.findViewById(R.id.btnVoltarDoCiclo);
        if (btnVoltar != null) {
            btnVoltar.setOnClickListener(v -> requireActivity().finish());
        }

        rvPlanosAcaoDo = view.findViewById(R.id.rvPlanosAcaoDo);
        if (rvPlanosAcaoDo != null) {
            rvPlanosAcaoDo.setLayoutManager(new LinearLayoutManager(requireContext()));
        }

        txtSemInformacoesDo = view.findViewById(R.id.txtSemInformacoesDo);
        txtTituloDo = view.findViewById(R.id.txtTituloDoCiclo);
        txtStatusDo = view.findViewById(R.id.txtStatusDoCiclo);
        txtDataInicio = view.findViewById(R.id.txtDataInicioDo);
        txtPrazoFinal = view.findViewById(R.id.txtPrazoFinalDo);

        Long idCiclo = 0L;
        if (getArguments() != null && getArguments().containsKey("idCiclo")) {
            idCiclo = getArguments().getLong("idCiclo", 0L);
        } else if (requireActivity().getIntent() != null && requireActivity().getIntent().hasExtra("idCiclo")) {
            idCiclo = requireActivity().getIntent().getLongExtra("idCiclo", 0L);
        }

        if (idCiclo != 0L) {
            buscarCiclo(idCiclo);
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

    private void buscarCiclo(Long idCiclo) {
        cicloService.getCiclo(idCiclo, SkeletonLoading.wrap(getView(), new RepositoryCallback<Ciclo>() {
            @Override
            public void onSuccess(Ciclo ciclo) {
                if (!isAdded() || ciclo == null) return;

                if (txtTituloDo != null && ciclo.getTitulo() != null) {
                    txtTituloDo.setText(ciclo.getTitulo());
                }

                if (txtStatusDo != null && ciclo.getStatus() != null) {
                    txtStatusDo.setText("Status: " + ciclo.getStatus().name());
                }

                if (txtDataInicio != null && ciclo.getDataInicio() != null) {
                    txtDataInicio.setText("Iniciado em: " + FormatoData.formatar(requireContext(), ciclo.getDataInicio()));
                }

                if (txtPrazoFinal != null && ciclo.getDataEstimadaFim() != null) {
                    txtPrazoFinal.setText("Prazo final: " + FormatoData.formatar(requireContext(), ciclo.getDataEstimadaFim()));
                }

                // Validação Acumulativa: FASE DO (índice 1). Qualquer status com índice >= 1 pode visualizar o DO.
                int statusIndex = obterIndiceStatus(ciclo.getStatus());
                boolean podeAcessarDo = statusIndex >= 1;

                if (podeAcessarDo) {
                    if (txtSemInformacoesDo != null) txtSemInformacoesDo.setVisibility(View.GONE);
                    if (rvPlanosAcaoDo != null) rvPlanosAcaoDo.setVisibility(View.VISIBLE);
                    buscarPlanoAcao(idCiclo);
                } else {
                    if (rvPlanosAcaoDo != null) rvPlanosAcaoDo.setVisibility(View.GONE);
                    if (txtSemInformacoesDo != null) txtSemInformacoesDo.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onError(int code, String message) {
            }
        }));
    }

    private void buscarPlanoAcao(Long idCiclo) {
        planoAcaoService.buscarPorCiclo(idCiclo, SkeletonLoading.wrap(getView(), new RepositoryCallback<List<PlanoAcao>>() {
            @Override
            public void onSuccess(List<PlanoAcao> planoAcaoList) {
                if (!isAdded()) return;

                if (planoAcaoList != null && !planoAcaoList.isEmpty()) {
                    if (txtSemInformacoesDo != null) txtSemInformacoesDo.setVisibility(View.GONE);
                    if (rvPlanosAcaoDo != null) {
                        rvPlanosAcaoDo.setVisibility(View.VISIBLE);
                        PlanoAcaoAdapter adapter = new PlanoAcaoAdapter(planoAcaoList, plano -> {
                            Bundle args = new Bundle();
                            if (plano.getId() != null) {
                                args.putLong("idPlanoAcao", plano.getId());
                            }
                            if (getView() != null) {
                                Navigation.findNavController(getView()).navigate(R.id.tarefasPlanoAcaoFragment, args);
                            }
                        });
                        rvPlanosAcaoDo.setAdapter(adapter);
                    }
                } else {
                    if (rvPlanosAcaoDo != null) rvPlanosAcaoDo.setVisibility(View.GONE);
                    if (txtSemInformacoesDo != null) txtSemInformacoesDo.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onError(int code, String message) {
                if (!isAdded()) return;
                if (rvPlanosAcaoDo != null) rvPlanosAcaoDo.setVisibility(View.GONE);
                if (txtSemInformacoesDo != null) txtSemInformacoesDo.setVisibility(View.VISIBLE);
            }
        }));
    }
}