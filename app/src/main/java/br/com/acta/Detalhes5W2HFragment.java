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

import br.com.acta.Api.Plano5W2HApi;
import br.com.acta.Auth.FirebaseTokenProvider;
import br.com.acta.Auth.TokenProvider;
import br.com.acta.Client.RepositoryCallback;
import br.com.acta.Client.RetrofitClient;
import br.com.acta.Model.Plano5W2H;
import br.com.acta.Services.Plano5W2HService;

public class Detalhes5W2HFragment extends Fragment {

    private final TokenProvider tokenProvider = new FirebaseTokenProvider(FirebaseAuth.getInstance());
    private final Plano5W2HApi plano5W2HApi = RetrofitClient.getInstance(tokenProvider).create(Plano5W2HApi.class);
    private final Plano5W2HService plano5W2HService = new Plano5W2HService(plano5W2HApi);

    private TextView txtWhat, txtWhy, txtWhere, txtWhen, txtWho, txtHow, txtHowMuch;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_detalhes_5w2h, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageButton btnVoltar = view.findViewById(R.id.btnVoltarDetalhes5W2H);
        if (btnVoltar != null) {
            btnVoltar.setOnClickListener(v -> {
                try {
                    Navigation.findNavController(v).navigateUp();
                } catch (Exception e) {
                    requireActivity().finish();
                }
            });
        }

        txtWhat = view.findViewById(R.id.txtWhatAcao);
        txtWhy = view.findViewById(R.id.txtWhyJustificativa);
        txtWhere = view.findViewById(R.id.txtWhereLocal);
        txtWhen = view.findViewById(R.id.txtWhenPrazo);
        txtWho = view.findViewById(R.id.txtWhoResponsavel);
        txtHow = view.findViewById(R.id.txtHowModoExecucao);
        txtHowMuch = view.findViewById(R.id.txtHowMuchCusto);

        Long idPlanoAcao = 0L;
        if (getArguments() != null && getArguments().containsKey("idPlanoAcao")) {
            idPlanoAcao = getArguments().getLong("idPlanoAcao", 0L);
        } else if (requireActivity().getIntent() != null && requireActivity().getIntent().hasExtra("idPlanoAcao")) {
            idPlanoAcao = requireActivity().getIntent().getLongExtra("idPlanoAcao", 0L);
        }

        if (idPlanoAcao != 0L) {
            buscar5W2H(idPlanoAcao);
        }
    }

    private void buscar5W2H(Long idPlanoAcao) {
        plano5W2HService.buscarPorPlanoAcao(idPlanoAcao, new RepositoryCallback<Plano5W2H>() {
            @Override
            public void onSuccess(Plano5W2H plano) {
                if (!isAdded() || plano == null) return;

                if (txtWhat != null && plano.getWhatAcao() != null) {
                    txtWhat.setText(plano.getWhatAcao());
                }
                if (txtWhy != null && plano.getWhyJustificativa() != null) {
                    txtWhy.setText(plano.getWhyJustificativa());
                }
                if (txtWhere != null && plano.getWhereLocal() != null) {
                    txtWhere.setText(plano.getWhereLocal());
                }
                if (txtWhen != null) {
                    String inicio = plano.getWhenInicio() != null ? plano.getWhenInicio() : "-";
                    String fim = plano.getWhenFim() != null ? plano.getWhenFim() : "-";
                    txtWhen.setText("Início: " + inicio + " - Fim: " + fim);
                }
                if (txtWho != null) {
                    if (plano.getWhoResponsavel() != null && plano.getWhoResponsavel().getNome() != null) {
                        txtWho.setText(plano.getWhoResponsavel().getNome());
                    } else {
                        txtWho.setText("ID: " + (plano.getIdWhoResponsavel() != null ? plano.getIdWhoResponsavel() : "-"));
                    }
                }
                if (txtHow != null && plano.getHowModoExecucao() != null) {
                    txtHow.setText(plano.getHowModoExecucao());
                }
                if (txtHowMuch != null && plano.getHowMuchCusto() != null) {
                    txtHowMuch.setText("R$ " + plano.getHowMuchCusto());
                }
            }

            @Override
            public void onError(int code, String message) {
            }
        });
    }
}
