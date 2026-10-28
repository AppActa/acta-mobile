package br.com.acta;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

import br.com.acta.Adapter.PlanoAcaoAdapter;
import br.com.acta.Api.CicloApi;
import br.com.acta.Api.PlanoAcaoApi;
import br.com.acta.Api.VerificacaoResultadoApi;
import br.com.acta.Api.UsuarioApi;
import br.com.acta.Auth.FirebaseTokenProvider;
import br.com.acta.Auth.TokenProvider;
import br.com.acta.Client.RepositoryCallback;
import br.com.acta.Client.RetrofitClient;
import br.com.acta.Model.Ciclo;
import br.com.acta.Model.Enum.StatusCiclo;
import br.com.acta.Model.PlanoAcao;
import br.com.acta.Model.VerificacaoResultado;
import br.com.acta.Model.Usuario;
import br.com.acta.Services.CicloService;
import br.com.acta.Services.PlanoAcaoService;
import br.com.acta.Services.VerificacaoResultadoService;
import br.com.acta.Services.UsuarioService;
import br.com.acta.utils.SkeletonLoading;
import br.com.acta.utils.FormatoData;

public class CheckCicloFragment extends Fragment {

    private final TokenProvider tokenProvider = new FirebaseTokenProvider(FirebaseAuth.getInstance());
    private final CicloApi cicloApi = RetrofitClient.getInstance(tokenProvider).create(CicloApi.class);
    private final PlanoAcaoApi planoAcaoApi = RetrofitClient.getInstance(tokenProvider).create(PlanoAcaoApi.class);
    private final VerificacaoResultadoApi verificacaoResultadoApi = RetrofitClient.getInstance(tokenProvider).create(VerificacaoResultadoApi.class);
    private final UsuarioApi usuarioApi = RetrofitClient.getInstance(tokenProvider).create(UsuarioApi.class);

    private final CicloService cicloService = new CicloService(cicloApi);
    private final PlanoAcaoService planoAcaoService = new PlanoAcaoService(planoAcaoApi);
    private final VerificacaoResultadoService verificacaoResultadoService = new VerificacaoResultadoService(verificacaoResultadoApi);
    private final UsuarioService usuarioService = new UsuarioService(usuarioApi);

    private RecyclerView rvPlanosAcaoCheck;
    private TextView txtSemInformacoesCheck;
    private TextView txtTituloCheck;
    private TextView txtStatusCheck;
    private TextView txtResponsavelCheck;
    private TextView txtDataInicioCheck;
    private TextView txtPrazoFinalCheck;
    private View cardVerificacaoInclude;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_check_ciclo, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageButton btnVoltar = view.findViewById(R.id.btnVoltarCheckCiclo);
        if (btnVoltar != null) {
            btnVoltar.setOnClickListener(v -> requireActivity().finish());
        }

        rvPlanosAcaoCheck = view.findViewById(R.id.rvPlanosAcaoCheck);
        if (rvPlanosAcaoCheck != null) {
            rvPlanosAcaoCheck.setLayoutManager(new LinearLayoutManager(requireContext()));
        }

        txtSemInformacoesCheck = view.findViewById(R.id.txtSemInformacoesCheck);
        txtTituloCheck = view.findViewById(R.id.txtTituloCheckCiclo);
        txtStatusCheck = view.findViewById(R.id.txtStatusCheckCiclo);
        txtResponsavelCheck = view.findViewById(R.id.txtResponsavelCheck);
        txtDataInicioCheck = view.findViewById(R.id.txtDataInicioCheck);
        txtPrazoFinalCheck = view.findViewById(R.id.txtPrazoFinalCheck);
        cardVerificacaoInclude = view.findViewById(R.id.cardVerificacaoResultadoInclude);

        Long idCiclo = 0L;
        if (getArguments() != null && getArguments().containsKey("idCiclo")) {
            idCiclo = getArguments().getLong("idCiclo", 0L);
        } else if (requireActivity().getIntent() != null && requireActivity().getIntent().hasExtra("idCiclo")) {
            idCiclo = requireActivity().getIntent().getLongExtra("idCiclo", 0L);
        }

