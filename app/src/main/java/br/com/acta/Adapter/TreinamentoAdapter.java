package br.com.acta.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import br.com.acta.Model.Treinamento;
import br.com.acta.Model.UsuarioTreinamento;
import br.com.acta.R;

public class TreinamentoAdapter extends RecyclerView.Adapter<TreinamentoAdapter.TreinamentoViewHolder> {
    private final List<Treinamento> treinamentos;
    private final List<UsuarioTreinamento> vinculos;

    public TreinamentoAdapter(List<Treinamento> treinamentos, List<UsuarioTreinamento> vinculos) {
        this.treinamentos = treinamentos != null ? treinamentos : new ArrayList<>();
        this.vinculos = vinculos != null ? vinculos : new ArrayList<>();
    }

    @NonNull
    @Override
    public TreinamentoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_card_treinamento, parent, false);
        return new TreinamentoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TreinamentoViewHolder holder, int position) {
        Treinamento treinamento = treinamentos.get(position);
        UsuarioTreinamento vinculo = vinculos.get(position);

        holder.titulo.setText(treinamento.getTitulo() != null ? treinamento.getTitulo() : "Treinamento");
        holder.responsavel.setText(vinculo.getObrigatorio() != null && vinculo.getObrigatorio()
                ? "Participação obrigatória" : "Participação opcional");
        holder.ciclo.setText(treinamento.getDescricao() != null ? treinamento.getDescricao() : "");
        holder.status.setText(vinculo.getStatus() != null ? vinculo.getStatus().name() : "PENDENTE");
    }

    @Override
    public int getItemCount() {
        return treinamentos.size();
    }

    static class TreinamentoViewHolder extends RecyclerView.ViewHolder {
        private final TextView titulo;
        private final TextView responsavel;
        private final TextView ciclo;
        private final TextView status;

        TreinamentoViewHolder(@NonNull View itemView) {
            super(itemView);
            titulo = itemView.findViewById(R.id.txtTituloTreinamento);
            responsavel = itemView.findViewById(R.id.txtResponsavelTreinamento);
            ciclo = itemView.findViewById(R.id.txtCicloTreinamento);
            status = itemView.findViewById(R.id.txtStatusBadgeTreinamento);
        }
    }
}
