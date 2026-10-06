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

import com.google.firebase.auth.FirebaseAuth;

import br.com.acta.Api.CicloApi;
import br.com.acta.Api.UsuarioApi;
import br.com.acta.Api.UsuarioCicloApi;
import br.com.acta.Auth.FirebaseTokenProvider;
import br.com.acta.Auth.TokenProvider;
import br.com.acta.Client.RepositoryCallback;
import br.com.acta.Client.RetrofitClient;
import br.com.acta.Model.Ciclo;
import br.com.acta.Model.Enum.StatusCiclo;
import br.com.acta.Services.CicloService;
import br.com.acta.Services.UsuarioCicloService;
import br.com.acta.Services.UsuarioService;

public class GeralCicloFragment extends Fragment {
    private final TokenProvider tokenProvider = new FirebaseTokenProvider(FirebaseAuth.getInstance());
    private final UsuarioApi usuarioApi = RetrofitClient.getInstance(tokenProvider).create(UsuarioApi.class);
    private final UsuarioCicloApi usuarioCicloApi = RetrofitClient.getInstance(tokenProvider).create(UsuarioCicloApi.class);
    private final CicloApi cicloApi = RetrofitClient.getInstance(tokenProvider).create(CicloApi.class);
    private final UsuarioService usuarioService = new UsuarioService(usuarioApi);
    private final UsuarioCicloService usuarioCicloService = new UsuarioCicloService(usuarioCicloApi);
    private final CicloService cicloService = new CicloService(cicloApi);

    private TextView txtTituloCiclo;
    private TextView txtStatusCiclo;
    private TextView txtDataInicio;
    private TextView txtPrazoFinal;

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
            Long idCiclo = requireActivity().getIntent().getLongExtra("idCiclo", 0L);
            if (idCiclo != 0L) {
                buscarCiclo(idCiclo);
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
