package br.com.acta.Adapter;

import android.graphics.Color;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import br.com.acta.Api.TarefaApi;
import br.com.acta.Auth.FirebaseTokenProvider;
import br.com.acta.Auth.TokenProvider;
import br.com.acta.Client.RepositoryCallback;
import br.com.acta.Client.RetrofitClient;
import br.com.acta.Model.Enum.StatusTarefa;
import br.com.acta.Model.Tarefa;
import br.com.acta.Model.TarefaStatusUpdateRequest;
import br.com.acta.R;
import br.com.acta.utils.FormatoData;
import br.com.acta.Services.TarefaService;

public class TarefaAdapter extends RecyclerView.Adapter<TarefaAdapter.TarefaViewHolder> {

    private final TokenProvider tokenProvider = new FirebaseTokenProvider(FirebaseAuth.getInstance());
    private final TarefaApi tarefaApi = RetrofitClient.getInstance(tokenProvider).create(TarefaApi.class);
    private final TarefaService tarefaService = new TarefaService(tarefaApi);

    private final List<Tarefa> tarefaList;
    private final List<Tarefa> tarefaListFull;
    private String textoFiltro = "";
    private StatusTarefa statusFiltro;
    private boolean showButton = true;

    public TarefaAdapter(List<Tarefa> tarefaList) {
        this.tarefaList = tarefaList != null ? new ArrayList<>(tarefaList) : new ArrayList<>();
        this.tarefaListFull = new ArrayList<>(this.tarefaList);
        this.showButton = true;
    }

