package br.com.acta.Adapter;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import br.com.acta.Model.Ciclo;

public class CicloAdapter extends RecyclerView.Adapter<CicloAdapter.CardViewHolder>{
    private List<Ciclo> cicloList;

    @NonNull
    @Override
    public CicloAdapter.CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull CicloAdapter.CardViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return cicloList.size();
    }
    public static class CardViewHolder extends RecyclerView.ViewHolder{

        public CardViewHolder(@NonNull View itemView) {
            super(itemView);

        }

    }
}
