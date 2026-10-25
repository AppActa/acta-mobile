package br.com.acta;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.ListPopupWindow;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import br.com.acta.Api.ChatbotApi;
import br.com.acta.Api.CicloApi;
import br.com.acta.Api.MeApi;
import br.com.acta.Api.UsuarioApi;
import br.com.acta.Adapter.ChatAdapter;
import br.com.acta.Auth.FirebaseTokenProvider;
import br.com.acta.Auth.TokenProvider;
import br.com.acta.Client.RepositoryCallback;
import br.com.acta.Client.RetrofitClient;
import br.com.acta.Client.RetrofitClientAuth;
import br.com.acta.Client.RetrofitClientChatbot;
import br.com.acta.Model.Ciclo;
import br.com.acta.Model.ChatItem;
import br.com.acta.Model.ChatRequest;
import br.com.acta.Model.ChatResponse;
import br.com.acta.Model.Enum.StatusCiclo;
import br.com.acta.Model.ListarChatsResponse;
import br.com.acta.Model.Me;
import br.com.acta.Model.MensagemItem;
import br.com.acta.Model.MensagensResponse;
import br.com.acta.Model.NovaConversaRequest;
import br.com.acta.Model.NovaConversaResponse;
import br.com.acta.Model.UsuarioCiclo;
import br.com.acta.Services.CicloService;
import br.com.acta.Services.ChatbotService;
import br.com.acta.Services.MeService;
import br.com.acta.Services.UsuarioService;

public class ChatFragment extends Fragment {
    private static final String PREFS_CHATBOT = "chatbot_preferences";
    private static final String PREF_PRIMEIRO_ACESSO = "primeiroAcessoChatbot";

    public interface Host {
        void onHistoricoAtualizado(List<ChatItem> conversas);
    }

    private final List<ChatMessage> mensagens = new ArrayList<>();
    private final TokenProvider tokenProvider = new FirebaseTokenProvider(FirebaseAuth.getInstance());
    private final MeService meService = new MeService(
            RetrofitClientAuth.getInstance(tokenProvider).create(MeApi.class));
    private final ChatbotService chatbotService = new ChatbotService(
            RetrofitClientChatbot.getInstance(tokenProvider).create(ChatbotApi.class));
    private final UsuarioService usuarioService = new UsuarioService(
            RetrofitClient.getInstance(tokenProvider).create(UsuarioApi.class));
    private final CicloService cicloService = new CicloService(
            RetrofitClient.getInstance(tokenProvider).create(CicloApi.class));

