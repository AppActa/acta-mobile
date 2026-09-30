package br.com.acta;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;

import br.com.acta.Api.MeApi;
import br.com.acta.Api.UsuarioApi;
import br.com.acta.Auth.FirebaseTokenProvider;
import br.com.acta.Auth.TokenProvider;
import br.com.acta.Client.RepositoryCallback;
import br.com.acta.Client.RetrofitClient;
import br.com.acta.Model.Me;
import br.com.acta.Model.Usuario;
import br.com.acta.Services.MeService;
import br.com.acta.Services.UsuarioService;

public class InicioFragment extends Fragment {
    private final TokenProvider tokenProvider = new FirebaseTokenProvider(FirebaseAuth.getInstance());
    private final UsuarioApi usuarioApi = RetrofitClient.getInstance(tokenProvider).create(UsuarioApi.class);
    private final UsuarioService usuarioService = new UsuarioService(usuarioApi);
    private final MeApi meApi = RetrofitClient.getInstance(tokenProvider).create(MeApi.class);
    private final MeService meService = new MeService(meApi);

    private Long id;
    private TextView txtSaudacao;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_inicio, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ShapeableImageView imgPerfilHeader = view.findViewById(R.id.imgPerfilHeader);
        txtSaudacao = view.findViewById(R.id.txtSaudacao);

        carregarMe(imgPerfilHeader);

        if (imgPerfilHeader != null) {
            imgPerfilHeader.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), Perfil.class);
                startActivity(intent);
            });
        }
    }

    private void buscarFotoDoBanco(Long idUsuario, ImageView imgFotoPerfil) {
        if (!isAdded()) return;

        usuarioService.buscarUsuario(idUsuario, new RepositoryCallback<Usuario>() {
            @Override
            public void onSuccess(Usuario usuario) {
                if (!isAdded()) return;

                if (usuario != null && usuario.getFotoUrl() != null && !usuario.getFotoUrl().isEmpty()) {
                    Glide.with(InicioFragment.this)
                            .load(usuario.getFotoUrl())
                            .skipMemoryCache(true)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .placeholder(R.drawable.reicon_profile_filled)
                            .error(R.drawable.reicon_profile_filled)
                            .into(imgFotoPerfil);
                } else {
                    imgFotoPerfil.setImageResource(R.drawable.reicon_profile_filled);
                }
            }

            @Override
            public void onError(int statusCode, String message) {
                if (!isAdded()) return;
                imgFotoPerfil.setImageResource(R.drawable.ic_arrow_forward);
            }
        });
    }

    public void carregarMe(ImageView imgPerfilHeader) {
        meService.getMe(new RepositoryCallback<Me>() {
            @Override
            public void onSuccess(Me me) {
                if (!isAdded()) return;

                id = me.getIdUsuario();

                if (txtSaudacao != null && me.getNome() != null && !me.getNome().isEmpty()) {
                    txtSaudacao.setText("Bom dia, " + me.getNome() + "!");
                }

                buscarFotoDoBanco(id, imgPerfilHeader);
            }

            @Override
            public void onError(int code, String message) {
                if (!isAdded()) return;
                Toast.makeText(requireContext(), "Erro " + code + ": " + message, Toast.LENGTH_LONG).show();
            }
        });
    }
}
