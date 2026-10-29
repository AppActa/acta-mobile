package br.com.acta;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

import br.com.acta.Adapter.TreinamentoAdapter;
import br.com.acta.Api.CicloApi;
import br.com.acta.Api.MeApi;
import br.com.acta.Api.TreinamentoApi;
import br.com.acta.Api.UsuarioTreinamentoApi;
import br.com.acta.Auth.FirebaseTokenProvider;
import br.com.acta.Auth.TokenProvider;
import br.com.acta.Client.RepositoryCallback;
import br.com.acta.Client.RetrofitClient;
import br.com.acta.Client.RetrofitClientAuth;
import br.com.acta.Model.Ciclo;
import br.com.acta.Model.Enum.StatusTreinamento;
import br.com.acta.Model.Me;
import br.com.acta.Model.Treinamento;
import br.com.acta.Model.UsuarioTreinamento;
import br.com.acta.Services.CicloService;
import br.com.acta.Services.MeService;
import br.com.acta.Services.TreinamentoService;
import br.com.acta.Services.UsuarioTreinamentoService;

public class ListaTreinamentosFragment extends Fragment {
    private static final String TAG = "TreinamentoFluxo";
    private final TokenProvider tokenProvider = new FirebaseTokenProvider(FirebaseAuth.getInstance());
    private final MeService meService = new MeService(RetrofitClientAuth.getInstance(tokenProvider).create(MeApi.class));
    private final CicloService cicloService = new CicloService(RetrofitClient.getInstance(tokenProvider).create(CicloApi.class));
    private final TreinamentoService treinamentoService = new TreinamentoService(RetrofitClient.getInstance(tokenProvider).create(TreinamentoApi.class));
    private final UsuarioTreinamentoService usuarioTreinamentoService = new UsuarioTreinamentoService(RetrofitClient.getInstance(tokenProvider).create(UsuarioTreinamentoApi.class));

