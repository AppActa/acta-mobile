package br.com.acta.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import br.com.acta.Model.Enum.StatusTreinamento;
import br.com.acta.Model.Treinamento;
import br.com.acta.Model.UsuarioTreinamento;
import br.com.acta.R;

public class TreinamentoAdapter extends RecyclerView.Adapter<TreinamentoAdapter.TreinamentoViewHolder> {
    private final List<Treinamento> treinamentos;
    private final List<UsuarioTreinamento> vinculos;
    private final OnStatusChangedListener listener;

    public interface OnStatusChangedListener {
        void onStatusChanged(UsuarioTreinamento vinculo, StatusTreinamento status);
    }

    public TreinamentoAdapter(List<Treinamento> treinamentos, List<UsuarioTreinamento> vinculos, OnStatusChangedListener listener) {
        this.treinamentos = treinamentos != null ? treinamentos : new ArrayList<>();
        this.vinculos = vinculos != null ? vinculos : new ArrayList<>();
        this.listener = listener;
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
        StatusTreinamento statusAtual = vinculo.getStatus() != null ? vinculo.getStatus() : StatusTreinamento.PENDENTE;
        holder.status.setText(statusAtual.name());

        holder.statusTreinamento.setOnItemSelectedListener(null);
        boolean permiteAlterarStatus = statusAtual == StatusTreinamento.PENDENTE;
        holder.statusTreinamento.setEnabled(permiteAlterarStatus);
        ArrayAdapter<StatusTreinamento> statusAdapter = new ArrayAdapter<>(holder.statusTreinamento.getContext(), android.R.layout.simple_spinner_item, StatusTreinamento.values());
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        holder.statusTreinamento.setAdapter(statusAdapter);
        holder.statusTreinamento.setSelection(statusAtual.ordinal(), false);
        holder.statusSelecionado = statusAtual;
        holder.salvarStatus.setEnabled(false);
        holder.salvarStatus.setVisibility(permiteAlterarStatus ? View.VISIBLE : View.GONE);
        holder.statusTreinamento.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int itemPosition, long id) {
                holder.statusSelecionado = StatusTreinamento.values()[itemPosition];
                holder.salvarStatus.setEnabled(holder.statusSelecionado != statusAtual);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        holder.salvarStatus.setOnClickListener(view -> {
            if (!permiteAlterarStatus || holder.statusSelecionado == statusAtual || listener == null) return;
            holder.salvarStatus.setEnabled(false);
            listener.onStatusChanged(vinculo, holder.statusSelecionado);
        });
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
        private final Spinner statusTreinamento;
        private final Button salvarStatus;
        private StatusTreinamento statusSelecionado;

        TreinamentoViewHolder(@NonNull View itemView) {
            super(itemView);
            titulo = itemView.findViewById(R.id.txtTituloTreinamento);
            responsavel = itemView.findViewById(R.id.txtResponsavelTreinamento);
            ciclo = itemView.findViewById(R.id.txtCicloTreinamento);
            status = itemView.findViewById(R.id.txtStatusBadgeTreinamento);
            statusTreinamento = itemView.findViewById(R.id.spnStatusTreinamento);
            salvarStatus = itemView.findViewById(R.id.btnSalvarStatusTreinamento);
        }
    }
}
