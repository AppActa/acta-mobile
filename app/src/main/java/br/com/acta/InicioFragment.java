package br.com.acta;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;

import br.com.acta.Api.UsuarioApi;
import br.com.acta.Auth.FirebaseTokenProvider;
import br.com.acta.Auth.TokenProvider;
import br.com.acta.Client.RepositoryCallback;
import br.com.acta.Client.RetrofitClient;
import br.com.acta.Model.Usuario;
import br.com.acta.Services.UsuarioService;

public class InicioFragment extends Fragment {
    TokenProvider tokenProvider = new FirebaseTokenProvider(FirebaseAuth.getInstance());
    private UsuarioApi usuarioApi = RetrofitClient.getInstance(tokenProvider).create(UsuarioApi.class);
    private UsuarioService usuarioService = new UsuarioService(usuarioApi);
    ImageView imgFotoPerfil;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_inicio, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ShapeableImageView imgPerfilHeader = view.findViewById(R.id.imgPerfilHeader);
        if (imgPerfilHeader != null) {
            imgPerfilHeader.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), Perfil.class);
                startActivity(intent);
            });
        }

    }
    private void buscarFotoDoBanco(Long idUsuario,@NonNull View view){
        usuarioService.buscarUsuario(idUsuario, new RepositoryCallback<Usuario>() {
            @Override
            public void onSuccess(Usuario usuario) {
                imgFotoPerfil = view.findViewById(R.id.imgPerfilHeader);
                // Verifica se o usuário e a URL da foto não são nulos
                if (usuario != null && usuario.getFotoUrl() != null && !usuario.getFotoUrl().isEmpty()) {
                    Glide.with(InicioFragment.this)
                            .load(usuario.getFotoUrl())
                            .skipMemoryCache(true) // Ignora cache em memória
                            .diskCacheStrategy(DiskCacheStrategy.NONE) // Força baixar a imagem atualizada
                            .placeholder(R.drawable.reicon_profile_filled)
                            .error(R.drawable.reicon_profile_filled)
                            .into(imgFotoPerfil);

                } else {
                    // Caso o usuário não tenha foto cadastrada, exibe a imagem padrão
                    imgFotoPerfil.setImageResource(R.drawable.reicon_profile_filled);
                }
            }

            @Override
            public void onError(int statusCode, String message) {
                imgFotoPerfil.setImageResource(R.drawable.ic_arrow_forward);
            }
        });
    }
}