    public TarefaAdapter(List<Tarefa> tarefaList, boolean showButton) {
        this.tarefaList = tarefaList != null ? new ArrayList<>(tarefaList) : new ArrayList<>();
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

        holder.txtTitulo.setText(tarefa.getTitulo() != null ? tarefa.getTitulo() : "Organizar materiais");
        holder.txtObjetivo.setText(tarefa.getDescricao() != null ? tarefa.getDescricao() : "Separar materiais por categoria e atualizar o inventário.");

        if (tarefa.getPrioridade() != null) {
            holder.txtCiclo.setText("Prioridade: " + tarefa.getPrioridade().name());
        } else {
            holder.txtCiclo.setText("Prioridade: MEDIA");
        }

        if (tarefa.getDataFimPrevista() != null) {
            holder.txtPrazo.setText("Prazo: " + FormatoData.formatar(holder.itemView.getContext(), tarefa.getDataFimPrevista()));
        } else {
            holder.txtPrazo.setText("Prazo: —");
        }

        if (tarefa.getStatus() != null) {
            holder.txtStatus.setText("Status: " + nomeStatus(tarefa.getStatus()));
        } else {
            holder.txtStatus.setText("Status: Em andamento");
        }

        boolean isConcluida = tarefa.getStatus() == StatusTarefa.CONCLUIDA;

        if (isConcluida) {
            holder.txtStatusPill.setText("Concluída");
            holder.txtStatusPill.setBackgroundResource(R.drawable.bg_status_pill_dark);

            holder.btnConcluirAction.setEnabled(false);
            holder.btnConcluirAction.setClickable(false);
            holder.btnConcluirAction.setText("Concluída");
            holder.btnConcluirAction.setTextColor(Color.parseColor("#757575"));
            holder.btnConcluirAction.setBackgroundColor(Color.parseColor("#D6D6D6"));
        } else {
            holder.txtStatusPill.setText(nomeStatus(tarefa.getStatus()));
            holder.txtStatusPill.setBackgroundResource(R.drawable.bg_status_pill);

            holder.btnConcluirAction.setEnabled(true);
            holder.btnConcluirAction.setClickable(true);
            holder.btnConcluirAction.setText("Concluída");
            holder.btnConcluirAction.setTextColor(Color.parseColor("#00A7B5"));
            holder.btnConcluirAction.setBackgroundResource(R.drawable.bg_card_white);

            holder.btnConcluirAction.setOnClickListener(v -> {
                if (tarefa.getId() == null) return;

                String hoje = LocalDate.now().toString();
                String dataInicio = tarefa.getDataInicioReal() != null && !tarefa.getDataInicioReal().trim().isEmpty()
                        ? tarefa.getDataInicioReal()
                        : hoje;

                TarefaStatusUpdateRequest request = new TarefaStatusUpdateRequest(dataInicio, hoje, StatusTarefa.CONCLUIDA);

                holder.btnConcluirAction.setEnabled(false);

                tarefaService.patchStatus(tarefa.getId(), request, new RepositoryCallback<Tarefa>() {
                    @Override
                    public void onSuccess(Tarefa tarefaAtualizada) {
                        tarefa.setStatus(StatusTarefa.CONCLUIDA);
                        aplicarFiltros();
                        Toast.makeText(v.getContext(), "Tarefa concluída!", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(int code, String message) {
                        holder.btnConcluirAction.setEnabled(true);
                        Toast.makeText(v.getContext(), "Erro ao concluir tarefa: " + message, Toast.LENGTH_SHORT).show();
                    }
                });
            });
        }

        // Reseta visibilidade ao reciclar
        holder.containerActionConcluir.setVisibility(View.GONE);
        holder.txtSetinhaExpand.setText("«");
        holder.txtCiclo.setVisibility(View.VISIBLE);
        holder.txtPrazo.setVisibility(View.VISIBLE);
        holder.txtStatus.setVisibility(View.VISIBLE);

        holder.btnToggleExpand.setOnClickListener(v -> {
            TransitionManager.beginDelayedTransition((ViewGroup) holder.itemView, new AutoTransition());
            if (holder.containerActionConcluir.getVisibility() == View.VISIBLE) {
                // FECHAR PAINEL
                holder.containerActionConcluir.setVisibility(View.GONE);
                holder.txtSetinhaExpand.setText("«");

                holder.txtCiclo.setVisibility(View.VISIBLE);
                holder.txtPrazo.setVisibility(View.VISIBLE);
                holder.txtStatus.setVisibility(View.VISIBLE);
            } else {
                // ABRIR PAINEL (Oculta prioridade, prazo e status para manter altura do card)
                holder.containerActionConcluir.setVisibility(View.VISIBLE);
                holder.txtSetinhaExpand.setText("»");

                holder.txtCiclo.setVisibility(View.GONE);
                holder.txtPrazo.setVisibility(View.GONE);
                holder.txtStatus.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public int getItemCount() {
        return tarefaList.size();
    }

    public void filtrar(String texto) {
        textoFiltro = texto == null ? "" : texto.trim().toLowerCase(Locale.ROOT);
        aplicarFiltros();
    }

    public void filtrarPorStatus(StatusTarefa status) {
        statusFiltro = status;
        aplicarFiltros();
    }

    public static String nomeStatus(StatusTarefa status) {
        if (status == null) {
            return "Em andamento";
        }
        switch (status) {
            case PENDENTE:
                return "Pendente";
            case EM_ANDAMENTO:
                return "Em andamento";
            case BLOQUEADA:
                return "Bloqueada";
            case CONCLUIDA:
                return "Concluída";
            case ATRASADA:
                return "Atrasada";
            case CANCELADA:
                return "Cancelada";
            default:
                return "Em andamento";
        }
    }

    private void aplicarFiltros() {
        tarefaList.clear();
        for (Tarefa tarefa : tarefaListFull) {
            StatusTarefa status = tarefa.getStatus() == null ? StatusTarefa.EM_ANDAMENTO : tarefa.getStatus();
            boolean statusCorresponde = statusFiltro == null || status == statusFiltro;
            boolean textoCorresponde = textoFiltro.isEmpty()
                    || (tarefa.getTitulo() != null && tarefa.getTitulo().toLowerCase(Locale.ROOT).contains(textoFiltro))
                    || (tarefa.getDescricao() != null && tarefa.getDescricao().toLowerCase(Locale.ROOT).contains(textoFiltro))
                    || nomeStatus(status).toLowerCase(Locale.ROOT).contains(textoFiltro)
                    || status.name().toLowerCase(Locale.ROOT).contains(textoFiltro);
            if (statusCorresponde && textoCorresponde) {
                tarefaList.add(tarefa);
            }
        }
        notifyDataSetChanged();
    }

    public static class TarefaViewHolder extends RecyclerView.ViewHolder {
        TextView txtTitulo;
        TextView txtCiclo;
        TextView txtPrazo;
        TextView txtStatus;
        TextView txtStatusPill;
        TextView txtObjetivo;
        View containerActionConcluir;
        View btnToggleExpand;
        TextView txtSetinhaExpand;
        Button btnConcluirAction;

        public TarefaViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTitulo = itemView.findViewById(R.id.txtTituloTarefa);
            txtCiclo = itemView.findViewById(R.id.txtCicloTarefa);
            txtPrazo = itemView.findViewById(R.id.txtPrazoTarefa);
            txtStatus = itemView.findViewById(R.id.txtStatusTarefa);
            txtStatusPill = itemView.findViewById(R.id.txtStatusPillTarefa);
            txtObjetivo = itemView.findViewById(R.id.txtObjetivoTarefa);
            containerActionConcluir = itemView.findViewById(R.id.containerActionConcluir);
            btnToggleExpand = itemView.findViewById(R.id.btnToggleExpand);
            txtSetinhaExpand = itemView.findViewById(R.id.txtSetinhaExpand);
            btnConcluirAction = itemView.findViewById(R.id.btnConcluirTarefaAction);
        }
    }
}
