package br.com.acta;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

import br.com.acta.Adapter.LicaoAprendidaAdapter;
import br.com.acta.Api.LicaoAprendidaApi;
import br.com.acta.Auth.FirebaseTokenProvider;
import br.com.acta.Auth.TokenProvider;
import br.com.acta.Client.RepositoryCallback;
import br.com.acta.Client.RetrofitClientMongo;
import br.com.acta.Model.LicaoAprendida;
import br.com.acta.Services.LicaoAprendidaService;
import br.com.acta.utils.SkeletonLoading;

public class LicoesAprendidasFragment extends Fragment {

    private final TokenProvider tokenProvider = new FirebaseTokenProvider(FirebaseAuth.getInstance());
    private final LicaoAprendidaApi licaoApi = RetrofitClientMongo.getInstance(tokenProvider).create(LicaoAprendidaApi.class);
    private final LicaoAprendidaService licaoService = new LicaoAprendidaService(licaoApi);

    private RecyclerView rvLicoes;
    private TextView txtQtdAprendizados;

    private List<LicaoAprendida> listaCompletaLicoes = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_licoes_aprendidas, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageButton btnVoltar = view.findViewById(R.id.btnVoltarAprendizados);
        if (btnVoltar != null) {
            btnVoltar.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());
        }

        rvLicoes = view.findViewById(R.id.rvLicoesAprendidas);
        if (rvLicoes != null) {
            rvLicoes.setLayoutManager(new LinearLayoutManager(requireContext()));
        }

        txtQtdAprendizados = view.findViewById(R.id.txtQtdAprendizados);
        EditText edtBuscar = view.findViewById(R.id.edtBuscarAprendizados);
        View btnAdicionarNovo = view.findViewById(R.id.btnAdicionarNovoAprendizado);

        if (btnAdicionarNovo != null) {
            btnAdicionarNovo.setOnClickListener(v -> {
                Bundle args = new Bundle();
                if (getArguments() != null && getArguments().containsKey("idCiclo")) {
                    args.putLong("idCiclo", getArguments().getLong("idCiclo", 0L));
                }
                Navigation.findNavController(v).navigate(R.id.registrarAprendizadoFragment, args);
            });
        }

        if (edtBuscar != null) {
            edtBuscar.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    filtrarLicoes(s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        long idCiclo = 0L;
        if (getArguments() != null && getArguments().containsKey("idCiclo")) {
            idCiclo = getArguments().getLong("idCiclo", 0L);
        } else if (requireActivity().getIntent() != null && requireActivity().getIntent().hasExtra("idCiclo")) {
            idCiclo = requireActivity().getIntent().getLongExtra("idCiclo", 0L);
        }

        if (idCiclo != 0L) {
            buscarLicoesPorCiclo(idCiclo);
        }
    }

    private void buscarLicoesPorCiclo(Long idCiclo) {
        licaoService.buscarPorCiclo(idCiclo, SkeletonLoading.wrap(getView(), new RepositoryCallback<List<LicaoAprendida>>() {
            @Override
            public void onSuccess(List<LicaoAprendida> licoes) {
                if (getView() == null) return;

                listaCompletaLicoes = licoes != null ? new ArrayList<>(licoes) : new ArrayList<>();
                String idLicao = getArguments() == null ? null : getArguments().getString("idLicao");
                if (idLicao != null) {
                    listaCompletaLicoes.removeIf(licao -> !idLicao.equalsIgnoreCase(licao.getId()));
                    if (listaCompletaLicoes.isEmpty()) Toast.makeText(requireContext(), "Esta lição não está disponível neste ciclo.", Toast.LENGTH_LONG).show();
                }
                atualizarInterface(listaCompletaLicoes);
            }

            @Override
            public void onError(int code, String message) {
                if (getView() != null) Toast.makeText(requireContext(), "Não foi possível carregar as lições. Tente novamente.", Toast.LENGTH_LONG).show();
            }
        }));
    }

    private void atualizarInterface(List<LicaoAprendida> licoes) {
        int qtd = licoes.size();
        if (txtQtdAprendizados != null) {
            txtQtdAprendizados.setText("Você tem " + qtd + " aprendizado" + (qtd == 1 ? "" : "s"));
        }

        if (rvLicoes != null) {
            rvLicoes.setAdapter(new LicaoAprendidaAdapter(licoes));
        }
    }

    private void filtrarLicoes(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            atualizarInterface(listaCompletaLicoes);
            return;
        }

        String query = texto.toLowerCase().trim();
        List<LicaoAprendida> filtradas = new ArrayList<>();

        for (LicaoAprendida item : listaCompletaLicoes) {
            boolean bateuTitulo = item.getTitulo() != null && item.getTitulo().toLowerCase().contains(query);
            boolean bateuLicao = item.getLicao() != null && item.getLicao().toLowerCase().contains(query);
            boolean bateuArea = item.getArea() != null && item.getArea().toLowerCase().contains(query);

            if (bateuTitulo || bateuLicao || bateuArea) {
                filtradas.add(item);
            }
        }

        atualizarInterface(filtradas);
    }
}