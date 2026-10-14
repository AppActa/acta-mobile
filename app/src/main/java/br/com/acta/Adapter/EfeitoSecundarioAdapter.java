package br.com.acta.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.math.BigDecimal;
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
            holder.txtDescricao.setTextColor(android.graphics.Color.parseColor("#238357"));
        } else if (efeito.getTipo() == TipoEfeitoSecundario.NEGATIVO) {
            holder.txtBadge.setText("Efeito: Negativo");
            holder.txtBadge.setBackgroundResource(R.drawable.bg_status_pill_dark);
            holder.txtDescricao.setTextColor(android.graphics.Color.parseColor("#083248"));
        } else {
            holder.txtBadge.setText("Efeito: Neutro");
            holder.txtBadge.setBackgroundResource(R.drawable.bg_status_pill);
        }

        holder.txtDescricao.setText(efeito.getDescricao() != null ? efeito.getDescricao() : "");
        holder.txtImpacto.setText(efeito.getImpactoEstimado() != null ? efeito.getImpactoEstimado() : "");

        if (efeito.getPeso() != null) {
            BigDecimal peso = efeito.getPeso();
            // Se o peso vier como fração 0.20, converte para 20%
            if (peso.compareTo(BigDecimal.ONE) <= 0 && peso.compareTo(BigDecimal.ZERO) > 0) {
                peso = peso.multiply(new BigDecimal("100"));
            }
            holder.txtPeso.setText("Peso registrado: " + peso.intValue() + "%");
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