    private ChatAdapter adapter;
    private RecyclerView chatMessages;
    private EditText chatInput;
    private View cycleSelector;
    private TextView cycleName;
    private TextView cyclePhase;
    private Host host;
    private final List<Ciclo> ciclosDisponiveis = new ArrayList<>();
    private Ciclo cicloSelecionado;
    private Integer usuarioId;
    private Integer empresaId;
    private String sessionId;
    private boolean perfilPronto;
    private boolean sessaoPronta;
    private boolean enviando;
    private boolean conversaAberta;
    private int geracaoConversa;
    private int geracaoCiclos;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof Host) host = (Host) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chat, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        chatMessages = view.findViewById(R.id.chatMessages);
        chatMessages.setLayoutManager(new LinearLayoutManager(requireContext()) {
            @Override
            public boolean canScrollVertically() {
                return conversaAberta;
            }
        });
        adapter = new ChatAdapter(mensagens, this::enviarMensagem);
        chatMessages.setAdapter(adapter);
        if (mensagens.isEmpty()) mensagens.add(new ChatMessage(telaInicialChat(), ""));
        adapter.notifyDataSetChanged();

        cycleSelector = view.findViewById(R.id.cycleSelector);
        cycleName = view.findViewById(R.id.cycleName);
        cyclePhase = view.findViewById(R.id.cyclePhase);
        cycleSelector.setOnClickListener(v -> mostrarSeletorCiclo());
        atualizarSeletorCiclo();

        chatInput = requireActivity().findViewById(R.id.chatInput);
        if (chatInput != null) chatInput.setEnabled(false);
        carregarPerfil();
    }

    private void carregarPerfil() {
        meService.getMe(new RepositoryCallback<Me>() {
            @Override
            public void onSuccess(Me perfil) {
                if (!isAdded()) return;
                if (perfil == null || !perfil.temPerfilActa()) {
                    mostrarErro("Não foi possível identificar seu perfil ACTA.");
                    return;
                }
                usuarioId = idApi(perfil.getIdUsuario());
                empresaId = idApi(perfil.getIdEmpresa());
                if (usuarioId == null || empresaId == null) {
                    mostrarErro("Os dados do seu perfil não são válidos para iniciar a conversa.");
                    return;
                }
                perfilPronto = true;
                carregarCiclos();
                criarConversa(null);
                carregarHistorico();
            }

            @Override
            public void onError(int statusCode, String message) {
                if (isAdded()) mostrarErro(message);
            }
        });
    }

    @Nullable
    private Integer idApi(Long id) {
        if (id == null || id <= 0 || id > Integer.MAX_VALUE) return null;
        return id.intValue();
    }

    private void carregarCiclos() {
        final int geracao = ++geracaoCiclos;
        usuarioService.buscarCiclosUsuario(usuarioId.longValue(), new RepositoryCallback<List<UsuarioCiclo>>() {
            @Override
            public void onSuccess(List<UsuarioCiclo> associacoes) {
                if (!isAdded() || geracao != geracaoCiclos) return;
                Set<Long> idsUnicos = new LinkedHashSet<>();
                Map<Long, Ciclo> ciclosPorId = new LinkedHashMap<>();
                if (associacoes != null) {
                    for (UsuarioCiclo associacao : associacoes) {
                        if (associacao == null || associacao.getIdCiclo() == null
                                || idApi(associacao.getIdCiclo()) == null) continue;
                        Long id = associacao.getIdCiclo();
                        idsUnicos.add(id);
                        Ciclo ciclo = new Ciclo();
                        ciclo.setId(id);
                        String nome = associacao.getNomeCiclo();
                        ciclo.setTitulo(nome == null || nome.trim().isEmpty()
                                ? "Ciclo " + id : nome.trim());
                        ciclo.setStatus(associacao.getStatusCiclo());
                        ciclosPorId.put(id, ciclo);
                    }
                }

                List<Long> ids = new ArrayList<>(idsUnicos);
                if (ids.isEmpty()) {
                    atualizarCiclosDisponiveis(ids, ciclosPorId, geracao);
                    return;
                }
                int[] pendentes = {ids.size()};
                for (Long id : ids) {
                    cicloService.getCiclo(id, new RepositoryCallback<Ciclo>() {
                        @Override
                        public void onSuccess(Ciclo ciclo) {
                            if (!isAdded() || geracao != geracaoCiclos) return;
                            if (ciclo != null) ciclosPorId.put(id, ciclo);
                            finalizarBuscaCiclo(ids, ciclosPorId, pendentes, geracao);
                        }

                        @Override
                        public void onError(int statusCode, String message) {
                            if (!isAdded() || geracao != geracaoCiclos) return;
                            finalizarBuscaCiclo(ids, ciclosPorId, pendentes, geracao);
                        }
                    });
                }
            }

            @Override
            public void onError(int statusCode, String message) {
                if (!isAdded() || geracao != geracaoCiclos) return;
                ciclosDisponiveis.clear();
                cicloSelecionado = null;
                atualizarSeletorCiclo();
            }
        });
    }

    private void finalizarBuscaCiclo(List<Long> ids, Map<Long, Ciclo> ciclosPorId,
                                     int[] pendentes, int geracao) {
        if (--pendentes[0] != 0 || !isAdded() || geracao != geracaoCiclos) return;
        atualizarCiclosDisponiveis(ids, ciclosPorId, geracao);
    }

    private void atualizarCiclosDisponiveis(List<Long> ids, Map<Long, Ciclo> ciclosPorId, int geracao) {
        if (!isAdded() || geracao != geracaoCiclos) return;
        ciclosDisponiveis.clear();
        for (Long id : ids) {
            Ciclo ciclo = ciclosPorId.get(id);
            if (ciclo != null) ciclosDisponiveis.add(ciclo);
        }
        if (cicloSelecionado != null && !contemCiclo(cicloSelecionado.getId())) cicloSelecionado = null;
        atualizarSeletorCiclo();
    }

    private boolean contemCiclo(Long idCiclo) {
        if (idCiclo == null) return false;
        for (Ciclo ciclo : ciclosDisponiveis) {
            if (idCiclo.equals(ciclo.getId())) return true;
        }
        return false;
    }

    private void mostrarSeletorCiclo() {
        if (!isAdded()) return;
        List<Ciclo> opcoes = new ArrayList<>();
        opcoes.add(null);
        opcoes.addAll(ciclosDisponiveis);
        ArrayAdapter<Ciclo> adapterOpcoes = new ArrayAdapter<Ciclo>(
                requireContext(), R.layout.item_chat_cycle_option, R.id.cycleOptionName, opcoes) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                return criarLinhaCiclo(position, convertView, parent);
            }

            @NonNull
            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                return criarLinhaCiclo(position, convertView, parent);
            }

            private View criarLinhaCiclo(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View linha = convertView == null
                        ? LayoutInflater.from(requireContext()).inflate(R.layout.item_chat_cycle_option, parent, false)
                        : convertView;
                Ciclo ciclo = getItem(position);
                TextView nome = linha.findViewById(R.id.cycleOptionName);
                TextView marca = linha.findViewById(R.id.cycleOptionCheck);
                nome.setText(ciclo == null ? "Todos os ciclos" : nomeCiclo(ciclo));
                boolean ativo = ciclo == null ? cicloSelecionado == null
                        : cicloSelecionado != null && ciclo.getId() != null
                        && ciclo.getId().equals(cicloSelecionado.getId());
                marca.setVisibility(ativo ? View.VISIBLE : View.INVISIBLE);
                return linha;
            }
        };

        ListPopupWindow menu = new ListPopupWindow(requireContext());
        menu.setAnchorView(cycleSelector);
        menu.setAdapter(adapterOpcoes);
        menu.setModal(true);
        menu.setWidth(cycleSelector.getWidth());
        menu.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        menu.setVerticalOffset(Math.round(6 * getResources().getDisplayMetrics().density));
        menu.setBackgroundDrawable(requireContext().getDrawable(R.drawable.bg_chat_cycle_dropdown));
        menu.setOnItemClickListener((parent, view, position, id) -> {
            cicloSelecionado = position == 0 ? null : ciclosDisponiveis.get(position - 1);
            atualizarSeletorCiclo();
            menu.dismiss();
        });
        menu.show();
    }

    private String nomeCiclo(Ciclo ciclo) {
        String titulo = ciclo == null ? null : ciclo.getTitulo();
        return titulo == null || titulo.trim().isEmpty()
                ? "Ciclo " + (ciclo == null ? "" : ciclo.getId()) : titulo.trim();
    }

    private void atualizarSeletorCiclo() {
        if (cycleName == null || cyclePhase == null) return;
        if (cicloSelecionado == null) {
            cycleName.setText("Todos os ciclos");
            cyclePhase.setText("TODOS");
            return;
        }
        cycleName.setText(nomeCiclo(cicloSelecionado));
        cyclePhase.setText(statusCurto(cicloSelecionado.getStatus()));
    }

    private String statusCurto(StatusCiclo status) {
        if (status == null) return "CICLO";
        switch (status) {
            case PLANEJAMENTO: return "PLAN";
            case EXECUCAO: return "EXEC";
            case VERIFICACAO: return "VER";
            case PADRONIZACAO: return "PAD";
            case CONCLUIDO: return "OK";
            case CANCELADO: return "CANC";
            case PAUSADO: return "PAUS";
            default: return "CICLO";
        }
    }

    private void criarConversa(@Nullable String sessaoAnterior) {
        if (!perfilPronto) return;
        sessaoPronta = false;
        atualizarCampoHabilitado();
        final int geracao = ++geracaoConversa;
        chatbotService.novaConversa(
                new NovaConversaRequest(usuarioId, empresaId, sessaoAnterior),
                new RepositoryCallback<NovaConversaResponse>() {
                    @Override
                    public void onSuccess(NovaConversaResponse resposta) {
                        if (!isAdded() || geracao != geracaoConversa) return;
                        sessionId = resposta.getSessionId();
                        sessaoPronta = sessionId != null && !sessionId.isEmpty();
                        atualizarCampoHabilitado();
                        if (!sessaoPronta) mostrarErro("A API não retornou uma sessão para a conversa.");
                    }

                    @Override
                    public void onError(int statusCode, String message) {
                        if (!isAdded() || geracao != geracaoConversa) return;
                        mostrarErro(message);
                        atualizarCampoHabilitado();
                    }
                }
        );
    }

    public void carregarHistorico() {
        if (!perfilPronto) return;
        chatbotService.listarChats(usuarioId, empresaId, 50, new RepositoryCallback<ListarChatsResponse>() {
            @Override
            public void onSuccess(ListarChatsResponse resposta) {
                if (isAdded() && host != null) host.onHistoricoAtualizado(resposta.getChats());
            }

            @Override
            public void onError(int statusCode, String message) {
                if (isAdded()) mostrarErro(message);
            }
        });
    }

    public void abrirConversa(String idSessao) {
        if (!perfilPronto || idSessao == null || idSessao.isEmpty()) return;
        conversaAberta = true;
        int geracao = ++geracaoConversa;
        sessionId = idSessao;
        sessaoPronta = false;
        enviando = false;
        atualizarCampoHabilitado();
        mensagens.clear();
        mensagens.add(new ChatMessage(ChatMessage.Type.LOADING, ""));
        adapter.notifyDataSetChanged();
        chatbotService.listarMensagens(idSessao, usuarioId, empresaId, 100, new RepositoryCallback<MensagensResponse>() {
            @Override
            public void onSuccess(MensagensResponse resposta) {
                if (!isAdded() || geracao != geracaoConversa) return;
                mensagens.clear();
                List<MensagemItem> itens = resposta.getMensagens();
                if (itens != null) {
                    for (MensagemItem item : itens) {
                        if (item == null || item.getContent() == null) continue;
                        ChatMessage.Type tipo = "user".equalsIgnoreCase(item.getRole())
                                || "usuario".equalsIgnoreCase(item.getRole())
                                ? ChatMessage.Type.USER : ChatMessage.Type.CATO;
                        String conteudo = tipo == ChatMessage.Type.CATO
                                ? normalizarResposta(item.getContent()) : item.getContent();
                        mensagens.add(new ChatMessage(tipo, conteudo));
                    }
                }
                if (mensagens.isEmpty()) mensagens.add(new ChatMessage(telaInicialChat(), ""));
                sessionId = resposta.getSessionId() == null ? idSessao : resposta.getSessionId();
                sessaoPronta = true;
                adapter.notifyDataSetChanged();
                atualizarVisualConversa();
                atualizarCampoHabilitado();
                rolarParaFinal();
            }

            @Override
            public void onError(int statusCode, String message) {
                if (!isAdded() || geracao != geracaoConversa) return;
                mensagens.clear();
                mensagens.add(new ChatMessage(ChatMessage.Type.CATO, mensagemErro(message)));
                sessaoPronta = true;
                adapter.notifyDataSetChanged();
                atualizarVisualConversa();
                atualizarCampoHabilitado();
            }
        });
    }

    public void iniciarNovaConversa() {
        String sessaoAnterior = sessionId;
        conversaAberta = false;
        geracaoConversa++;
        sessionId = null;
        sessaoPronta = false;
        enviando = false;
        mensagens.clear();
        mensagens.add(new ChatMessage(telaInicialChat(), ""));
        if (adapter != null) adapter.notifyDataSetChanged();
        if (chatInput != null) chatInput.setText("");
        atualizarVisualConversa();
        if (perfilPronto) criarConversa(sessaoAnterior);
        else atualizarCampoHabilitado();
    }

    private ChatMessage.Type telaInicialChat() {
        SharedPreferences preferencias = requireContext().getSharedPreferences(PREFS_CHATBOT, Context.MODE_PRIVATE);
        boolean primeiroAcesso = preferencias.getBoolean(PREF_PRIMEIRO_ACESSO, true);
        return primeiroAcesso ? ChatMessage.Type.WELCOME : ChatMessage.Type.SUGGESTIONS;
    }

    public void enviarMensagem(String texto) {
        if (!sessaoPronta || enviando || texto == null || texto.trim().isEmpty()) return;
        conversaAberta = true;
        String mensagem = texto.trim();
        requireContext().getSharedPreferences(PREFS_CHATBOT, Context.MODE_PRIVATE)
                .edit()
                .putBoolean(PREF_PRIMEIRO_ACESSO, false)
                .apply();
        if (mensagens.size() == 1 && (mensagens.get(0).getType() == ChatMessage.Type.WELCOME
                || mensagens.get(0).getType() == ChatMessage.Type.SUGGESTIONS)) mensagens.clear();

        mensagens.add(new ChatMessage(ChatMessage.Type.USER, mensagem));
        mensagens.add(new ChatMessage(ChatMessage.Type.LOADING, ""));
        int posicaoCarregando = mensagens.size() - 1;
        adapter.notifyDataSetChanged();
        enviando = true;
        atualizarCampoHabilitado();
        atualizarVisualConversa();
        rolarParaFinal();

        int geracao = geracaoConversa;
        List<Integer> idsCiclo = new ArrayList<>();
        for (Ciclo ciclo : ciclosDisponiveis) {
            Integer id = ciclo == null ? null : idApi(ciclo.getId());
            if (id != null && !idsCiclo.contains(id)) idsCiclo.add(id);
        }
        if (idsCiclo.isEmpty()) idsCiclo = null;
        Integer idCicloAtivo = cicloSelecionado == null ? null : idApi(cicloSelecionado.getId());
        if (idCicloAtivo != null && (idsCiclo == null || !idsCiclo.contains(idCicloAtivo))) {
            idCicloAtivo = null;
        }
        ChatRequest request = new ChatRequest(mensagem, sessionId, idsCiclo, idCicloAtivo, usuarioId, empresaId);
        chatbotService.enviarMensagem(request, new RepositoryCallback<ChatResponse>() {
            @Override
            public void onSuccess(ChatResponse resposta) {
                if (!isAdded() || geracao != geracaoConversa) return;
                sessionId = resposta.getSessionId() == null ? sessionId : resposta.getSessionId();
                substituirCarregando(posicaoCarregando, mensagemErro(normalizarResposta(resposta.getResposta())));
                enviando = false;
                atualizarCampoHabilitado();
                carregarHistorico();
                rolarParaFinal();
            }

            @Override
            public void onError(int statusCode, String message) {
                if (!isAdded() || geracao != geracaoConversa) return;
                substituirCarregando(posicaoCarregando, mensagemErro(message));
                enviando = false;
                atualizarCampoHabilitado();
                rolarParaFinal();
            }
        });
    }

    private String mensagemErro(String texto) {
        return texto == null || texto.trim().isEmpty()
                ? "Não foi possível concluir a solicitação. Tente novamente."
                : texto;
    }

    private String normalizarResposta(String resposta) {
        if (resposta == null) return null;
        String respostaTrim = resposta.trim();
        if (!respostaTrim.startsWith("[")
                || !(respostaTrim.contains("'type'") || respostaTrim.contains("\"type\""))) {
            return respostaTrim;
        }
        Matcher matcher = Pattern.compile("['\\\"]text['\\\"]\\s*:\\s*(['\\\"])(.*?)(?<!\\\\)\\1", Pattern.DOTALL)
                .matcher(resposta);
        StringBuilder texto = new StringBuilder();
        while (matcher.find()) {
            if (texto.length() > 0) texto.append('\n');
            texto.append(matcher.group(2)
                    .replace("\\n", "\n")
                    .replace("\\r", "\r")
                    .replace("\\t", "\t")
                    .replace("\\'", "'")
                    .replace("\\\"", "\"")
                    .replace("\\\\", "\\"));
        }
        return texto.length() > 0 ? texto.toString().trim() : respostaTrim;
    }

    private void substituirCarregando(int posicao, String texto) {
        if (posicao >= 0 && posicao < mensagens.size()
                && mensagens.get(posicao).getType() == ChatMessage.Type.LOADING) {
            mensagens.set(posicao, new ChatMessage(ChatMessage.Type.CATO, texto));
            adapter.notifyItemChanged(posicao);
        }
    }

    private void mostrarErro(String texto) {
        if (adapter == null) return;
        if (mensagens.size() == 1 && (mensagens.get(0).getType() == ChatMessage.Type.WELCOME
                || mensagens.get(0).getType() == ChatMessage.Type.SUGGESTIONS)) mensagens.clear();
        mensagens.add(new ChatMessage(ChatMessage.Type.CATO, mensagemErro(texto)));
        adapter.notifyDataSetChanged();
        atualizarVisualConversa();
        rolarParaFinal();
    }

    private void atualizarCampoHabilitado() {
        if (chatInput != null) chatInput.setEnabled(sessaoPronta && !enviando);
    }

    private void atualizarVisualConversa() {
        View root = getView();
        if (root == null || !isAdded()) return;
        boolean conversaIniciada = !mensagens.isEmpty()
                && mensagens.get(0).getType() != ChatMessage.Type.WELCOME
                && mensagens.get(0).getType() != ChatMessage.Type.SUGGESTIONS;
        if (conversaIniciada) root.setBackgroundResource(R.drawable.bg_chat_content_wash);
        else root.setBackground(null);
        int padding = conversaIniciada ? Math.round(12 * getResources().getDisplayMetrics().density) : 0;
        chatMessages.setPadding(0, padding, 0, padding);
    }

    private void rolarParaFinal() {
        if (chatMessages != null && adapter != null && adapter.getItemCount() > 0) {
            chatMessages.post(() -> chatMessages.scrollToPosition(adapter.getItemCount() - 1));
        }
    }

    @Override
    public void onDestroyView() {
        chatMessages = null;
        chatInput = null;
        adapter = null;
        cycleSelector = null;
        cycleName = null;
        cyclePhase = null;
        super.onDestroyView();
    }

    @Override
    public void onDetach() {
        host = null;
        super.onDetach();
    }
}
