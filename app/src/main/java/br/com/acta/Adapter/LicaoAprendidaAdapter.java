package br.com.acta.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import br.com.acta.Model.LicaoAprendida;
import br.com.acta.R;

public class LicaoAprendidaAdapter extends RecyclerView.Adapter<LicaoAprendidaAdapter.LicaoViewHolder> {

    private final List<LicaoAprendida> licaoList;

    public LicaoAprendidaAdapter(List<LicaoAprendida> licaoList) {
        this.licaoList = licaoList != null ? licaoList : new ArrayList<>();
    }

    @NonNull
    @Override
    public LicaoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_card_licao_aprendida, parent, false);
        return new LicaoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LicaoViewHolder holder, int position) {
        LicaoAprendida licao = licaoList.get(position);

        holder.txtTitulo.setText(licao.getTitulo() != null ? licao.getTitulo() : "Conferência dupla de pedidos");

        if (licao.getLicao() != null && !licao.getLicao().trim().isEmpty()) {
            holder.txtConteudo.setText(licao.getLicao());
            holder.txtConteudo.setVisibility(View.VISIBLE);
        } else {
            holder.txtConteudo.setVisibility(View.GONE);
        }

        String resultadoStr = licao.getResultado() != null ? "Resultado: " + licao.getResultado() : "Resultado: identificação mais rápida";
        String areaStr = licao.getArea() != null ? " • Área: " + licao.getArea() : " • Área: Estoque";

        holder.txtRodape.setText(resultadoStr + areaStr);
    }

    @Override
    public int getItemCount() {
        return licaoList.size();
    }

    public static class LicaoViewHolder extends RecyclerView.ViewHolder {
        TextView txtTitulo;
        TextView txtConteudo;
        TextView txtRodape;

        public LicaoViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTitulo = itemView.findViewById(R.id.txtTituloLicao);
            txtConteudo = itemView.findViewById(R.id.txtConteudoLicao);
            txtRodape = itemView.findViewById(R.id.txtRodapeLicao);
        }
    }
}
