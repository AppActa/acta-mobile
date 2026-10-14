package br.com.acta.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import br.com.acta.Model.Meta;
import br.com.acta.R;

public class MetaAdapter extends RecyclerView.Adapter<MetaAdapter.MetaViewHolder> {

    private final List<Meta> metaList;
    private OnMetaClickListener listener;

    public interface OnMetaClickListener {
        void onMetaClick(Meta meta);
    }

    public MetaAdapter(List<Meta> metaList) {
        this.metaList = metaList != null ? metaList : new ArrayList<>();
    }

    public MetaAdapter(List<Meta> metaList, OnMetaClickListener listener) {
        this.metaList = metaList != null ? metaList : new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public MetaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_card_meta, parent, false);
        return new MetaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MetaViewHolder holder, int position) {
        Meta meta = metaList.get(position);

        holder.txtObjetivo.setText(meta.getObjetivo() != null ? "Meta: " + meta.getObjetivo() : "Meta: Chegar a 2%");

        String unidade = meta.getUnidadeMedida() != null ? meta.getUnidadeMedida() : "%";
        String baseStr = meta.getValorBase() != null ? String.valueOf(meta.getValorBase()) + unidade : "8" + unidade;
        String alvoStr = meta.getValorAlvo() != null ? String.valueOf(meta.getValorAlvo()) + unidade : "2" + unidade;

        holder.txtValores.setText("• Base: " + baseStr + "  →  Alvo: " + alvoStr);

        String prazoStr = meta.getPrazo() != null && !meta.getPrazo().trim().isEmpty() ? meta.getPrazo() : "30/10/2026";
        holder.txtResponsavel.setText("• Prazo: " + prazoStr);

        if (holder.btnDetalhes != null) {
            holder.btnDetalhes.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onMetaClick(meta);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return metaList.size();
    }

    public static class MetaViewHolder extends RecyclerView.ViewHolder {
        TextView txtObjetivo;
        TextView txtValores;
        TextView txtResponsavel;
        View btnDetalhes;

        public MetaViewHolder(@NonNull View itemView) {
            super(itemView);
            txtObjetivo = itemView.findViewById(R.id.txtObjetivoMeta);
            txtValores = itemView.findViewById(R.id.txtValoresBaseEAlvo);
            txtResponsavel = itemView.findViewById(R.id.txtResponsavelMeta);
            btnDetalhes = itemView.findViewById(R.id.btnDetalhesVerificacao);
        }
    }
}
