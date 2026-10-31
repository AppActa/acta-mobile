package br.com.acta;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

import br.com.acta.Adapter.NotificacaoAdapter;
import br.com.acta.Api.NotificacaoApi;
import br.com.acta.Client.RetrofitClientFcm;
import br.com.acta.Model.Notificacao;
import br.com.acta.Model.Enum.DestinoDeepLink;
import br.com.acta.utils.DeepLink;
import br.com.acta.utils.FormatoData;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificacoesFragment extends Fragment {
    private final NotificacaoApi notificacaoApi = RetrofitClientFcm.api();
    private final List<Call<?>> chamadasPendentes = new ArrayList<>();
    private List<Notificacao> notificacoes = new ArrayList<>();
    private List<Notificacao> notificacoesExibidas = new ArrayList<>();
    private ListView listaNotificacoes;
    private TextView resumoNotificacoes;
    private View botaoAtualizar;
    private View botaoMarcarTodas;
    private String idNotificacaoPendente;
    private String deepLinkPendente;
    private int geracaoView;
    private AlertDialog dialogNotificacao;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle estadoSalvo) {
        return inflater.inflate(R.layout.fragment_notificacoes, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle estadoSalvo) {
        listaNotificacoes = view.findViewById(R.id.listaNotificacoes);
        resumoNotificacoes = view.findViewById(R.id.resumoNotificacoes);
        botaoAtualizar = view.findViewById(R.id.atualizarNotificacoes);
        botaoMarcarTodas = view.findViewById(R.id.lerTodasNotificacoes);

        if (estadoSalvo != null) {
            idNotificacaoPendente = estadoSalvo.getString("pendente");
            deepLinkPendente = estadoSalvo.getString("deepLinkPendente");
        } else if (getArguments() != null) {
            idNotificacaoPendente = getArguments().getString("idNotificacao");
            deepLinkPendente = getArguments().getString("deepLink");
        }

        view.findViewById(R.id.voltarNotificacoes).setOnClickListener(botao -> NavHostFragment.findNavController(this).navigateUp());
        botaoAtualizar.setOnClickListener(botao -> carregar());
        botaoMarcarTodas.setOnClickListener(botao -> {
            habilitarAcoes(false);
            executar(notificacaoApi.lerTodas(), quantidadeAlterada -> carregar());
        });
        listaNotificacoes.setOnItemClickListener((parent, viewItem, posicao, idItem) -> abrir(notificacoesExibidas.get(posicao)));

        carregar();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle estadoSalvo) {
        super.onSaveInstanceState(estadoSalvo);
        estadoSalvo.putString("pendente", idNotificacaoPendente);
        estadoSalvo.putString("deepLinkPendente", deepLinkPendente);
    }

    private <T> void executar(Call<T> chamada, Consumer<T> aoConcluir) {
        executar(chamada, aoConcluir, this::mostrarErro);
    }

    private <T> void executar(Call<T> chamada, Consumer<T> aoConcluir, Consumer<String> aoFalhar) {
        chamadasPendentes.add(chamada);
        int geracaoAtual = geracaoView;

        chamada.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<T> chamadaRetornada, Response<T> resposta) {
                chamadasPendentes.remove(chamadaRetornada);
                if (chamadaRetornada.isCanceled() || getView() == null || geracaoAtual != geracaoView) {
                    return;
                }

                if (resposta.isSuccessful()) {
                    T dados = resposta.body();
                    if (dados == null) {
                        aoFalhar.accept("Não foi possível carregar os dados. Tente novamente.");
                        return;
                    }
                    aoConcluir.accept(dados);
                } else {
                    aoFalhar.accept(mensagemErroHttp(resposta.code()));
                }
            }

            @Override
            public void onFailure(Call<T> chamadaRetornada, Throwable erroConexao) {
                chamadasPendentes.remove(chamadaRetornada);
                if (!chamadaRetornada.isCanceled() && getView() != null && geracaoAtual == geracaoView) {
                    aoFalhar.accept("Não foi possível conectar. Tente novamente.");
                }
            }
        });
    }

    private String mensagemErroHttp(int status) {
        switch (status) {
            case 401:
                return "Sua sessão expirou. Entre novamente.";
            case 403:
                return "Você não tem acesso a este item.";
            case 404:
                return "Item indisponível ou sem acesso.";
            default:
                return "Não foi possível concluir. Tente atualizar.";
        }
    }

    private void mostrarErro(String mensagem) {
        habilitarAcoes(true);
        resumoNotificacoes.setText(mensagem);
        Toast.makeText(requireContext(), mensagem, Toast.LENGTH_LONG).show();
    }

    private void carregar() {
        habilitarAcoes(false);
        resumoNotificacoes.setText("Carregando…");

        executar(notificacaoApi.listar(), dados -> {
            notificacoes = dados;
            mostrarNotificacoes();
            habilitarAcoes(true);
            abrirNotificacaoPendente();
        });
    }

    private void habilitarAcoes(boolean habilitadas) {
        botaoAtualizar.setEnabled(habilitadas);
        botaoMarcarTodas.setEnabled(habilitadas);
        listaNotificacoes.setEnabled(habilitadas);
    }

    private void mostrarNotificacoes() {
        notificacoesExibidas = new ArrayList<>();
        for (Notificacao notificacao : notificacoes) {
            if (notificacao.lidoEm == null) {
                notificacoesExibidas.add(notificacao);
            }
        }

        listaNotificacoes.setAdapter(new NotificacaoAdapter(requireContext(), notificacoesExibidas));
        String resumo = notificacoesExibidas.isEmpty()
            ? "Nenhuma notificação não lida."
            : notificacoesExibidas.size() + " notificações não lidas";
        resumoNotificacoes.setText(resumo);
    }

    private void abrirNotificacaoPendente() {
        if (idNotificacaoPendente == null && deepLinkPendente == null) {
            return;
        }
        String idRecebido = idNotificacaoPendente;
        boolean recebeuLink = deepLinkPendente != null;
        DeepLink linkRecebido = DeepLink.parse(deepLinkPendente);
        idNotificacaoPendente = null;
        deepLinkPendente = null;
        if (getArguments() != null) {
            getArguments().remove("idNotificacao");
            getArguments().remove("deepLink");
        }

        for (int i = 0; i < notificacoes.size(); i++) {
            Notificacao notificacao = notificacoes.get(i);
            boolean corresponde = recebeuLink
                ? linkRecebido != null && linkRecebido.corresponde(notificacao.tipoEntidade, notificacao.idEntidade)
                : String.valueOf(notificacao.id).equals(idRecebido);
            if (corresponde) {
                int posicaoExibida = notificacoesExibidas.indexOf(notificacao);
                if (posicaoExibida >= 0) {
                    listaNotificacoes.setSelection(posicaoExibida);
                }
                abrir(notificacao);
                return;
            }
        }

        if (recebeuLink) {
            criarDialog("Item indisponível", null, "Não foi possível abrir este item e ele não foi encontrado na sua central.");
        } else {
            criarDialog("Notificação indisponível", null, "Esta notificação expirou ou não está disponível nesta conta.");
        }
    }

    private String formatarTexto(String valor) {
        return valor == null ? "" : valor.replace('_', ' ');
    }

    private void abrir(Notificacao notificacao) {
        String titulo = formatarTexto(notificacao.tipo).toLowerCase(Locale.getDefault());
        if (!titulo.isEmpty()) {
            titulo = titulo.substring(0, 1).toUpperCase(Locale.getDefault()) + titulo.substring(1);
        }
        String detalhes = "Criada em: " + FormatoData.formatar(requireContext(), notificacao.criadoEm);
        detalhes += "\nReferência: " + formatarTexto(notificacao.tipoEntidade) + " " + formatarTexto(notificacao.idEntidade);
        detalhes += notificacao.lidoEm == null ? "\nNão lida" : "\nLida em: " + FormatoData.formatar(requireContext(), notificacao.lidoEm);
        if (notificacao.expiraEm != null) {
            detalhes += "\nExpira em: " + FormatoData.formatar(requireContext(), notificacao.expiraEm);
        }

        AlertDialog dialog = criarDialog(titulo, formatarTexto(notificacao.categoria), detalhes);
        Button botaoAbrir = dialog.findViewById(R.id.abrirDialogNotificacao);
        Button botaoMarcarLida = dialog.findViewById(R.id.lerDialogNotificacao);
        TextView mensagemErro = dialog.findViewById(R.id.erroDialogNotificacao);
        DeepLink linkNotificacao = DeepLink.parse(notificacao.deepLink);
        boolean treinamento = DestinoDeepLink.TREINAMENTO.correspondeTipoEntidade(notificacao.tipoEntidade) || (linkNotificacao != null && linkNotificacao.destino == DestinoDeepLink.TREINAMENTO);
        botaoAbrir.setVisibility(treinamento ? View.GONE : View.VISIBLE);
        botaoMarcarLida.setVisibility(notificacao.lidoEm == null ? View.VISIBLE : View.GONE);

        View.OnClickListener acao = botao -> {
            boolean deveAbrirDestino = botao == botaoAbrir;
            if (deveAbrirDestino && notificacao.lidoEm != null) {
                dialog.dismiss();
                abrirDestino(notificacao);
                return;
            }
            botaoAbrir.setEnabled(false);
            botaoMarcarLida.setEnabled(false);
            mensagemErro.setVisibility(View.GONE);
            executar(notificacaoApi.ler(notificacao.id), notificacaoLida -> {
                boolean dialogAberto = dialog.isShowing();
                dialog.dismiss();
                carregar();
                if (deveAbrirDestino && dialogAberto) {
                    abrirDestino(notificacao);
                }
            }, mensagem -> {
                if (!dialog.isShowing()) {
                    return;
                }
                botaoAbrir.setEnabled(true);
                botaoMarcarLida.setEnabled(true);
                mensagemErro.setText(mensagem);
                mensagemErro.setVisibility(View.VISIBLE);
            });
        };
        botaoAbrir.setOnClickListener(acao);
        botaoMarcarLida.setOnClickListener(acao);
    }

    private AlertDialog criarDialog(String titulo, String categoria, String detalhes) {
        if (dialogNotificacao != null) {
            dialogNotificacao.dismiss();
        }
        View conteudo = getLayoutInflater().inflate(R.layout.dialog_notificacao, null);
        TextView textoTitulo = conteudo.findViewById(R.id.tituloDialogNotificacao);
        TextView textoCategoria = conteudo.findViewById(R.id.categoriaDialogNotificacao);
        TextView textoDetalhes = conteudo.findViewById(R.id.detalhesDialogNotificacao);
        textoTitulo.setText(titulo);
        textoCategoria.setText(categoria);
        textoCategoria.setVisibility(categoria == null ? View.GONE : View.VISIBLE);
        textoDetalhes.setText(detalhes);
        AlertDialog dialog = new AlertDialog.Builder(requireContext()).setView(conteudo).create();
        conteudo.findViewById(R.id.voltarDialogNotificacao).setOnClickListener(view -> dialog.dismiss());
        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        dialogNotificacao = dialog;
        return dialog;
    }

    private void abrirDestino(Notificacao notificacao) {
        DeepLink link = DeepLink.parse(notificacao.deepLink);
        if (link == null || !link.corresponde(notificacao.tipoEntidade, notificacao.idEntidade)) {
            mostrarErro("O endereço deste item não está disponível.");
            return;
        }
        if (!((HomeActivity) requireActivity()).abrirDeepLink(link.toString())) {
            criarDialog("Item indisponível", null, "Este item ainda não tem destino disponível no aplicativo. A notificação permanece nesta central.");
        }
    }

    @Override
    public void onDestroyView() {
        if (dialogNotificacao != null) {
            dialogNotificacao.dismiss();
            dialogNotificacao = null;
        }
        geracaoView++;
        for (Call<?> chamada : chamadasPendentes) {
            chamada.cancel();
        }
        chamadasPendentes.clear();
        listaNotificacoes = null;
        notificacoesExibidas = new ArrayList<>();
        resumoNotificacoes = null;
        botaoAtualizar = null;
        botaoMarcarTodas = null;
        super.onDestroyView();
    }
}
