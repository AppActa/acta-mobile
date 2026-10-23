package br.com.acta.Adapter;

import android.text.Html;
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

        if (holder.txtStatusBadge != null) {
            String status = licao.getSeveridade() != null ? licao.getSeveridade() : "Pendente";
            holder.txtStatusBadge.setText(status);
        }

        if (holder.txtTitulo != null) {
            holder.txtTitulo.setText(licao.getTitulo() != null ? licao.getTitulo() : "Conferência dupla de pedidos");
        }

        if (holder.txtResponsavel != null) {
            String respNome = licao.getArea() != null ? licao.getArea() : "Camila Souza";
            String htmlResp = "<font color='#757575'>Responsável: </font><font color='#111111'><b>" + respNome + "</b></font>";
            holder.txtResponsavel.setText(Html.fromHtml(htmlResp, Html.FROM_HTML_MODE_LEGACY));
        }

        if (holder.txtCiclo != null) {
            String cicloNome = licao.getLicao() != null ? licao.getLicao() : "Reduzir retrabalho na separação";
            String htmlCiclo = "<font color='#757575'>Ciclo: </font><font color='#111111'><b>" + cicloNome + "</b></font>";
            holder.txtCiclo.setText(Html.fromHtml(htmlCiclo, Html.FROM_HTML_MODE_LEGACY));
        }
    }

    @Override
    public int getItemCount() {
        return licaoList.size();
    }

    public static class LicaoViewHolder extends RecyclerView.ViewHolder {
        TextView txtStatusBadge;
        TextView txtTitulo;
        TextView txtResponsavel;
        TextView txtCiclo;

        public LicaoViewHolder(@NonNull View itemView) {
            super(itemView);
            txtStatusBadge = itemView.findViewById(R.id.txtStatusBadgeLicao);
            txtTitulo = itemView.findViewById(R.id.txtTituloLicao);
            txtResponsavel = itemView.findViewById(R.id.txtResponsavelLicao);
            txtCiclo = itemView.findViewById(R.id.txtCicloLicao);
        }
    }
}
