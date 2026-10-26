package br.com.acta.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import br.com.acta.R;

public class SugestaoChatAdapter extends RecyclerView.Adapter<SugestaoChatAdapter.ViewHolder> {
    public interface OnSugestaoClickListener {
        void onSugestaoClick(String sugestao);
    }

    private static final String[] TITULOS = {
            "Como está meu ciclo?",
            "Quais tarefas tenho hoje?",
            "Tenho algo atrasado?",
            "Quais treinamentos devo fazer?"
    };
    private static final String[] DESCRICOES = {
            "Veja o andamento atual",
            "Confira o que está com você",
            "Veja prazos pendentes",
            "Consulte os materiais"
    };

    private final OnSugestaoClickListener listener;

    public SugestaoChatAdapter(OnSugestaoClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_suggestion, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String titulo = TITULOS[position];
        holder.titulo.setText(titulo);
        holder.descricao.setText(DESCRICOES[position]);
        holder.itemView.setContentDescription(titulo + ". " + DESCRICOES[position]);
        holder.itemView.setOnClickListener(v -> listener.onSugestaoClick(titulo));
    }

    @Override
    public int getItemCount() {
        return TITULOS.length;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView titulo;
        private final TextView descricao;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            titulo = itemView.findViewById(R.id.suggestionTitle);
            descricao = itemView.findViewById(R.id.suggestionDescription);
        }
    }
}
