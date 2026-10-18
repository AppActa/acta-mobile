package br.com.acta;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
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
import java.util.function.Consumer;

import br.com.acta.Api.NotificacaoApi;
import br.com.acta.Client.RetrofitClientFcm;
import br.com.acta.Model.Notificacao;
import br.com.acta.utils.DeepLink;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.R.layout.simple_list_item_1;

public class NotificacoesFragment extends Fragment {
    private final NotificacaoApi api = RetrofitClientFcm.api();
    private final List<Call<?>> chamadas = new ArrayList<>();
    private List<Notificacao> notificacoes = new ArrayList<>();
    private ListView listaNotificacoes;
    private TextView resumoNotificacoes;
    private View botaoAtualizar;
    private View botaoLerTodas;
    private String idNotificacaoPendente;
    private String deepLinkPendente;
    private int geracaoView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle state) {
        return inflater.inflate(R.layout.fragment_notificacoes, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle state) {
        listaNotificacoes = view.findViewById(R.id.listaNotificacoes);
        resumoNotificacoes = view.findViewById(R.id.resumoNotificacoes);
        botaoAtualizar = view.findViewById(R.id.atualizarNotificacoes);
        botaoLerTodas = view.findViewById(R.id.lerTodasNotificacoes);
        idNotificacaoPendente = state != null ? state.getString("pendente") : getArguments() == null ? null : getArguments().getString("idNotificacao");
        deepLinkPendente = state != null ? state.getString("deepLinkPendente") : getArguments() == null ? null : getArguments().getString("deepLink");
        view.findViewById(R.id.voltarNotificacoes).setOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());
        botaoAtualizar.setOnClickListener(v -> carregar());
        botaoLerTodas.setOnClickListener(v -> {
            botaoLerTodas.setEnabled(false);
            executar(api.lerTodas(), quantidadeAlterada -> carregar());
        });
        listaNotificacoes.setOnItemClickListener((parent, viewItem, posicao, idItem) -> abrir(notificacoes.get(posicao)));

        carregar();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle out) {
        super.onSaveInstanceState(out);
        out.putString("pendente", idNotificacaoPendente);
        out.putString("deepLinkPendente", deepLinkPendente);
    }

    private <T> void executar(Call<T> chamada, Consumer<T> sucesso) {
        chamadas.add(chamada);
        int geracaoAtual = geracaoView;

        chamada.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<T> chamadaRetornada, Response<T> resposta) {
                chamadas.remove(chamadaRetornada);
                if (chamadaRetornada.isCanceled() || getView() == null || geracaoAtual != geracaoView) return;

                if (resposta.isSuccessful()) {
                    T dados = resposta.body();
                    if (dados == null) {
                        erro("Não foi possível carregar os dados. Tente atualizar.");
                        return;
                    }
                    sucesso.accept(dados);
                } else {
                    erro(mensagemErroHttp(resposta.code()));
                }
            }

            @Override
            public void onFailure(Call<T> chamadaRetornada, Throwable falha) {
                chamadas.remove(chamadaRetornada);
                if (!chamadaRetornada.isCanceled() && getView() != null && geracaoAtual == geracaoView) erro("Não foi possível conectar. Tente atualizar.");
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

    private void erro(String mensagem) {
        botaoAtualizar.setEnabled(true);
        botaoLerTodas.setEnabled(true);
        resumoNotificacoes.setText(mensagem);
        Toast.makeText(requireContext(), mensagem, Toast.LENGTH_LONG).show();
    }

    private void carregar() {
        botaoAtualizar.setEnabled(false);
        botaoLerTodas.setEnabled(false);
        resumoNotificacoes.setText("Carregando…");

        executar(api.listar(), dados -> {
            notificacoes = dados;
            List<String> linhas = new ArrayList<>();
            for (Notificacao notificacao : notificacoes) {
                linhas.add((notificacao.lidoEm == null ? "● Não lida · " : "Lida · ") + texto(notificacao.tipo)
                    + "\n" + texto(notificacao.categoria) + " · " + notificacao.criadoEm
                    + (notificacao.expiraEm == null ? "" : "\nExpira em: " + notificacao.expiraEm));
            }

            listaNotificacoes.setAdapter(new ArrayAdapter<>(requireContext(), simple_list_item_1, linhas));
            botaoAtualizar.setEnabled(true);
            botaoLerTodas.setEnabled(true);
            resumoNotificacoes.setText(notificacoes.isEmpty() ? "Nenhuma notificação." : notificacoes.size() + " notificações");
            executar(api.quantidade(), quantidade -> resumoNotificacoes.setText(
                notificacoes.isEmpty() ? "Nenhuma notificação." : quantidade + " não lidas · " + notificacoes.size() + " notificações"));
            if (deepLinkPendente != null) {
                DeepLink link = DeepLink.parse(deepLinkPendente);
                deepLinkPendente = null;
                if (getArguments() != null) getArguments().remove("deepLink");
                for (int i = 0; i < notificacoes.size(); i++) {
                    Notificacao notificacao = notificacoes.get(i);
                    if (link != null && link.corresponde(notificacao.tipoEntidade, notificacao.idEntidade)) {
                        listaNotificacoes.setSelection(i);
                        abrir(notificacao);
                        return;
                    }
                }
                new AlertDialog.Builder(requireContext())
                    .setMessage("Não foi possível abrir este item e ele não foi encontrado na sua central.")
                    .setPositiveButton("OK", null).show();
            }
            if (idNotificacaoPendente != null) {
                String idNotificacao = idNotificacaoPendente;
                idNotificacaoPendente = null;
                if (getArguments() != null) getArguments().remove("idNotificacao");
                for (int i = 0; i < notificacoes.size(); i++) {
                    if (String.valueOf(notificacoes.get(i).id).equals(idNotificacao)) {
                        listaNotificacoes.setSelection(i);
                        abrir(notificacoes.get(i));
                        return;
                    }
                }

                new AlertDialog.Builder(requireContext()).setMessage("Esta notificação expirou ou não está disponível nesta conta.")
                    .setPositiveButton("OK", null).show();
            }
        });
    }

    private String texto(String valor) { return valor == null ? "" : valor.replace('_', ' '); }

    private void abrir(Notificacao notificacao) {
        new AlertDialog.Builder(requireContext()).setTitle(texto(notificacao.tipo))
            .setMessage(texto(notificacao.categoria) + "\nCriada em: " + notificacao.criadoEm
                + "\nReferência: " + texto(notificacao.tipoEntidade) + " " + texto(notificacao.idEntidade)
                + (notificacao.lidoEm == null ? "\nNão lida" : "\nLida em: " + notificacao.lidoEm))
            .setNeutralButton("Fechar", null)
            .setNegativeButton("Marcar como lida", (dialog, botao) -> executar(api.ler(notificacao.id), item -> carregar()))
            .setPositiveButton("Abrir", (dialog, botao) -> executar(api.ler(notificacao.id), item -> {
                carregar();
                destino(notificacao);
            })).show();
    }

    private void destino(Notificacao notificacao) {
        DeepLink link = DeepLink.parse(notificacao.deepLink);
        if (link == null || !link.corresponde(notificacao.tipoEntidade, notificacao.idEntidade)) {
            erro("O endereço deste item não está disponível.");
            return;
        }
        if (!((HomeActivity) requireActivity()).abrirDeepLink(link.toString())) {
            new AlertDialog.Builder(requireContext())
                .setMessage("Este item ainda não tem destino disponível no aplicativo. A notificação permanece nesta central.")
                .setPositiveButton("OK", null).show();
            return;
        }
    }

    @Override
    public void onDestroyView() {
        geracaoView++;
        for (Call<?> chamada : chamadas) chamada.cancel();
        chamadas.clear();
        super.onDestroyView();
    }
}
