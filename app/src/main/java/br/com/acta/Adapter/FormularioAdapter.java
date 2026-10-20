package br.com.acta.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import br.com.acta.Model.Formulario;
import br.com.acta.R;

public class FormularioAdapter extends RecyclerView.Adapter<FormularioAdapter.FormularioViewHolder> {

    private final List<Formulario> formularioList;

    public interface OnFormularioClickListener {
        void onFormularioClick(Formulario formulario);
    }

    private final OnFormularioClickListener listener;

    public FormularioAdapter(List<Formulario> formularioList, OnFormularioClickListener listener) {
        this.formularioList = formularioList != null ? formularioList : new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public FormularioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_card_formulario, parent, false);
        return new FormularioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FormularioViewHolder holder, int position) {
        Formulario formulario = formularioList.get(position);

        if (holder.txtTitulo != null) {
            holder.txtTitulo.setText(formulario.getTitulo() != null ? formulario.getTitulo() : "");
        }

        if (holder.txtSubtitulo != null) {
            int qtdPerguntas = formulario.getPerguntas() != null ? formulario.getPerguntas().size() : 0;
            String tipoStr = formulario.getTipo() != null ? formulario.getTipo().name() : "";
            if (!tipoStr.isEmpty()) {
                holder.txtSubtitulo.setText(tipoStr + " · " + qtdPerguntas + " pergunta" + (qtdPerguntas == 1 ? "" : "s"));
            } else {
                holder.txtSubtitulo.setText(qtdPerguntas + " pergunta" + (qtdPerguntas == 1 ? "" : "s"));
            }
        }

        if (holder.txtDescricao != null) {
            if (formulario.getDescricao() != null && !formulario.getDescricao().trim().isEmpty()) {
                holder.txtDescricao.setText(formulario.getDescricao());
                holder.txtDescricao.setVisibility(View.VISIBLE);
            } else {
                holder.txtDescricao.setVisibility(View.GONE);
            }
        }

        if (holder.txtStatusBadge != null) {
            if (formulario.getStatus() != null) {
                holder.txtStatusBadge.setText(formulario.getStatus().name());
                holder.txtStatusBadge.setVisibility(View.VISIBLE);
            } else {
                holder.txtStatusBadge.setVisibility(View.GONE);
            }
        }

        View.OnClickListener clickListener = v -> {
            if (listener != null) {
                listener.onFormularioClick(formulario);
            }
        };

        holder.itemView.setOnClickListener(clickListener);
        if (holder.btnResponder != null) {
            holder.btnResponder.setOnClickListener(clickListener);
        }
    }

    @Override
    public int getItemCount() {
        return formularioList.size();
    }

    public static class FormularioViewHolder extends RecyclerView.ViewHolder {
        TextView txtTitulo;
        TextView txtSubtitulo;
        TextView txtDescricao;
        TextView txtStatusBadge;
        View btnResponder;

        public FormularioViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTitulo = itemView.findViewById(R.id.txtTituloFormularioCard);
            txtSubtitulo = itemView.findViewById(R.id.txtSubtituloFormularioCard);
            txtDescricao = itemView.findViewById(R.id.txtDescricaoFormularioCard);
            txtStatusBadge = itemView.findViewById(R.id.txtStatusBadgeFormularioCard);
            btnResponder = itemView.findViewById(R.id.btnResponderFormularioCard);
        }
    }
}
