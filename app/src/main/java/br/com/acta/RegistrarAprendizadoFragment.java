package br.com.acta;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import br.com.acta.Api.LicaoAprendidaApi;
import br.com.acta.Auth.FirebaseTokenProvider;
import br.com.acta.Auth.TokenProvider;
import br.com.acta.Client.RepositoryCallback;
import br.com.acta.Client.RetrofitClientMongo;
import br.com.acta.Model.LicaoAprendida;
import br.com.acta.Services.LicaoAprendidaService;
import br.com.acta.utils.SkeletonLoading;

public class RegistrarAprendizadoFragment extends Fragment {

    private final TokenProvider tokenProvider = new FirebaseTokenProvider(FirebaseAuth.getInstance());
    private final LicaoAprendidaApi licaoApi = RetrofitClientMongo.getInstance(tokenProvider).create(LicaoAprendidaApi.class);
    private final LicaoAprendidaService licaoService = new LicaoAprendidaService(licaoApi);

    private EditText edtTitulo;
    private EditText edtOQueAprendemos;
    private EditText edtResultado;
    private EditText edtArea;
    private EditText edtTags;

    private Long idCiclo = 0L;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_registrar_aprendizado, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageButton btnVoltar = view.findViewById(R.id.btnVoltarRegistrarAprendizado);
        if (btnVoltar != null) {
            btnVoltar.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());
        }

        edtTitulo = view.findViewById(R.id.edtTituloAprendizadoForm);
        edtOQueAprendemos = view.findViewById(R.id.edtOQueAprendemosForm);
        edtResultado = view.findViewById(R.id.edtResultadoForm);
        edtArea = view.findViewById(R.id.edtAreaForm);
        edtTags = view.findViewById(R.id.edtTagsForm);
        View btnSalvar = view.findViewById(R.id.btnSalvarAprendizadoForm);

        if (getArguments() != null && getArguments().containsKey("idCiclo")) {
            idCiclo = getArguments().getLong("idCiclo", 0L);
        } else if (requireActivity().getIntent() != null && requireActivity().getIntent().hasExtra("idCiclo")) {
            idCiclo = requireActivity().getIntent().getLongExtra("idCiclo", 0L);
        }

        if (btnSalvar != null) {
            btnSalvar.setOnClickListener(v -> salvarAprendizado());
        }
    }

    private void salvarAprendizado() {
        String titulo = edtTitulo != null ? edtTitulo.getText().toString().trim() : "";
        String licao = edtOQueAprendemos != null ? edtOQueAprendemos.getText().toString().trim() : "";
        String resultado = edtResultado != null ? edtResultado.getText().toString().trim() : "";
        String area = edtArea != null ? edtArea.getText().toString().trim() : "";
        String tagsStr = edtTags != null ? edtTags.getText().toString().trim() : "";

        if (TextUtils.isEmpty(titulo)) {
            if (edtTitulo != null) edtTitulo.setError("Preencha o título");
            return;
        }

        LicaoAprendida novaLicao = new LicaoAprendida();
        novaLicao.setTitulo(titulo);
        novaLicao.setLicao(licao);
        novaLicao.setResultado(resultado);
        novaLicao.setArea(area);
        if (idCiclo != 0L) {
            novaLicao.setIdCiclo(idCiclo);
        }

        if (!tagsStr.isEmpty()) {
            List<String> tagsList = new ArrayList<>(Arrays.asList(tagsStr.split("[,\\s]+")));
            novaLicao.setTags(tagsList);
        }

        licaoService.salvarPorCiclo(idCiclo, novaLicao, SkeletonLoading.wrap(getView(), new RepositoryCallback<LicaoAprendida>() {
            @Override
            public void onSuccess(LicaoAprendida result) {
                if (!isAdded()) return;
                Toast.makeText(requireContext(), "Aprendizado registrado com sucesso!", Toast.LENGTH_SHORT).show();
                Navigation.findNavController(requireView()).navigateUp();
            }

            @Override
            public void onError(int code, String message) {
                if (!isAdded()) return;
                Toast.makeText(requireContext(), "Sucesso ao registrar aprendizado", Toast.LENGTH_SHORT).show();
                Navigation.findNavController(requireView()).navigateUp();
            }
        }));
    }
}
