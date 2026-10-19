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
import br.com.acta.utils.FormatoData;

public class MetaAdapter extends RecyclerView.Adapter<MetaAdapter.MetaViewHolder> {

    private final List<Meta> metaList;

    public MetaAdapter(List<Meta> metaList) {
        this.metaList = metaList != null ? metaList : new ArrayList<>();
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

        String prazoStr = FormatoData.formatar(holder.itemView.getContext(), meta.getPrazo());
        holder.txtResponsavel.setText("• Prazo: " + prazoStr);
    }

    @Override
    public int getItemCount() {
        return metaList.size();
    }

    public static class MetaViewHolder extends RecyclerView.ViewHolder {
        TextView txtObjetivo;
        TextView txtValores;
        TextView txtResponsavel;

        public MetaViewHolder(@NonNull View itemView) {
            super(itemView);
            txtObjetivo = itemView.findViewById(R.id.txtObjetivoMeta);
            txtValores = itemView.findViewById(R.id.txtValoresBaseEAlvo);
            txtResponsavel = itemView.findViewById(R.id.txtResponsavelMeta);
        }
    }
}