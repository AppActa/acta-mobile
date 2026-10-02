package br.com.acta.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

import br.com.acta.Api.CicloApi;
import br.com.acta.Api.UsuarioApi;
import br.com.acta.Auth.FirebaseTokenProvider;
import br.com.acta.Auth.TokenProvider;
import br.com.acta.Client.RepositoryCallback;
import br.com.acta.Client.RetrofitClient;
import br.com.acta.InicioFragment;
import br.com.acta.Model.Ciclo;
import br.com.acta.Model.Usuario;
import br.com.acta.Perfil;
import br.com.acta.R;
import br.com.acta.Services.CicloService;

public class CicloAdapter extends RecyclerView.Adapter<CicloAdapter.CardViewHolder>{
    private List<Ciclo> cicloList;
    public CicloAdapter(List<Ciclo> cicloList){
        this.cicloList = cicloList;
    }
    TokenProvider tokenProvider = new FirebaseTokenProvider(FirebaseAuth.getInstance());
    private CicloApi cicloApi = RetrofitClient.getInstance(tokenProvider).create(CicloApi.class);
    private CicloService cicloService = new CicloService(cicloApi);

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
                    .error(R.drawable.reicon_profile_filled)
                    .into(holder.imagemCiclo);
        } else {
            holder.imagemCiclo.setImageResource(R.drawable.ic_arrow_forward);
        }
        holder.titulo.setText(ciclo.getTitulo());
        holder.status.setText("Status:"+ ciclo.getStatus());
        holder.card.setOnClickListener(v->{

        });
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
