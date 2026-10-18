package br.com.acta.Model.Enum;

public enum DestinoDeepLink {
    TAREFA("tarefas"),
    CICLO("ciclos"),
    TREINAMENTO("treinamentos"),
    FORMULARIO("formularios"),
    LICAO_APRENDIDA("licoes"),
    NOTIFICACAO("notificacoes");

    private final String rota;

    DestinoDeepLink(String rota) {
        this.rota = rota;
    }

    public String getRota() {
        return rota;
    }

    public static DestinoDeepLink porRota(String rota) {
        for (DestinoDeepLink destino : values()) {
            if (destino.rota.equalsIgnoreCase(rota)) return destino;
        }
        return null;
    }

    public boolean aceitaId(String id) {
        if (id == null) return false;

        switch (this) {
            case FORMULARIO:
            case LICAO_APRENDIDA: return id.matches("[0-9a-fA-F]{24}") || ehUuid(id);
            default:
                if (!id.matches("[1-9][0-9]*")) return false;
                try {
                    Long.parseLong(id);
                    return true;
                } catch (NumberFormatException erro) { return false; }
        }
    }

    public boolean correspondeTipoEntidade(String tipoEntidade) {
        return this != NOTIFICACAO && name().equals(tipoEntidade);
    }

    private static boolean ehUuid(String id) {
        return id.matches("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}");
    }
}