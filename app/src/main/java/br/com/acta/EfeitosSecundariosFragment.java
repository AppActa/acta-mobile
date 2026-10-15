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

import br.com.acta.Adapter.EfeitoSecundarioAdapter;
import br.com.acta.Api.EfeitoSecundarioApi;
import br.com.acta.Api.VerificacaoResultadoApi;
import br.com.acta.Auth.FirebaseTokenProvider;
import br.com.acta.Auth.TokenProvider;
import br.com.acta.Client.RepositoryCallback;
import br.com.acta.Client.RetrofitClient;
import br.com.acta.Model.EfeitoSecundario;
import br.com.acta.Model.VerificacaoResultado;
import br.com.acta.Services.EfeitoSecundarioService;
import br.com.acta.Services.VerificacaoResultadoService;
import br.com.acta.utils.SkeletonLoading;

public class EfeitosSecundariosFragment extends Fragment {

    private final TokenProvider tokenProvider = new FirebaseTokenProvider(FirebaseAuth.getInstance());
    private final EfeitoSecundarioApi efeitoSecundarioApi = RetrofitClient.getInstance(tokenProvider).create(EfeitoSecundarioApi.class);
    private final VerificacaoResultadoApi verificacaoResultadoApi = RetrofitClient.getInstance(tokenProvider).create(VerificacaoResultadoApi.class);

    private final EfeitoSecundarioService efeitoSecundarioService = new EfeitoSecundarioService(efeitoSecundarioApi);
    private final VerificacaoResultadoService verificacaoResultadoService = new VerificacaoResultadoService(verificacaoResultadoApi);

    private RecyclerView rvEfeitos;
    private View cardResultadoInclude;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_efeitos_secundarios, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageButton btnVoltar = view.findViewById(R.id.btnVoltarEfeitosSecundarios);
        if (btnVoltar != null) {
            btnVoltar.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());
        }

        rvEfeitos = view.findViewById(R.id.rvEfeitosSecundarios);
        if (rvEfeitos != null) {
            rvEfeitos.setLayoutManager(new LinearLayoutManager(requireContext()));
        }

        cardResultadoInclude = view.findViewById(R.id.cardResultadoResumoInclude);

        Long idResultado = 0L;
        Long idCiclo = 0L;

        if (getArguments() != null) {
            if (getArguments().containsKey("idResultado")) {
                idResultado = getArguments().getLong("idResultado", 0L);
            }
            if (getArguments().containsKey("idCiclo")) {
                idCiclo = getArguments().getLong("idCiclo", 0L);
            }
        }

        if (idResultado != 0L) {
            buscarEfeitos(idResultado);
            buscarResultado(idResultado);
        } else if (idCiclo != 0L) {
            buscarResultadoPorCiclo(idCiclo);
        }
    }

    private void buscarEfeitos(Long idResultado) {
        efeitoSecundarioService.buscarEfeitosSecundarios(idResultado, SkeletonLoading.wrap(getView(), new RepositoryCallback<List<EfeitoSecundario>>() {
            @Override
            public void onSuccess(List<EfeitoSecundario> lista) {
                if (!isAdded()) return;

                if (lista != null && !lista.isEmpty()) {
                    if (rvEfeitos != null) {
                        rvEfeitos.setAdapter(new EfeitoSecundarioAdapter(lista));
                    }
                }
            }

            @Override
            public void onError(int code, String message) {
            }
        }));
    }

    private void buscarResultado(Long idResultado) {
        verificacaoResultadoService.buscarPorId(idResultado, SkeletonLoading.wrap(getView(), new RepositoryCallback<VerificacaoResultado>() {
            @Override
            public void onSuccess(VerificacaoResultado resultado) {
                if (!isAdded() || resultado == null) return;
                preencherCardResultado(resultado);
            }

            @Override
            public void onError(int code, String message) {
            }
        }));
    }

    private void buscarResultadoPorCiclo(Long idCiclo) {
        verificacaoResultadoService.buscarPorCiclo(idCiclo, SkeletonLoading.wrap(getView(), new RepositoryCallback<List<VerificacaoResultado>>() {
            @Override
            public void onSuccess(List<VerificacaoResultado> lista) {
                if (!isAdded() || lista == null || lista.isEmpty()) return;

                VerificacaoResultado resultado = lista.get(0);
                preencherCardResultado(resultado);

                if (resultado.getId() != null) {
                    buscarEfeitos(resultado.getId());
                }
            }

            @Override
            public void onError(int code, String message) {
            }
        }));
    }

    private void preencherCardResultado(VerificacaoResultado resultado) {
        if (cardResultadoInclude == null || resultado == null) return;

        TextView txtBadge = cardResultadoInclude.findViewById(R.id.txtStatusVerificacaoBadge);
        TextView txtObservacao = cardResultadoInclude.findViewById(R.id.txtObservacaoResultado);
        View btnVerEfeitos = cardResultadoInclude.findViewById(R.id.btnVerEfeitosSecundarios);

        if (txtBadge != null && resultado.getStatus() != null) {
            txtBadge.setText(resultado.getStatus().name());
        }
        if (txtObservacao != null && resultado.getObservacao() != null) {
            txtObservacao.setText(resultado.getObservacao());
        }

        // Oculta o botão "Ver efeitos secundários" pois já estamos na tela de efeitos!
        if (btnVerEfeitos != null) {
            btnVerEfeitos.setVisibility(View.GONE);
        }
    }
}
