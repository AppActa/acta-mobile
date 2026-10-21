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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

import br.com.acta.Adapter.FormularioAdapter;
import br.com.acta.Api.FormularioApi;
import br.com.acta.Auth.FirebaseTokenProvider;
import br.com.acta.Auth.TokenProvider;
import br.com.acta.Client.RepositoryCallback;
import br.com.acta.Client.RetrofitClientMongo;
import br.com.acta.Model.Formulario;
import br.com.acta.Services.FormularioService;
import br.com.acta.utils.SkeletonLoading;

public class ListaFormulariosFragment extends Fragment {

    private final TokenProvider tokenProvider = new FirebaseTokenProvider(FirebaseAuth.getInstance());
    private final FormularioApi formularioApi = RetrofitClientMongo.getInstance(tokenProvider).create(FormularioApi.class);
    private final FormularioService formularioService = new FormularioService(formularioApi);

    private RecyclerView rvFormularios;
    private TextView txtSubtituloHeader;
    private EditText edtBuscar;

    private List<Formulario> listaCompleta = new ArrayList<>();
    private Long idCicloAtual = 0L;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_lista_formularios, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageButton btnVoltar = view.findViewById(R.id.btnVoltarListaFormularios);
        if (btnVoltar != null) {
            btnVoltar.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());
        }

        rvFormularios = view.findViewById(R.id.rvListaFormularios);
        if (rvFormularios != null) {
            rvFormularios.setLayoutManager(new LinearLayoutManager(requireContext()));
        }

        txtSubtituloHeader = view.findViewById(R.id.txtSubtituloListaFormulariosHeader);
        edtBuscar = view.findViewById(R.id.edtBuscarFormularios);

        if (edtBuscar != null) {
            edtBuscar.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    filtrarFormularios(s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        if (getArguments() != null && getArguments().containsKey("idCiclo")) {
            idCicloAtual = getArguments().getLong("idCiclo", 0L);
        } else if (requireActivity().getIntent() != null && requireActivity().getIntent().hasExtra("idCiclo")) {
            idCicloAtual = requireActivity().getIntent().getLongExtra("idCiclo", 0L);
        }

        if (idCicloAtual != 0L) {
            carregarFormulariosPorCiclo(idCicloAtual);
        }
    }

    private void carregarFormulariosPorCiclo(Long idCiclo) {
        formularioService.buscarPorCiclo(idCiclo, SkeletonLoading.wrap(getView(), new RepositoryCallback<List<Formulario>>() {
            @Override
            public void onSuccess(List<Formulario> formularios) {
                if (!isAdded()) return;

                listaCompleta = formularios != null ? formularios : new ArrayList<>();
                atualizarInterface(listaCompleta);
            }

            @Override
            public void onError(int code, String message) {
            }
        }));
    }

    private void atualizarInterface(List<Formulario> formularios) {
        if (rvFormularios != null) {
            rvFormularios.setAdapter(new FormularioAdapter(formularios, formulario -> {
                if (formulario.getId() != null && getView() != null) {
                    Bundle args = new Bundle();
                    args.putString("idFormulario", formulario.getId());
                    Navigation.findNavController(getView()).navigate(R.id.formularioFragment, args);
                }
            }));
        }
    }

    private void filtrarFormularios(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            atualizarInterface(listaCompleta);
            return;
        }

        String query = texto.toLowerCase().trim();
        List<Formulario> filtrados = new ArrayList<>();

        for (Formulario item : listaCompleta) {
            boolean bateuTitulo = item.getTitulo() != null && item.getTitulo().toLowerCase().contains(query);
            boolean bateuDescricao = item.getDescricao() != null && item.getDescricao().toLowerCase().contains(query);

            if (bateuTitulo || bateuDescricao) {
                filtrados.add(item);
            }
        }

        atualizarInterface(filtrados);
    }
}
