package br.com.acta.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

import br.com.acta.Api.CicloApi;
import br.com.acta.Auth.FirebaseTokenProvider;
import br.com.acta.Auth.TokenProvider;
import br.com.acta.Client.RetrofitClient;
import br.com.acta.Model.Ciclo;
import br.com.acta.R;

public class CicloAdapter extends RecyclerView.Adapter<CicloAdapter.CardViewHolder> {
    private final List<Ciclo> cicloList;
    private final List<Ciclo> cicloListFull;

    public CicloAdapter(List<Ciclo> cicloList) {
        this.cicloList = cicloList;
        this.cicloListFull = new ArrayList<>(cicloList != null ? cicloList : new ArrayList<>());
    }

    private final TokenProvider tokenProvider = new FirebaseTokenProvider(FirebaseAuth.getInstance());
    private final CicloApi cicloApi = RetrofitClient.getInstance(tokenProvider).create(CicloApi.class);

    @NonNull
    @Override
    public CicloAdapter.CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ciclo_card, parent, false);
        return new CardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CicloAdapter.CardViewHolder holder, int position) {
        Ciclo ciclo = cicloList.get(position);
        if (ciclo.getIconeUrl() != null && !ciclo.getIconeUrl().isEmpty()) {
            Glide.with(holder.imagemCiclo.getContext())
                    .load(ciclo.getIconeUrl())
                    .placeholder(R.drawable.reicon_profile_filled)
                    .error(R.drawable.logo_azul)
                    .into(holder.imagemCiclo);
        } else {
            holder.imagemCiclo.setImageResource(R.drawable.ic_arrow_forward);
        }
        holder.titulo.setText(ciclo.getTitulo());
        holder.status.setText("Status:" + ciclo.getStatus());
        holder.card.setOnClickListener(v -> {

        });
    }

    public void filtrar(String texto) {
        cicloList.clear(); // 1. Limpa a lista visível da tela

        if (texto == null || texto.trim().isEmpty()) {
            // 2. Se a busca estiver vazia, restaura a lista completa original
            cicloList.addAll(cicloListFull);
        } else {
            String filtro = texto.toLowerCase().trim();
            // 3. Percorre todos os ciclos verificando se o título contém o texto digitado
            for (Ciclo ciclo : cicloListFull) {
                if (ciclo.getTitulo() != null && ciclo.getTitulo().toLowerCase().contains(filtro)) {
                    cicloList.add(ciclo); // Adiciona na lista filtrada
                }
            }
        }

        notifyDataSetChanged(); // 4. Atualiza o RecyclerView instantaneamente!
    }

    @Override
    public int getItemCount() {
        return cicloList.size();
    }

    public static class CardViewHolder extends RecyclerView.ViewHolder {
        ImageView imagemCiclo;
        TextView titulo;
        TextView status;
        ConstraintLayout card;

        public CardViewHolder(@NonNull View itemView) {
            super(itemView);
            imagemCiclo = itemView.findViewById(R.id.imgIconeCiclo);
            titulo = itemView.findViewById(R.id.txtTituloCiclo);
            status = itemView.findViewById(R.id.txtStatusCiclo);
            card = itemView.findViewById(R.id.card);
        }
    }
}
