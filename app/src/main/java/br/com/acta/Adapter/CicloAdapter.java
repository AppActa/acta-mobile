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

import java.util.List;

import br.com.acta.Model.Ciclo;
import br.com.acta.R;

public class CicloAdapter extends RecyclerView.Adapter<CicloAdapter.CardViewHolder>{
    private List<Ciclo> cicloList;
    public CicloAdapter(List<Ciclo> cicloList){
        this.cicloList = cicloList;
    }

    @NonNull
    @Override
    public CicloAdapter.CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ciclo_card, parent, false);
        return new CardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CicloAdapter.CardViewHolder holder, int position) {
        Ciclo ciclo = cicloList.get(position);
        Glide.with(holder.imagemCiclo.getContext()).load(ciclo.getIconeUrl()).into(holder.imagemCiclo);
    }
    private void buscarFotoBanco(Long id){

    }

    @Override
    public int getItemCount() {
        return cicloList.size();
    }
    public static class CardViewHolder extends RecyclerView.ViewHolder{
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
