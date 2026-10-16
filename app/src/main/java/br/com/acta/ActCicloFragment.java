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

import com.google.firebase.auth.FirebaseAuth;

import br.com.acta.Api.CicloApi;
import br.com.acta.Auth.FirebaseTokenProvider;
import br.com.acta.Auth.TokenProvider;
import br.com.acta.Client.RepositoryCallback;
import br.com.acta.Client.RetrofitClient;
import br.com.acta.Model.Ciclo;
import br.com.acta.Model.Enum.StatusCiclo;
import br.com.acta.Services.CicloService;
import br.com.acta.utils.SkeletonLoading;

public class ActCicloFragment extends Fragment {

    private final TokenProvider tokenProvider = new FirebaseTokenProvider(FirebaseAuth.getInstance());
    private final CicloApi cicloApi = RetrofitClient.getInstance(tokenProvider).create(CicloApi.class);
    private final CicloService cicloService = new CicloService(cicloApi);

    private TextView txtSemInformacoesAct;
    private TextView txtTituloAct;
    private TextView txtStatusAct;
    private TextView txtResponsavelAct;
    private TextView txtDataInicioAct;
    private TextView txtPrazoFinalAct;
    private View cardAprendizadosInclude;

    private Long idCicloAtual = 0L;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_act_ciclo, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageButton btnVoltar = view.findViewById(R.id.btnVoltarActCiclo);
        if (btnVoltar != null) {
            btnVoltar.setOnClickListener(v -> requireActivity().finish());
        }

        txtSemInformacoesAct = view.findViewById(R.id.txtSemInformacoesAct);
        txtTituloAct = view.findViewById(R.id.txtTituloActCiclo);
        txtStatusAct = view.findViewById(R.id.txtStatusActCiclo);
        txtResponsavelAct = view.findViewById(R.id.txtResponsavelAct);
        txtDataInicioAct = view.findViewById(R.id.txtDataInicioAct);
        txtPrazoFinalAct = view.findViewById(R.id.txtPrazoFinalAct);
        cardAprendizadosInclude = view.findViewById(R.id.cardAprendizadosCicloInclude);

        if (getArguments() != null && getArguments().containsKey("idCiclo")) {
            idCicloAtual = getArguments().getLong("idCiclo", 0L);
        } else if (requireActivity().getIntent() != null && requireActivity().getIntent().hasExtra("idCiclo")) {
            idCicloAtual = requireActivity().getIntent().getLongExtra("idCiclo", 0L);
        }

        if (idCicloAtual != 0L) {
            buscarCiclo(idCicloAtual);
        }

        if (cardAprendizadosInclude != null) {
            View btnVerAprendizados = cardAprendizadosInclude.findViewById(R.id.btnVerAprendizados);
            if (btnVerAprendizados != null) {
                btnVerAprendizados.setOnClickListener(v -> {
                    Bundle args = new Bundle();
                    if (idCicloAtual != 0L) {
                        args.putLong("idCiclo", idCicloAtual);
                    }
                    if (getView() != null) {
                        Navigation.findNavController(getView()).navigate(R.id.licoesAprendidasFragment, args);
                    }
                });
            }
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

                if (txtTituloAct != null && ciclo.getTitulo() != null) {
                    txtTituloAct.setText(ciclo.getTitulo());
                }

                if (txtStatusAct != null && ciclo.getStatus() != null) {
                    txtStatusAct.setText("Status: " + ciclo.getStatus().name());
                }

                if (txtDataInicioAct != null && ciclo.getDataInicio() != null) {
                    txtDataInicioAct.setText("Iniciado em: " + ciclo.getDataInicio());
                }

                if (txtPrazoFinalAct != null && ciclo.getDataEstimadaFim() != null) {
                    txtPrazoFinalAct.setText("Prazo final: " + ciclo.getDataEstimadaFim());
                }

                // Validação Acumulativa: FASE ACT (índice 3). Qualquer status com índice >= 3 pode visualizar o ACT.
                int statusIndex = obterIndiceStatus(ciclo.getStatus());
                boolean podeAcessarAct = statusIndex >= 3;

                if (podeAcessarAct) {
                    if (txtSemInformacoesAct != null) txtSemInformacoesAct.setVisibility(View.GONE);
                    if (cardAprendizadosInclude != null) cardAprendizadosInclude.setVisibility(View.VISIBLE);
                } else {
                    if (cardAprendizadosInclude != null) cardAprendizadosInclude.setVisibility(View.GONE);
                    if (txtSemInformacoesAct != null) txtSemInformacoesAct.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onError(int code, String message) {
            }
        }));
    }
}