        if (idCiclo != 0L) {
            buscarCiclo(idCiclo);
            buscarVerificacaoResultado(idCiclo);
            buscarPlanosAcao(idCiclo);
        }
    }

    private int obterIndiceStatus(StatusCiclo statusCiclo) {
        if (statusCiclo == null) return 0;
        String statusStr = statusCiclo.name().toUpperCase();
        switch (statusStr) {
            case "EXECUCAO":
            case "DO":
            case "DOING":
                return 1;
            case "VERIFICACAO":
            case "CHECK":
            case "ACOMPANHAR":
                return 2;
            case "PADRONIZACAO":
            case "ACT":
            case "CONCLUIDO":
            case "DONE":
                return 3;
            case "PLANEJAMENTO":
            case "PLAN":
            default:
                return 0;
        }
    }

    private void buscarCiclo(Long idCiclo) {
        cicloService.getCiclo(idCiclo, SkeletonLoading.wrap(getView(), new RepositoryCallback<Ciclo>() {
            @Override
            public void onSuccess(Ciclo ciclo) {
                if (!isAdded() || ciclo == null) return;

                buscarResponsavel(ciclo.getIdGestor());

                if (txtTituloCheck != null && ciclo.getTitulo() != null) {
                    txtTituloCheck.setText(ciclo.getTitulo());
                }

                if (txtStatusCheck != null && ciclo.getStatus() != null) {
                    txtStatusCheck.setText("Etapa: " + ciclo.getStatus().name());
                }

                if (txtDataInicioCheck != null && ciclo.getDataInicio() != null) {
                    txtDataInicioCheck.setText("Iniciado em: " + FormatoData.formatar(requireContext(), ciclo.getDataInicio()));
                }

                if (txtPrazoFinalCheck != null && ciclo.getDataEstimadaFim() != null) {
                    txtPrazoFinalCheck.setText("Prazo final: " + FormatoData.formatar(requireContext(), ciclo.getDataEstimadaFim()));
                }

                // Validação Acumulativa: FASE CHECK (índice 2). Qualquer status com índice >= 2 pode visualizar o CHECK.
                int statusIndex = obterIndiceStatus(ciclo.getStatus());
                boolean podeAcessarCheck = statusIndex >= 2;

                if (podeAcessarCheck) {
                    if (txtSemInformacoesCheck != null) txtSemInformacoesCheck.setVisibility(View.GONE);
                    if (cardVerificacaoInclude != null) cardVerificacaoInclude.setVisibility(View.VISIBLE);
                    if (rvPlanosAcaoCheck != null) rvPlanosAcaoCheck.setVisibility(View.VISIBLE);
                } else {
                    if (cardVerificacaoInclude != null) cardVerificacaoInclude.setVisibility(View.GONE);
                    if (rvPlanosAcaoCheck != null) rvPlanosAcaoCheck.setVisibility(View.GONE);
                    if (txtSemInformacoesCheck != null) txtSemInformacoesCheck.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onError(int code, String message) {
            }
        }));
    }

    private void buscarResponsavel(Long idGestor) {
        if (txtResponsavelCheck == null) return;
        if (idGestor == null) {
            txtResponsavelCheck.setText("Responsável pelo ciclo: Não informado");
            return;
        }

        usuarioService.buscarUsuario(idGestor, new RepositoryCallback<Usuario>() {
            @Override
            public void onSuccess(Usuario usuario) {
                if (!isAdded() || txtResponsavelCheck == null) return;
                String nome = usuario == null || usuario.getNome() == null || usuario.getNome().trim().isEmpty()
                        ? "Não informado"
                        : usuario.getNome().trim();
                txtResponsavelCheck.setText("Responsável pelo ciclo: " + nome);
            }

            @Override
            public void onError(int code, String message) {
                if (isAdded() && txtResponsavelCheck != null) {
                    txtResponsavelCheck.setText("Responsável pelo ciclo: Não informado");
                }
            }
        });
    }

    private void buscarVerificacaoResultado(Long idCiclo) {
        verificacaoResultadoService.buscarPorCiclo(idCiclo, SkeletonLoading.wrap(getView(), new RepositoryCallback<List<VerificacaoResultado>>() {
            @Override
            public void onSuccess(List<VerificacaoResultado> lista) {
                if (!isAdded() || lista == null || lista.isEmpty()) return;

                VerificacaoResultado resultado = lista.get(0);

                if (cardVerificacaoInclude != null) {
                    TextView txtBadge = cardVerificacaoInclude.findViewById(R.id.txtStatusVerificacaoBadge);
                    TextView txtObservacao = cardVerificacaoInclude.findViewById(R.id.txtObservacaoResultado);
                    View btnVerEfeitos = cardVerificacaoInclude.findViewById(R.id.btnVerEfeitosSecundarios);

                    if (txtBadge != null && resultado.getStatus() != null) {
                        txtBadge.setText(resultado.getStatus().name());
                    }
                    if (txtObservacao != null && resultado.getObservacao() != null) {
                        txtObservacao.setText(resultado.getObservacao());
                    }

                    if (btnVerEfeitos != null) {
                        btnVerEfeitos.setOnClickListener(v -> {
                            Bundle args = new Bundle();
                            if (resultado.getId() != null) {
                                args.putLong("idResultado", resultado.getId());
                            }
                            args.putLong("idCiclo", idCiclo);
                            if (getView() != null) {
                                Navigation.findNavController(getView()).navigate(R.id.efeitosSecundariosFragment, args);
                            }
                        });
                    }
                }
            }

            @Override
            public void onError(int code, String message) {
            }
        }));
    }

    private void buscarPlanosAcao(Long idCiclo) {
        planoAcaoService.buscarPorCiclo(idCiclo, SkeletonLoading.wrap(getView(), new RepositoryCallback<List<PlanoAcao>>() {
            @Override
            public void onSuccess(List<PlanoAcao> planoAcaoList) {
                if (!isAdded()) return;

                if (planoAcaoList != null && !planoAcaoList.isEmpty()) {
                    if (rvPlanosAcaoCheck != null) {
                        rvPlanosAcaoCheck.setVisibility(View.VISIBLE);
                        PlanoAcaoAdapter adapter = new PlanoAcaoAdapter(planoAcaoList, plano -> {
                            Bundle args = new Bundle();
                            if (plano.getId() != null) {
                                args.putLong("idPlanoAcao", plano.getId());
                            }
                            if (getView() != null) {
                                Navigation.findNavController(getView()).navigate(R.id.metasPlanoAcaoFragment, args);
                            }
                        });
                        rvPlanosAcaoCheck.setAdapter(adapter);
                    }
                } else {
                    if (rvPlanosAcaoCheck != null) rvPlanosAcaoCheck.setVisibility(View.GONE);
                }
            }

            @Override
            public void onError(int code, String message) {
            }
        }));
    }
}
