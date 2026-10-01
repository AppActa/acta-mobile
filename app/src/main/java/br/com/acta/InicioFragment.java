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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

import br.com.acta.Adapter.CicloAdapter;
import br.com.acta.Api.CicloApi;
import br.com.acta.Api.MeApi;
import br.com.acta.Api.UsuarioApi;
import br.com.acta.Api.UsuarioCicloApi;
import br.com.acta.Auth.FirebaseTokenProvider;
import br.com.acta.Auth.TokenProvider;
import br.com.acta.Client.RepositoryCallback;
import br.com.acta.Client.RetrofitClient;
import br.com.acta.DAO.AppDatabase;
import br.com.acta.DAO.CicloDao;
import br.com.acta.DAO.MeDao;
import br.com.acta.DAO.UsuarioDao;
import br.com.acta.Model.Ciclo;
import br.com.acta.Model.Me;
import br.com.acta.Model.Usuario;
import br.com.acta.Model.UsuarioCiclo;
import br.com.acta.Services.CicloService;
import br.com.acta.Services.MeService;
import br.com.acta.Services.UsuarioCicloService;
import br.com.acta.Services.UsuarioService;

public class InicioFragment extends Fragment {
    private final TokenProvider tokenProvider = new FirebaseTokenProvider(FirebaseAuth.getInstance());
    private final UsuarioApi usuarioApi = RetrofitClient.getInstance(tokenProvider).create(UsuarioApi.class);
    private final UsuarioCicloApi usuarioCicloApi = RetrofitClient.getInstance(tokenProvider).create(UsuarioCicloApi.class);
    private final CicloApi cicloApi = RetrofitClient.getInstance(tokenProvider).create(CicloApi.class);
    private final UsuarioService usuarioService = new UsuarioService(usuarioApi);
    private final UsuarioCicloService usuarioCicloService = new UsuarioCicloService(usuarioCicloApi);
    private final CicloService cicloService = new CicloService(cicloApi);
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
        RecyclerView recyclerView = view.findViewById(R.id.rvMeusCiclos);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));

        ShapeableImageView imgPerfilHeader = view.findViewById(R.id.imgPerfilHeader);
        txtSaudacao = view.findViewById(R.id.txtSaudacao);

        // 1. CARREGAMENTO OFFLINE INSTANTÂNEO DO SQLITE LOCAL (Offline-First)
        carregarDadosSQLiteLocal(imgPerfilHeader, recyclerView);

        // 2. Chamar a API em segundo plano para atualizar
        carregarMe(imgPerfilHeader, recyclerView);

        if (imgPerfilHeader != null) {
            imgPerfilHeader.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), Perfil.class);
                startActivity(intent);
            });
        }
    }

    private void carregarDadosSQLiteLocal(ShapeableImageView imgPerfilHeader, RecyclerView recyclerView) {
        try {
            // A. Carregar Nome do Usuário do MeDao
            MeDao meDao = AppDatabase.getInstance(requireContext()).meDao();
            Me meLocal = meDao.buscarPrimeiro();
            if (meLocal != null && meLocal.getNome() != null && !meLocal.getNome().isEmpty()) {
                txtSaudacao.setText("Bom dia, " + meLocal.getNome() + "!");
            }

            // B. Carregar Foto do Perfil do UsuarioDao
            UsuarioDao usuarioDao = AppDatabase.getInstance(requireContext()).usuarioDao();
            Usuario userLocal = usuarioDao.buscarPrimeiro();
            if (userLocal != null && userLocal.getFotoUrl() != null && !userLocal.getFotoUrl().isEmpty() && imgPerfilHeader != null) {
                Glide.with(this)
                        .load(userLocal.getFotoUrl())
                        .placeholder(R.drawable.reicon_profile_filled)
                        .error(R.drawable.reicon_profile_filled)
                        .into(imgPerfilHeader);
            }

            CicloDao cicloDao = AppDatabase.getInstance(requireContext()).cicloDao();
            List<Ciclo> ciclosLocais = cicloDao.listarTodos();
            if (ciclosLocais != null && !ciclosLocais.isEmpty()) {
                recyclerView.setAdapter(new CicloAdapter(ciclosLocais));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void buscarUsuarioFotoECiclo(Long idUsuario, ImageView imgFotoPerfil, RecyclerView rvMeusCiclos) {
        if (!isAdded()) return;

        // 1. Busca foto do usuário na API e salva no SQLite
        usuarioService.buscarUsuario(idUsuario, new RepositoryCallback<Usuario>() {
            @Override
            public void onSuccess(Usuario usuario) {
                if (!isAdded()) return;

                if (usuario != null) {
                    // Salva no SQLite local
                    try {
                        AppDatabase.getInstance(requireContext()).usuarioDao().salvar(usuario);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    if (usuario.getFotoUrl() != null && !usuario.getFotoUrl().isEmpty()) {
                        Glide.with(InicioFragment.this)
                                .load(usuario.getFotoUrl())
                                .skipMemoryCache(true)
                                .diskCacheStrategy(DiskCacheStrategy.NONE)
                                .placeholder(R.drawable.reicon_profile_filled)
                                .error(R.drawable.reicon_profile_filled)
                                .into(imgFotoPerfil);
                    }
                }
            }

            @Override
            public void onError(int statusCode, String message) {
            }
        });

        // 2. Busca lista de UsuarioCiclo no endpoint GET usuario/ciclos-usuario/{idUsuario}
        usuarioService.buscarCiclosUsuario(idUsuario, new RepositoryCallback<List<UsuarioCiclo>>() {
            @Override
            public void onSuccess(List<UsuarioCiclo> listaUsuarioCiclos) {
                if (!isAdded()) return;

                if (listaUsuarioCiclos != null && !listaUsuarioCiclos.isEmpty()) {
                    buscarEDesalvarCadaCiclo(listaUsuarioCiclos, rvMeusCiclos);
                } else {
                    Toast.makeText(requireContext(), "Nenhum ciclo encontrado para o usuário", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(int code, String message) {
                if (!isAdded()) return;
                Toast.makeText(requireContext(), "Erro ao buscar ciclos: " + message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void buscarEDesalvarCadaCiclo(List<UsuarioCiclo> listaUsuarioCiclos, RecyclerView rvMeusCiclos) {
        if (!isAdded() || listaUsuarioCiclos == null || listaUsuarioCiclos.isEmpty()) return;

        List<Ciclo> listaCiclosCompleta = new ArrayList<>();
        int totalCiclos = listaUsuarioCiclos.size();
        int[] contador = {0};

        for (UsuarioCiclo uc : listaUsuarioCiclos) {
            Long idCiclo = uc.getIdCiclo();

            if (idCiclo != null) {
                // Busca os detalhes completos do Ciclo pelo idCiclo na rota GET ciclo/{id}
                cicloService.getCiclo(idCiclo, new RepositoryCallback<Ciclo>() {
                    @Override
                    public void onSuccess(Ciclo ciclo) {
                        if (!isAdded()) return;

                        if (ciclo != null) {
                            listaCiclosCompleta.add(ciclo);
                            // Salva o Ciclo individualmente no SQLite Local
                            try {
                                AppDatabase.getInstance(requireContext()).cicloDao().salvar(ciclo);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        contador[0]++;
                        if (contador[0] == totalCiclos) {
                            exibirCiclosNaTela(listaCiclosCompleta, rvMeusCiclos);
                        }
                    }

                    @Override
                    public void onError(int code, String message) {
                        if (!isAdded()) return;

                        contador[0]++;
                        if (contador[0] == totalCiclos) {
                            exibirCiclosNaTela(listaCiclosCompleta, rvMeusCiclos);
                        }
                    }
                });
            } else {
                contador[0]++;
                if (contador[0] == totalCiclos) {
                    exibirCiclosNaTela(listaCiclosCompleta, rvMeusCiclos);
                }
            }
        }
    }

    private void exibirCiclosNaTela(List<Ciclo> listaCiclos, RecyclerView rvMeusCiclos) {
        if (!isAdded()) return;

        if (!listaCiclos.isEmpty()) {
            CicloAdapter adapter = new CicloAdapter(listaCiclos);
            rvMeusCiclos.setAdapter(adapter);
            Toast.makeText(requireContext(), "Ciclos carregados da API: " + listaCiclos.size(), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(requireContext(), "Nenhum ciclo encontrado", Toast.LENGTH_SHORT).show();
        }
    }

    public void carregarMe(ImageView imgPerfilHeader, RecyclerView rvMeusCiclos) {
        meService.getMe(new RepositoryCallback<Me>() {
            @Override
            public void onSuccess(Me me) {
                if (!isAdded()) return;

                if (me != null) {
                    // Salva o Me no SQLite local
                    try {
                        AppDatabase.getInstance(requireContext()).meDao().salvar(me);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    id = me.getIdUsuario();

                    if (txtSaudacao != null && me.getNome() != null && !me.getNome().isEmpty()) {
                        txtSaudacao.setText("Bom dia, " + me.getNome() + "!");
                    }

                    buscarUsuarioFotoECiclo(id, imgPerfilHeader, rvMeusCiclos);
                }
            }

            @Override
            public void onError(int code, String message) {
                if (!isAdded()) return;
                Toast.makeText(requireContext(), "Erro " + code + ": " + message, Toast.LENGTH_LONG).show();
            }
        });
    }
}
