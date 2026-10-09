package br.com.acta.Adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import br.com.acta.CicloPdcaActivity;
import br.com.acta.Model.Tarefa;
import br.com.acta.R;

public class TarefaAdapter extends RecyclerView.Adapter<TarefaAdapter.TarefaViewHolder> {

    private final List<Tarefa> tarefaList;
    private final List<Tarefa> tarefaListFull;
    private boolean showButton = true;

    public TarefaAdapter(List<Tarefa> tarefaList) {
        this.tarefaList = tarefaList != null ? tarefaList : new ArrayList<>();
        this.tarefaListFull = new ArrayList<>(this.tarefaList);
        this.showButton = true;
    }

    public TarefaAdapter(List<Tarefa> tarefaList, boolean showButton) {
        this.tarefaList = tarefaList != null ? tarefaList : new ArrayList<>();
        this.tarefaListFull = new ArrayList<>(this.tarefaList);
        this.showButton = showButton;
    }

    @NonNull
    @Override
    public TarefaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_card_tarefa, parent, false);
        return new TarefaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TarefaViewHolder holder, int position) {
        Tarefa tarefa = tarefaList.get(position);

        holder.txtTitulo.setText(tarefa.getStatus() != null ? "Tarefa: " + tarefa.getStatus().name() : "Revisar permissões de acesso");

        if (tarefa.getPrioridade() != null) {
            holder.txtCiclo.setText("Prioridade: " + tarefa.getPrioridade().name());
        } else {
            holder.txtCiclo.setText("Prioridade: MEDIA");
        }

        if (tarefa.getDataFimPrevista() != null) {
            holder.txtPrazo.setText("Prazo: " + tarefa.getDataFimPrevista());
        } else {
            holder.txtPrazo.setText("Prazo: Amanhã");
        }

        if (tarefa.getStatus() != null) {
            holder.txtStatus.setText("Status: " + tarefa.getStatus().name());
        } else {
            holder.txtStatus.setText("Status: Em andamento");
        }

        if (!showButton) {
            holder.btnAcessarCiclo.setVisibility(View.GONE);
        } else {
            holder.btnAcessarCiclo.setVisibility(View.VISIBLE);
            holder.btnAcessarCiclo.setOnClickListener(v -> {
                Intent intent = new Intent(v.getContext(), CicloPdcaActivity.class);
                if (tarefa.getIdPlanoAcao() != null) {
                    intent.putExtra("idPlanoAcao", tarefa.getIdPlanoAcao());
                }
                v.getContext().startActivity(intent);
            });
        }
    }

    @Override
    public int getItemCount() {
        return tarefaList.size();
    }

    public void filtrar(String texto) {
        tarefaList.clear();
        if (texto == null || texto.trim().isEmpty()) {
            tarefaList.addAll(tarefaListFull);
        } else {
            String filtro = texto.toLowerCase().trim();
            for (Tarefa tarefa : tarefaListFull) {
                boolean match = false;
                if (tarefa.getStatus() != null && tarefa.getStatus().name().toLowerCase().contains(filtro)) {
                    match = true;
                }
                if (tarefa.getDataFimPrevista() != null && tarefa.getDataFimPrevista().toLowerCase().contains(filtro)) {
                    match = true;
                }
                if (tarefa.getPrioridade() != null && tarefa.getPrioridade().name().toLowerCase().contains(filtro)) {
                    match = true;
                }
                if (match) {
                    tarefaList.add(tarefa);
                }
            }
        }
        notifyDataSetChanged();
    }

    public static class TarefaViewHolder extends RecyclerView.ViewHolder {
        TextView txtTitulo;
        TextView txtCiclo;
        TextView txtPrazo;
        TextView txtStatus;
        View btnAcessarCiclo;

        public TarefaViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTitulo = itemView.findViewById(R.id.txtTituloTarefa);
            txtCiclo = itemView.findViewById(R.id.txtCicloTarefa);
            txtPrazo = itemView.findViewById(R.id.txtPrazoTarefa);
            txtStatus = itemView.findViewById(R.id.txtStatusTarefa);
            btnAcessarCiclo = itemView.findViewById(R.id.btnAcessarCicloTarefa);
        }
    }
}
