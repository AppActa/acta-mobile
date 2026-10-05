package br.com.acta.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import br.com.acta.Model.PlanoAcao;
import br.com.acta.R;

public class PlanoAcaoAdapter extends RecyclerView.Adapter<PlanoAcaoAdapter.PlanoAcaoViewHolder> {

    private final List<PlanoAcao> planoAcaoList;
    private final List<PlanoAcao> planoAcaoListFull;

    public PlanoAcaoAdapter(List<PlanoAcao> planoAcaoList) {
        this.planoAcaoList = planoAcaoList != null ? planoAcaoList : new ArrayList<>();
        this.planoAcaoListFull = new ArrayList<>(this.planoAcaoList);
    }

    @NonNull
    @Override
    public PlanoAcaoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_card_plano_acao, parent, false);
        return new PlanoAcaoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlanoAcaoViewHolder holder, int position) {
        PlanoAcao plano = planoAcaoList.get(position);
        holder.card.setOnClickListener(view -> {

        });

        if (plano.getOrigem() != null) {
            holder.lblOrigem.setText("Origem: " + plano.getOrigem().name());
        } else {
            holder.lblOrigem.setText("Origem: MANUAL");
        }

        if (plano.getStatus() != null) {
            holder.txtStatus.setText(plano.getStatus().name());
        } else {
            holder.txtStatus.setText("PENDENTE");
        }

        holder.txtNome.setText(plano.getNome() != null ? plano.getNome() : "");
        holder.txtObjetivo.setText(plano.getObjetivo() != null ? plano.getObjetivo() : "");

        if (plano.getPrioridade() != null) {
            holder.txtPrioridade.setText("Prioridade: " + plano.getPrioridade().name());
        } else {
            holder.txtPrioridade.setText("Prioridade: MEDIA");
        }
    }

    @Override
    public int getItemCount() {
        return planoAcaoList.size();
    }

    public void filtrar(String texto) {
        planoAcaoList.clear();
        if (texto == null || texto.trim().isEmpty()) {
            planoAcaoList.addAll(planoAcaoListFull);
        } else {
            String filtro = texto.toLowerCase().trim();
            for (PlanoAcao plano : planoAcaoListFull) {
                if (plano.getNome() != null && plano.getNome().toLowerCase().contains(filtro)) {
                    planoAcaoList.add(plano);
                }
            }
        }
        notifyDataSetChanged();
    }

    public static class PlanoAcaoViewHolder extends RecyclerView.ViewHolder {
        TextView lblOrigem;
        TextView txtStatus;
        TextView txtNome;
        TextView txtObjetivo;
        TextView txtPrioridade;
        ConstraintLayout card;

        public PlanoAcaoViewHolder(@NonNull View itemView) {
            super(itemView);
            lblOrigem = itemView.findViewById(R.id.lblOrigemPlanoAcao);
            txtStatus = itemView.findViewById(R.id.txtStatusPlanoAcao);
            txtNome = itemView.findViewById(R.id.txtNomePlanoAcao);
            txtObjetivo = itemView.findViewById(R.id.txtObjetivoPlanoAcao);
            txtPrioridade = itemView.findViewById(R.id.txtPrioridadePlanoAcao);
            card = itemView.findViewById(R.id.cardPlanoAcaoItem);
        }
    }
}
