package br.com.acta;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import br.com.acta.Adapter.PerguntaAdapter;
import br.com.acta.Api.FormularioApi;
import br.com.acta.Api.MeApi;
import br.com.acta.Api.RespostaFormularioApi;
import br.com.acta.Auth.FirebaseTokenProvider;
import br.com.acta.Auth.TokenProvider;
import br.com.acta.Client.RepositoryCallback;
import br.com.acta.Client.RetrofitClientAuth;
import br.com.acta.Client.RetrofitClientMongo;
import br.com.acta.Model.Formulario;
import br.com.acta.Model.Me;
import br.com.acta.Model.Pergunta;
import br.com.acta.Model.RespostaFormulario;
import br.com.acta.Model.RespostasFormulario;
import br.com.acta.Services.FormularioService;
import br.com.acta.Services.MeService;
import br.com.acta.Services.RespostaFormularioService;
import br.com.acta.utils.DialogSucesso;

public class FormularioFragment extends Fragment {
    public static final String ARG_ID_FORMULARIO = "idFormulario";
    private final TokenProvider tokenProvider = new FirebaseTokenProvider(FirebaseAuth.getInstance());
    private final FormularioService formularioService = new FormularioService(RetrofitClientMongo.getInstance(tokenProvider).create(FormularioApi.class));
    private final RespostaFormularioService respostaService = new RespostaFormularioService(RetrofitClientMongo.getInstance(tokenProvider).create(RespostaFormularioApi.class));
    private final MeService meService = new MeService(RetrofitClientAuth.getInstance(tokenProvider).create(MeApi.class));
    private RespostasFormulario respostas = new RespostasFormulario();
    private Formulario formulario;
    private String idFormulario;
    private RecyclerView lista;
    private Button botaoEnviar;
    private View progresso;
    private View botaoTentar;
    private TextView textoStatus;
    private boolean carregando;
    private boolean enviando;
    private boolean enviado;
    private boolean respostaVerificada;
    private final ActivityResultLauncher<Intent> seletorEndereco = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), resultado -> {
                if (resultado.getResultCode() != Activity.RESULT_OK || resultado.getData() == null || respostas == null) {
                    return;
                }
                String idPergunta = resultado.getData().getStringExtra(TelaMapa.EXTRA_ID_PERGUNTA);
                String cep = resultado.getData().getStringExtra(TelaMapa.EXTRA_CEP);
                if (idPergunta == null || cep == null) {
                    return;
                }
                respostas.atualizar(idPergunta, cep);
                if (lista != null && lista.getAdapter() != null) {
                    lista.getAdapter().notifyDataSetChanged();
                } else if (isAdded() && formulario != null && getView() != null) {
                    mostrarFormulario();
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_formulario, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        KeyboardInsets.apply(view.findViewById(R.id.conteudoFormulario), false);
        lista = view.findViewById(R.id.listaPerguntasFormulario);
        lista.setLayoutManager(new LinearLayoutManager(requireContext()));
        lista.setSaveEnabled(false);
        botaoEnviar = view.findViewById(R.id.btnEnviarFormulario);
        progresso = view.findViewById(R.id.progressoFormulario);
        botaoTentar = view.findViewById(R.id.btnTentarFormulario);
        textoStatus = view.findViewById(R.id.txtStatusFormulario);
        Bundle argumentos = getArguments();
        idFormulario = argumentos == null ? null : argumentos.getString(ARG_ID_FORMULARIO);

        if (savedInstanceState != null) {
            Gson gson = new Gson();
            formulario = gson.fromJson(savedInstanceState.getString("formulario"), Formulario.class);
            RespostasFormulario respostasSalvas = gson.fromJson(savedInstanceState.getString("respostas"), RespostasFormulario.class);
            if (respostasSalvas != null) {
                respostas = respostasSalvas;
            }
            enviado = savedInstanceState.getBoolean("enviado");
        }

        view.findViewById(R.id.btnVoltarFormulario).setOnClickListener(botao -> Navigation.findNavController(botao).navigateUp());
        botaoTentar.setOnClickListener(botao -> carregar());
        botaoEnviar.setOnClickListener(botao -> enviar());
        atualizarAcoes();
        carregar();
    }

    private void carregar() {
        if (carregando) {
            return;
        }
        if (!idFormularioValido(idFormulario)) {
            erroCarregamento("Não foi possível identificar o formulário.");
            return;
        }
        carregando = true;
        respostaVerificada = false;
        mostrarMensagem(null);
        botaoTentar.setVisibility(View.GONE);
        atualizarAcoes();
        formularioService.buscarPorId(idFormulario, new RepositoryCallback<Formulario>() {
            @Override
            public void onSuccess(Formulario resultado) {
                if (!estruturaValida(resultado)) {
                    erroCarregamento("Este formulário está incompleto e não pode ser exibido.");
                    return;
                }
                formulario = resultado;
                verificarResposta();
            }

            @Override
            public void onError(int codigo, String detalhe) {
                erroCarregamento(mensagemErro(codigo, "Não foi possível carregar o formulário. Tente novamente."));
            }
        });
    }

    private boolean idFormularioValido(String id) {
        if (id == null) {
            return false;
        }
        if (id.matches("[0-9a-fA-F]{24}")) {
            return true;
        }
        try {
            return UUID.fromString(id).toString().equalsIgnoreCase(id);
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    private void verificarResposta() {
        respostaService.buscarPorFormulario(idFormulario, new RepositoryCallback<List<RespostaFormulario>>() {
            @Override
            public void onSuccess(List<RespostaFormulario> respostasExistentes) {
                enviado = respostasExistentes != null && !respostasExistentes.isEmpty();
                respostaVerificada = true;
                carregando = false;
                mostrarFormulario();
            }

            @Override
            public void onError(int codigo, String detalhe) {
                erroCarregamento(mensagemErro(codigo, "Não foi possível verificar se você já respondeu. Tente novamente."));
            }
        });
    }

    private void mostrarFormulario() {
        if (getView() == null) {
            return;
        }
        TextView titulo = requireView().findViewById(R.id.txtTituloFormulario);
        TextView descricao = requireView().findViewById(R.id.txtDescricaoFormulario);
        titulo.setText(formulario.getTitulo());
        descricao.setText(formulario.getDescricao());
        lista.setAdapter(new PerguntaAdapter(formulario.getPerguntas(), respostas, seletorEndereco));
        mostrarMensagem(enviado ? getString(R.string.formulario_ja_respondido) : null);
        atualizarAcoes();
    }

    private void enviar() {
        if (carregando || enviando || enviado || !respostaVerificada || formulario == null) {
            return;
        }
        PerguntaAdapter adapter = (PerguntaAdapter) lista.getAdapter();
        if (adapter == null || adapter.validar() != -1) {
            return;
        }
        enviando = true;
        mostrarMensagem(null);
        atualizarAcoes();
        meService.getMe(new RepositoryCallback<Me>() {
            @Override
            public void onSuccess(Me usuario) {
                if (usuario.getIdUsuario() == null) {
                    erroEnvio("Não foi possível identificar sua conta. Entre novamente.");
                    return;
                }
                salvar(usuario);
            }

            @Override
            public void onError(int codigo, String detalhe) {
                erroEnvio(mensagemErro(codigo, "Não foi possível verificar sua conta. Tente novamente."));
            }
        });
    }

    private void salvar(Me usuario) {
        RespostaFormulario resposta = new RespostaFormulario();
        resposta.setIdFormulario(idFormulario);
        resposta.setIdCiclo(formulario.getIdCiclo());
        resposta.setIdEmpresa(formulario.getIdEmpresa());
        resposta.setIdUsuario(usuario.getIdUsuario());
        resposta.setRespostas(respostas.montar(formulario.getPerguntas()));
        respostaService.inserir(idFormulario, resposta, new RepositoryCallback<RespostaFormulario>() {
            @Override
            public void onSuccess(RespostaFormulario resultado) {
                enviando = false;
                enviado = true;
                if (isAdded()) {
                    String msg = "Formulário enviado com sucesso ao gestor";
                    if (formulario != null && formulario.getTitulo() != null && !formulario.getTitulo().trim().isEmpty()) {
                        msg = "Formulário de " + formulario.getTitulo().toLowerCase().trim() + " enviado ao gestor";
                    }
                    DialogSucesso.exibir(requireContext(), msg, () -> {
                        if (isAdded() && getView() != null) {
                            Navigation.findNavController(getView()).navigateUp();
                        }
                    });
                }
                atualizarAcoes();
            }

            @Override
            public void onError(int codigo, String detalhe) {
                erroEnvio(mensagemErro(codigo, "Não foi possível confirmar o envio. Suas respostas foram mantidas. Tente novamente."));
                Toast.makeText(requireContext(), "Erro ao salvar (" + codigo + "): ", Toast.LENGTH_LONG).show();
            }
        });
    }

    private boolean estruturaValida(Formulario resultado) {
        if (idFormulario == null || !idFormulario.equalsIgnoreCase(resultado.getId()) || resultado.getPerguntas() == null || resultado.getPerguntas().isEmpty()) {
            return false;
        }
        Set<String> ids = new HashSet<>();
        for (Pergunta pergunta : resultado.getPerguntas()) {
            if (pergunta == null || pergunta.getId() == null || pergunta.getId().trim().isEmpty() || !ids.add(pergunta.getId())) {
                return false;
            }
        }
        return true;
    }

    private String mensagemErro(int codigo, String fallback) {
        if (codigo == 401) {
            return "Sua sessão expirou. Entre novamente.";
        }
        if (codigo == 403) {
            return "Sua conta não tem permissão para acessar este formulário.";
        }
        if (codigo == 404) {
            return "Este formulário não está mais disponível.";
        }
        return fallback;
    }

    private void erroCarregamento(String mensagem) {
        carregando = false;
        mostrarMensagem(mensagem);
        if (getView() != null) {
            botaoTentar.setVisibility(View.VISIBLE);
        }
        atualizarAcoes();
    }

    private void erroEnvio(String mensagem) {
        enviando = false;
        mostrarMensagem(mensagem);
        atualizarAcoes();
    }

    private void mostrarMensagem(String mensagem) {
        if (getView() == null) {
            return;
        }
        textoStatus.setText(mensagem);
        textoStatus.setTextColor(requireContext().getColor(R.color.formulario_erro));
        textoStatus.setVisibility(mensagem == null ? View.GONE : View.VISIBLE);
    }

    private void atualizarAcoes() {
        if (getView() == null) {
            return;
        }
        boolean pronto = formulario != null && respostaVerificada && !carregando && !enviando && !enviado;
        progresso.setVisibility(carregando || enviando ? View.VISIBLE : View.GONE);
        botaoEnviar.setEnabled(pronto);
        botaoEnviar.setText(enviado ? R.string.formulario_enviado : R.string.formulario_enviar);
        PerguntaAdapter adapter = (PerguntaAdapter) lista.getAdapter();
        if (adapter != null) {
            adapter.setHabilitado(pronto);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle estadoSalvo) {
        super.onSaveInstanceState(estadoSalvo);
        Gson gson = new Gson();
        estadoSalvo.putString("formulario", gson.toJson(formulario));
        estadoSalvo.putString("respostas", gson.toJson(respostas));
        estadoSalvo.putBoolean("enviado", enviado);
    }

    @Override
    public void onDestroyView() {
        lista.setAdapter(null);
        lista = null;
        botaoEnviar = null;
        progresso = null;
        botaoTentar = null;
        textoStatus = null;
        super.onDestroyView();
    }
}
