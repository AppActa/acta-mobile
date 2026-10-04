package br.com.acta;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

import br.com.acta.Adapter.PlanoAcaoAdapter;
import br.com.acta.Api.CicloApi;
import br.com.acta.Api.PlanoAcaoApi;
import br.com.acta.Api.UsuarioApi;
import br.com.acta.Api.UsuarioCicloApi;
import br.com.acta.Auth.FirebaseTokenProvider;
import br.com.acta.Auth.TokenProvider;
import br.com.acta.Client.RepositoryCallback;
import br.com.acta.Client.RetrofitClient;
import br.com.acta.Model.Ciclo;
import br.com.acta.Model.PlanoAcao;
import br.com.acta.Services.CicloService;
import br.com.acta.Services.PlanoAcaoService;
import br.com.acta.Services.UsuarioCicloService;
import br.com.acta.Services.UsuarioService;

public class DetalhesCicloFragment extends Fragment {
    private final TokenProvider tokenProvider = new FirebaseTokenProvider(FirebaseAuth.getInstance());
    private final UsuarioApi usuarioApi = RetrofitClient.getInstance(tokenProvider).create(UsuarioApi.class);
    private final UsuarioCicloApi usuarioCicloApi = RetrofitClient.getInstance(tokenProvider).create(UsuarioCicloApi.class);
    private final CicloApi cicloApi = RetrofitClient.getInstance(tokenProvider).create(CicloApi.class);
    private final PlanoAcaoApi planoAcaoApi = RetrofitClient.getInstance(tokenProvider).create(PlanoAcaoApi.class);
    private final PlanoAcaoService planoAcaoService = new PlanoAcaoService(planoAcaoApi);
    private final UsuarioService usuarioService = new UsuarioService(usuarioApi);
    private final UsuarioCicloService usuarioCicloService = new UsuarioCicloService(usuarioCicloApi);
    private final CicloService cicloService = new CicloService(cicloApi);

    private RecyclerView rvPlanosAcao;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_detalhes_ciclo, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageButton btnVoltar = view.findViewById(R.id.btnVoltarDetalhesCiclo);
        if (btnVoltar != null) {
            btnVoltar.setOnClickListener(v -> requireActivity().finish());
        }

        rvPlanosAcao = view.findViewById(R.id.rvPlanosAcao);
        if (rvPlanosAcao != null) {
            rvPlanosAcao.setLayoutManager(new LinearLayoutManager(requireContext()));
        }

        // Pega o idCiclo passado na Intent da CicloPdcaActivity
        if (requireActivity().getIntent() != null && requireActivity().getIntent().hasExtra("idCiclo")) {
            Long idCiclo = requireActivity().getIntent().getLongExtra("idCiclo", 0L);
            if (idCiclo != 0L) {
                buscarCiclo(idCiclo);
                buscarPlanoAcao(idCiclo);
            }
        }
    }

    private void buscarCiclo(Long idCiclo) {
        cicloService.getCiclo(idCiclo, new RepositoryCallback<Ciclo>() {
            @Override
            public void onSuccess(Ciclo ciclo) {
            }

            @Override
            public void onError(int code, String message) {
            }
        });
    }

    private void buscarPlanoAcao(Long idCiclo) {
        planoAcaoService.buscarPorCiclo(idCiclo, new RepositoryCallback<List<PlanoAcao>>() {
            @Override
            public void onSuccess(List<PlanoAcao> planoAcaoList) {
                if (!isAdded()) return;

                if (rvPlanosAcao != null && planoAcaoList != null) {
                    PlanoAcaoAdapter adapter = new PlanoAcaoAdapter(planoAcaoList);
                    rvPlanosAcao.setAdapter(adapter);
                }
            }

            @Override
            public void onError(int code, String message) {
            }
        });
    }
}
