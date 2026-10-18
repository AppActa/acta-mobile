package br.com.acta.utils;

import java.net.URI;
import java.net.URISyntaxException;

import br.com.acta.Model.Enum.DestinoDeepLink;

public final class DeepLink {
    public final DestinoDeepLink destino;
    public final String id;

    private DeepLink(DestinoDeepLink destino, String id) {
        this.destino = destino;
        this.id = id;
    }

    public static DeepLink parse(String endereco) {
        if (endereco == null) return null;

        try {
            URI uri = new URI(endereco);
            if (!"acta".equalsIgnoreCase(uri.getScheme()) || uri.getHost() == null) return null;
            if (uri.getUserInfo() != null || uri.getPort() != -1 || uri.getRawQuery() != null || uri.getRawFragment() != null) return null;

            DestinoDeepLink destino = DestinoDeepLink.porRota(uri.getHost());
            if (destino == null) return null;

            String caminho = uri.getRawPath();
            if (caminho == null || !caminho.startsWith("/")) return null;
            String id = caminho.substring(1);

            if (!destino.aceitaId(id)) return null;

            return new DeepLink(destino, id);
        } catch (URISyntaxException erro) {
            return null;
        }
    }

    public long idNumerico() {
        return Long.parseLong(id);
    }

    public boolean corresponde(String tipoEntidade, String idEntidade) {
        return destino.correspondeTipoEntidade(tipoEntidade) && id.equalsIgnoreCase(idEntidade);
    }

    @Override
    public String toString() {
        return "acta://" + destino.getRota() + "/" + id;
    }
}