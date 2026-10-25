package br.com.acta.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import br.com.acta.Model.ChatItem;
import br.com.acta.R;

public class ListaConversasAdapter extends RecyclerView.Adapter<ListaConversasAdapter.ViewHolder> {
    public interface OnConversaClickListener {
        void onConversaClick(ChatItem conversa);
    }

    private final List<ChatItem> conversas = new ArrayList<>();
    private final List<ChatItem> conversasVisiveis = new ArrayList<>();
    private final OnConversaClickListener listener;
    private String filtro = "";

    public ListaConversasAdapter(OnConversaClickListener listener) {
        this.listener = listener;
    }

    public void setConversas(List<ChatItem> itens) {
        conversas.clear();
        if (itens != null) conversas.addAll(itens);
        aplicarFiltro();
    }

    public void filtrar(String texto) {
        filtro = texto == null ? "" : texto.trim().toLowerCase(Locale.getDefault());
        aplicarFiltro();
    }

    private void aplicarFiltro() {
        conversasVisiveis.clear();
        for (ChatItem conversa : conversas) {
            String titulo = conversa.getTitulo() == null ? "Nova conversa" : conversa.getTitulo();
            if (titulo.toLowerCase(Locale.getDefault()).contains(filtro)) conversasVisiveis.add(conversa);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_conversation_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChatItem conversa = conversasVisiveis.get(position);
        holder.titulo.setText(conversa.getTitulo() == null ? "Nova conversa" : conversa.getTitulo());
        holder.itemView.setOnClickListener(v -> listener.onConversaClick(conversa));
    }

    @Override
    public int getItemCount() {
        return conversasVisiveis.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView titulo;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            titulo = itemView.findViewById(R.id.historyConversationTitle);
        }
    }
}