    private RecyclerView listaTreinamentos;
    private TextView estadoLista;
    private TextView subtitulo;
    private final List<Treinamento> treinamentos = new ArrayList<>();
    private final List<UsuarioTreinamento> vinculos = new ArrayList<>();
    private int chamadasPendentes;
    private Long idCiclo;
    private Long idUsuario;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_lista_treinamentos, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ImageButton voltar = view.findViewById(R.id.btnVoltarListaTreinamentos);
        voltar.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());
        listaTreinamentos = view.findViewById(R.id.rvListaTreinamentos);
        listaTreinamentos.setLayoutManager(new LinearLayoutManager(requireContext()));
        estadoLista = view.findViewById(R.id.txtEstadoListaTreinamentos);
        subtitulo = view.findViewById(R.id.txtSubtituloListaTreinamentos);
        idCiclo = getArguments() != null ? getArguments().getLong("idCiclo", 0L) : 0L;

        if (idCiclo == 0L) {
            mostrarEstado("Não foi possível identificar o ciclo.");
            return;
        }

        carregarCiclo();
        carregarUsuario();
    }

    private void carregarCiclo() {
        cicloService.getCiclo(idCiclo, new RepositoryCallback<>() {
            @Override
            public void onSuccess(Ciclo ciclo) {
                if (!isAdded() || ciclo == null || subtitulo == null) return;
                subtitulo.setText(ciclo.getTitulo() != null ? "Ciclo: " + ciclo.getTitulo() : "Treinamentos do ciclo");
            }

            @Override
            public void onError(int code, String message) {
                if (isAdded()) mostrarErro("Não foi possível carregar o ciclo.");
            }
        });
    }

    private void carregarUsuario() {
        mostrarEstado("Carregando treinamentos...");
        meService.getMe(new RepositoryCallback<>() {
            @Override
            public void onSuccess(Me me) {
                if (!isAdded()) return;
                if (me == null || me.getIdUsuario() == null) {
                    mostrarErro("Não foi possível identificar seu usuário.");
                    return;
                }
                idUsuario = me.getIdUsuario();
                carregarTreinamentosDoCiclo();
            }

            @Override
            public void onError(int code, String message) {
                if (isAdded()) mostrarErro("Não foi possível carregar seus treinamentos. usuario");
            }
        });
    }

    private void carregarTreinamentosDoCiclo() {
        treinamentoService.buscarTreinamentos(idCiclo, new RepositoryCallback<>() {
            @Override
            public void onSuccess(List<Treinamento> resultado) {
                if (!isAdded()) return;
                Log.d(TAG, "Treinamentos recebidos do ciclo: " + (resultado == null ? "null" : resultado.size()));
                if (resultado == null || resultado.isEmpty()) {
                    mostrarEstado("Você ainda não tem treinamentos neste ciclo.");
                    return;
                }
                chamadasPendentes = resultado.size();
                for (Treinamento treinamento : resultado) {
                    if (treinamento == null || treinamento.getId() == null) {
                        Log.w(TAG, "Treinamento descartado: objeto ou id nulo");
                        finalizarChamada();
                        continue;
                    }
                    Log.d(TAG, "Consultando participantes do treinamento; id presente: true");
                    carregarVinculoUsuario(treinamento);
                }
            }

            @Override
            public void onError(int code, String message) {
                if (isAdded()) mostrarErro("Não foi possível carregar seus treinamentos., treinamento  " + code +" "+ message);
            }
        });
    }

    private void carregarVinculoUsuario(Treinamento treinamento) {
        usuarioTreinamentoService.buscar(treinamento.getId(), new RepositoryCallback<>() {
            @Override
            public void onSuccess(List<UsuarioTreinamento> resultado) {
                if (!isAdded()) return;
                Log.d(TAG, "Vínculos recebidos: " + (resultado == null ? "null" : resultado.size()));
                if (resultado != null) {
                    for (UsuarioTreinamento vinculo : resultado) {
                        boolean usuarioCorrespondente = vinculo != null && vinculo.getIdUsuario() != null && vinculo.getIdUsuario().equals(idUsuario);
                        Log.d(TAG, "Vínculo: objeto=" + (vinculo != null)
                                + ", idUsuario presente=" + (vinculo != null && vinculo.getIdUsuario() != null)
                                + ", idTreinamento presente=" + (vinculo != null && vinculo.getIdTreinamento() != null)
                                + ", corresponde ao usuário atual=" + usuarioCorrespondente);
                        if (usuarioCorrespondente) {
                            exibirVinculoDoUsuario(treinamento, vinculo);
                            return;
                        }
                    }
                }
                Log.w(TAG, "Treinamento descartado: nenhum vínculo corresponde ao usuário atual");
                finalizarChamada();
            }

            @Override
            public void onError(int code, String message) {
                if (!isAdded()) return;
                Log.e(TAG, "Falha ao carregar vínculos; HTTP/código=" + code);
                finalizarChamada();
            }
        });
    }

    private void exibirVinculoDoUsuario(Treinamento treinamento, UsuarioTreinamento vinculo) {
        vinculo.setIdTreinamento(treinamento.getId());
        treinamentos.add(treinamento);
        vinculos.add(vinculo);
        Log.d(TAG, "Treinamento adicionado à lista de exibição. Total: " + treinamentos.size());
        finalizarChamada();
    }

    private void buscarTreinamentoDoVinculo(UsuarioTreinamento vinculo) {
        if (vinculo.getIdTreinamento() == null) {
            finalizarChamada();
            return;
        }
        treinamentoService.buscar(vinculo.getIdTreinamento(), new RepositoryCallback<>() {
            @Override
            public void onSuccess(Treinamento treinamento) {
                if (!isAdded()) return;
                if (treinamento != null) {
                    treinamentos.add(treinamento);
                    vinculos.add(vinculo);
                    Log.d(TAG, "Treinamento adicionado às listas de exibição. Total: " + treinamentos.size());
                } else {
                    Log.w(TAG, "Treinamento descartado: busca por id retornou null");
                }
                finalizarChamada();
            }

            @Override
            public void onError(int code, String message) {
                if (isAdded()) {
                    Log.e(TAG, "Falha ao buscar treinamento individual; HTTP/código=" + code);
                    finalizarChamada();
                }
            }
        });
    }

    private void finalizarChamada() {
        chamadasPendentes--;
        if (chamadasPendentes <= 0) exibirTreinamentos();
    }

    private void exibirTreinamentos() {
        if (!isAdded() || listaTreinamentos == null) return;
        Log.d(TAG, "Exibindo treinamentos: " + treinamentos.size() + ", vínculos: " + vinculos.size());
        listaTreinamentos.setAdapter(new TreinamentoAdapter(treinamentos, vinculos, this::atualizarStatusTreinamento));
        if (treinamentos.isEmpty()) {
            mostrarEstado("Você ainda não tem treinamentos neste ciclo. exibir errado");
        } else if (estadoLista != null) {
            estadoLista.setVisibility(View.GONE);
        }
    }

    private void mostrarErro(String mensagem) {
        mostrarEstado(mensagem);
        if (getContext() != null) {
            Toast.makeText(getContext(), mensagem, Toast.LENGTH_SHORT).show();
        }
    }

    private void atualizarStatusTreinamento(UsuarioTreinamento vinculo, StatusTreinamento status) {
        if (vinculo == null || vinculo.getIdTreinamento() == null || vinculo.getIdUsuario() == null) return;

        usuarioTreinamentoService.patchStatus(vinculo.getIdTreinamento(), vinculo.getIdUsuario(), status, new RepositoryCallback<UsuarioTreinamento>() {
            @Override
            public void onSuccess(UsuarioTreinamento atualizado) {
                if (!isAdded()) return;
                vinculo.setStatus(atualizado != null && atualizado.getStatus() != null ? atualizado.getStatus() : status);
                if (listaTreinamentos != null && listaTreinamentos.getAdapter() != null) {
                    listaTreinamentos.getAdapter().notifyDataSetChanged();
                }
                Toast.makeText(requireContext(), "Status do treinamento atualizado.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(int code, String message) {
                if (!isAdded()) return;
                if (listaTreinamentos != null && listaTreinamentos.getAdapter() != null) {
                    listaTreinamentos.getAdapter().notifyDataSetChanged();
                }
                Log.e(TAG, "Falha ao atualizar status do treinamento. Código=" + code + ", mensagem=" + message);
                Toast.makeText(requireContext(), "Falha ao atualizar status. Código: " + code + ". " + message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void mostrarEstado(String mensagem) {
        if (estadoLista == null) return;
        estadoLista.setText(mensagem);
        estadoLista.setVisibility(View.VISIBLE);
    }
}
