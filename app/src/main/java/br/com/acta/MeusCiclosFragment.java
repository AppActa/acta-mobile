package br.com.acta;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import br.com.acta.Adapter.CicloAdapter;
import br.com.acta.DAO.AppDatabase;
import br.com.acta.Model.Ciclo;

public class MeusCiclosFragment extends Fragment {

    private CicloAdapter cicloAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_meus_ciclos, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageButton btnVoltar = view.findViewById(R.id.btnVoltarMeusCiclos);
        if (btnVoltar != null) {
            btnVoltar.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());
        }

        RecyclerView rvTodosCiclosGrid = view.findViewById(R.id.rvTodosCiclosGrid);
        if (rvTodosCiclosGrid != null) {
            rvTodosCiclosGrid.setLayoutManager(new GridLayoutManager(requireContext(), 2));

            try {
                List<Ciclo> ciclosLocais = AppDatabase.getInstance(requireContext()).cicloDao().listarTodos();
                if (ciclosLocais != null && !ciclosLocais.isEmpty()) {
                    cicloAdapter = new CicloAdapter(ciclosLocais);
                    rvTodosCiclosGrid.setAdapter(cicloAdapter);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        EditText edtBuscarCiclo = view.findViewById(R.id.edtBuscarCiclo);
        if (edtBuscarCiclo != null) {
            edtBuscarCiclo.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (cicloAdapter != null) {
                        cicloAdapter.filtrar(s.toString());
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }
    }
}
