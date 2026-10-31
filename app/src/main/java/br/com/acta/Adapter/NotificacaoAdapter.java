package br.com.acta.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import br.com.acta.Model.Notificacao;
import br.com.acta.R;
import br.com.acta.utils.FormatoData;

public class NotificacaoAdapter extends ArrayAdapter<Notificacao> {
    public NotificacaoAdapter(@NonNull Context context, @NonNull List<Notificacao> notificacoes) {
        super(context, 0, notificacoes);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ItemViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_notificacao, parent, false);
            holder = new ItemViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ItemViewHolder) convertView.getTag();
        }

        Notificacao notificacao = getItem(position);
        if (notificacao == null) {
            return convertView;
        }

        boolean naoLida = notificacao.lidoEm == null;
        holder.status.setSelected(naoLida);
        if (naoLida) {
            holder.status.setText("Não lida");
            holder.status.setVisibility(View.VISIBLE);
        } else {
            holder.status.setVisibility(View.GONE);
        }
        holder.titulo.setText(formatarTexto(notificacao.tipo));

        String categoria = formatarTexto(notificacao.categoria);
        String dataCriacao = FormatoData.formatar(getContext(), notificacao.criadoEm);
        holder.categoriaData.setText(categoria.isEmpty() ? dataCriacao : categoria + " · " + dataCriacao);

        if (notificacao.expiraEm == null) {
            holder.expiracao.setVisibility(View.GONE);
            holder.expiracao.setText("");
        } else {
            holder.expiracao.setText("Expira em: " + FormatoData.formatar(getContext(), notificacao.expiraEm));
            holder.expiracao.setVisibility(View.VISIBLE);
        }

        return convertView;
    }

    private String formatarTexto(String valor) {
        return valor == null ? "" : valor.replace('_', ' ');
    }

    private static class ItemViewHolder {
        private final TextView status;
        private final TextView titulo;
        private final TextView categoriaData;
        private final TextView expiracao;

        private ItemViewHolder(View itemView) {
            status = itemView.findViewById(R.id.statusNotificacaoItem);
            titulo = itemView.findViewById(R.id.tituloNotificacaoItem);
            categoriaData = itemView.findViewById(R.id.categoriaDataNotificacaoItem);
            expiracao = itemView.findViewById(R.id.expiracaoNotificacaoItem);
        }
    }
}
