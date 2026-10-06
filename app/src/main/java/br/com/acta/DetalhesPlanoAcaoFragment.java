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

import br.com.acta.Api.PlanoAcaoApi;
import br.com.acta.Auth.FirebaseTokenProvider;
import br.com.acta.Auth.TokenProvider;
import br.com.acta.Client.RepositoryCallback;
import br.com.acta.Client.RetrofitClient;
import br.com.acta.Model.PlanoAcao;
import br.com.acta.Services.PlanoAcaoService;

public class DetalhesPlanoAcaoFragment extends Fragment {

    private final TokenProvider tokenProvider = new FirebaseTokenProvider(FirebaseAuth.getInstance());
    private final PlanoAcaoApi planoAcaoApi = RetrofitClient.getInstance(tokenProvider).create(PlanoAcaoApi.class);
    private final PlanoAcaoService planoAcaoService = new PlanoAcaoService(planoAcaoApi);

    private TextView txtNomePlano;
    private TextView txtPrioridade;
    private TextView txtStatus;
    private TextView txtObjetivo;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_detalhes_plano_acao, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageButton btnVoltar = view.findViewById(R.id.btnVoltarDetalhesPlanoAcao);
        if (btnVoltar != null) {
            btnVoltar.setOnClickListener(v -> {
                try {
                    Navigation.findNavController(v).navigateUp();
                } catch (Exception e) {
                    requireActivity().finish();
                }
            });
        }

        txtNomePlano = view.findViewById(R.id.txtNomePlanoAcaoDetalhe);
        txtPrioridade = view.findViewById(R.id.txtPrioridadePlanoAcaoDetalhe);
        txtStatus = view.findViewById(R.id.txtStatusPlanoAcaoDetalhe);
        txtObjetivo = view.findViewById(R.id.txtObjetivoPlanoAcaoDetalhe);

        Long idPlanoAcao = 0L;
        if (getArguments() != null && getArguments().containsKey("idPlanoAcao")) {
            idPlanoAcao = getArguments().getLong("idPlanoAcao", 0L);
        } else if (requireActivity().getIntent() != null && requireActivity().getIntent().hasExtra("idPlanoAcao")) {
            idPlanoAcao = requireActivity().getIntent().getLongExtra("idPlanoAcao", 0L);
        }

        if (idPlanoAcao != 0L) {
            buscarPlanoAcao(idPlanoAcao);
        }
    }

    private void buscarPlanoAcao(Long idPlanoAcao) {
        planoAcaoService.buscarPorId(idPlanoAcao, new RepositoryCallback<PlanoAcao>() {
            @Override
            public void onSuccess(PlanoAcao plano) {
                if (!isAdded() || plano == null) return;

                // Salva o idCiclo na Intent da Activity para que ao voltar o GeralCicloFragment funcione perfeitamente
                if (plano.getIdCiclo() != null && requireActivity().getIntent() != null) {
                    requireActivity().getIntent().putExtra("idCiclo", plano.getIdCiclo());
                }

                if (txtNomePlano != null && plano.getNome() != null) {
                    txtNomePlano.setText(plano.getNome());
                }

                if (txtPrioridade != null && plano.getPrioridade() != null) {
                    txtPrioridade.setText("Prioridade: " + plano.getPrioridade().name());
                }

                if (txtStatus != null && plano.getStatus() != null) {
                    txtStatus.setText(plano.getStatus().name());
                }

                if (txtObjetivo != null && plano.getObjetivo() != null) {
                    txtObjetivo.setText(plano.getObjetivo());
                }
            }

            @Override
            public void onError(int code, String message) {
            }
        });
    }
}
