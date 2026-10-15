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

import br.com.acta.Adapter.MetaAdapter;
import br.com.acta.Api.MetaApi;
import br.com.acta.Api.PlanoAcaoApi;
import br.com.acta.Auth.FirebaseTokenProvider;
import br.com.acta.Auth.TokenProvider;
import br.com.acta.Client.RepositoryCallback;
import br.com.acta.Client.RetrofitClient;
import br.com.acta.Model.Meta;
import br.com.acta.Model.PlanoAcao;
import br.com.acta.Services.MetaService;
import br.com.acta.Services.PlanoAcaoService;
import br.com.acta.utils.SkeletonLoading;

public class MetasPlanoAcaoFragment extends Fragment {

    private final TokenProvider tokenProvider = new FirebaseTokenProvider(FirebaseAuth.getInstance());
    private final MetaApi metaApi = RetrofitClient.getInstance(tokenProvider).create(MetaApi.class);
    private final PlanoAcaoApi planoAcaoApi = RetrofitClient.getInstance(tokenProvider).create(PlanoAcaoApi.class);

    private final MetaService metaService = new MetaService(metaApi);
    private final PlanoAcaoService planoAcaoService = new PlanoAcaoService(planoAcaoApi);

    private RecyclerView rvMetas;
    private TextView txtNomePlano;
    private TextView txtPrioridadePlano;
    private TextView txtStatusBadgePlano;
    private TextView txtObjetivoPlano;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_metas_plano_acao, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageButton btnVoltar = view.findViewById(R.id.btnVoltarMetasPlano);
        if (btnVoltar != null) {
            btnVoltar.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());
        }

        rvMetas = view.findViewById(R.id.rvMetasPlanoAcao);
        if (rvMetas != null) {
            rvMetas.setLayoutManager(new LinearLayoutManager(requireContext()));
        }

        txtNomePlano = view.findViewById(R.id.txtNomePlanoMetasHeader);
        txtPrioridadePlano = view.findViewById(R.id.txtPrioridadePlanoMetasHeader);
        txtStatusBadgePlano = view.findViewById(R.id.txtStatusPlanoBadgeMetasHeader);
        txtObjetivoPlano = view.findViewById(R.id.txtObjetivoPlanoMetasHeader);

        Long idPlanoAcao = 0L;
        if (getArguments() != null && getArguments().containsKey("idPlanoAcao")) {
            idPlanoAcao = getArguments().getLong("idPlanoAcao", 0L);
        } else if (requireActivity().getIntent() != null && requireActivity().getIntent().hasExtra("idPlanoAcao")) {
            idPlanoAcao = requireActivity().getIntent().getLongExtra("idPlanoAcao", 0L);
        }

        if (idPlanoAcao != 0L) {
            buscarPlanoAcao(idPlanoAcao);
            buscarMetasDoPlano(idPlanoAcao);
        }
    }

    private void buscarPlanoAcao(Long idPlanoAcao) {
        planoAcaoService.buscarPorId(idPlanoAcao, SkeletonLoading.wrap(getView(), new RepositoryCallback<PlanoAcao>() {
            @Override
            public void onSuccess(PlanoAcao plano) {
                if (!isAdded() || plano == null) return;

                if (txtNomePlano != null && plano.getNome() != null) {
                    txtNomePlano.setText(plano.getNome());
                }

                if (txtPrioridadePlano != null) {
                    if (plano.getPrioridade() != null) {
                        txtPrioridadePlano.setText("Prioridade: " + plano.getPrioridade().name());
                    } else {
                        txtPrioridadePlano.setText("Prioridade: Alta");
                    }
                }

                if (txtStatusBadgePlano != null) {
                    if (plano.getStatus() != null) {
                        txtStatusBadgePlano.setText(plano.getStatus().name());
                    } else {
                        txtStatusBadgePlano.setText("Em execução");
                    }
                }

                if (txtObjetivoPlano != null && plano.getObjetivo() != null) {
                    txtObjetivoPlano.setText(plano.getObjetivo());
                }
            }

            @Override
            public void onError(int code, String message) {
            }
        }));
    }

    private void buscarMetasDoPlano(Long idPlanoAcao) {
        metaService.buscarPorPlanoAcao(idPlanoAcao, SkeletonLoading.wrap(getView(), new RepositoryCallback<List<Meta>>() {
            @Override
            public void onSuccess(List<Meta> metas) {
                if (!isAdded()) return;

                if (metas != null && !metas.isEmpty()) {
                    if (rvMetas != null) {
                        rvMetas.setAdapter(new MetaAdapter(metas));
                    }
                }
            }

            @Override
            public void onError(int code, String message) {
            }
        }));
    }
}
