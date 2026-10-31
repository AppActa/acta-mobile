package br.com.acta.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import br.com.acta.Model.EfeitoSecundario;
import br.com.acta.Model.Enum.TipoEfeitoSecundario;
import br.com.acta.R;

public class EfeitoSecundarioAdapter extends RecyclerView.Adapter<EfeitoSecundarioAdapter.ViewHolder> {

    private final List<EfeitoSecundario> lista;

    public EfeitoSecundarioAdapter(List<EfeitoSecundario> lista) {
        this.lista = lista != null ? lista : new ArrayList<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_card_efeito_secundario, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        EfeitoSecundario efeito = lista.get(position);

        if (efeito.getTipo() == TipoEfeitoSecundario.POSITIVO) {
            holder.txtBadge.setText("Efeito: Positivo");
            holder.txtBadge.setBackgroundResource(R.drawable.bg_status_pill);
            holder.txtDescricao.setTextColor(holder.itemView.getContext().getColor(R.color.efeito_positivo));
        } else if (efeito.getTipo() == TipoEfeitoSecundario.NEGATIVO) {
            holder.txtBadge.setText("Efeito: Negativo");
            holder.txtBadge.setBackgroundResource(R.drawable.bg_status_pill_dark);
            holder.txtDescricao.setTextColor(holder.itemView.getContext().getColor(R.color.efeito_descricao));
        } else {
            holder.txtBadge.setText("Efeito: Neutro");
            holder.txtBadge.setBackgroundResource(R.drawable.bg_status_pill);
            holder.txtDescricao.setTextColor(holder.itemView.getContext().getColor(R.color.efeito_descricao));
        }

        holder.txtDescricao.setText(efeito.getDescricao() != null ? efeito.getDescricao() : "");
        holder.txtImpacto.setText(efeito.getImpactoEstimado() != null ? efeito.getImpactoEstimado() : "");

        if (efeito.getPeso() != null) {
            double pesoVal = efeito.getPeso();
            if (pesoVal <= 1.0 && pesoVal > 0) pesoVal = pesoVal * 100;
            holder.txtPeso.setText("Peso registrado: " + (int) pesoVal + "%");
        } else {
            holder.txtPeso.setText("Peso registrado: 0%");
        }
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtBadge;
        TextView txtDescricao;
        TextView txtImpacto;
        TextView txtPeso;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtBadge = itemView.findViewById(R.id.txtTipoEfeitoBadge);
            txtDescricao = itemView.findViewById(R.id.txtDescricaoEfeitoSecundario);
            txtImpacto = itemView.findViewById(R.id.txtImpactoEstimado);
            txtPeso = itemView.findViewById(R.id.txtPesoRegistrado);
        }
    }
}